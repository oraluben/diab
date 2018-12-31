/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.settings

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import it.diab.R
import it.diab.glucose.export.ExportGlucoseService
import it.diab.insulin.ml.PluginManager
import it.diab.util.UIUtils
import it.diab.util.extensions.format
import it.diab.util.extensions.get
import java.util.Date

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: SharedPreferences

    private lateinit var pluginManager: Preference
    private lateinit var pluginRemover: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        prefs = preferenceManager.sharedPreferences

        val exportData = findPreference("pref_export_data")
        exportData?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showExportDialog()
        }

        val pluginCategory = findPreference("plugin_category") as PreferenceCategory
        pluginManager = pluginCategory.findPreference("pref_plugin_manager")
        pluginRemover = pluginCategory.findPreference("pref_plugin_remover")

        pluginManager.setOnPreferenceClickListener { fetchPluginFile() }
        pluginRemover.setOnPreferenceClickListener { askPluginRemoval() }

        prefs.registerOnSharedPreferenceChangeListener(this)
        updatePluginPrefs()

        val style = findPreference(PREF_UI_STYLE) as ListPreference
        setupStylePref(style)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_STORAGE_EXPORT -> handleExportResult()
            REQUEST_USER_AUTH -> handleUserAuthResult(resultCode)
            REQUEST_SELECT_PLUGIN -> onPluginSelected(resultCode, data)
        }
    }

    override fun onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)

        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PluginManager.LAST_UPDATE -> updatePluginPrefs()
            PREF_UI_STYLE -> UIUtils.setStyleMode(prefs[PREF_UI_STYLE, "1"])
        }
    }

    private fun showExportDialog(): Boolean {
        val activity = activity ?: return false

        AlertDialog.Builder(activity)
            .setTitle(R.string.export_ask_title)
            .setMessage(R.string.export_ask_message)
            .setPositiveButton(R.string.export_ask_positive) { _, _ -> requestExport() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()

        return true
    }

    private fun requestExport() {
        val activity = activity ?: return

        if (!hasStorageAccess(activity)) {
            requestStorageAccess(REQUEST_STORAGE_EXPORT)
            return
        }

        val keyguardManager = activity.getSystemService(KeyguardManager::class.java)
        val title = getString(R.string.export_ask_auth_title)
        val message = getString(R.string.export_ask_auth_message)
        val requestIntent = keyguardManager.createConfirmDeviceCredentialIntent(title, message)

        if (requestIntent != null) {
            activity.startActivityForResult(requestIntent, REQUEST_USER_AUTH)
            return
        }

        // No secure lock screen is set
        startExport()
    }

    private fun startExport() {
        val activity = activity ?: return

        val intent = Intent(activity, ExportGlucoseService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
    }

    private fun handleExportResult() {
        val activity = activity ?: return

        if (hasStorageAccess(activity)) {
            requestExport()
            return
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.export_ask_title)
            .setMessage(R.string.export_ask_permission_message)
            .setPositiveButton(R.string.export_ask_permission_positive) { _, _ -> startExport() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun handleUserAuthResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            startExport()
            return
        }

        Toast.makeText(context, R.string.export_failed_auth, Toast.LENGTH_LONG).show()
    }

    private fun updatePluginPrefs() {
        val lastUpdated = prefs[PluginManager.LAST_UPDATE, 0L]

        if (lastUpdated == 0L) {
            pluginRemover.isVisible = false
            pluginManager.setSummary(R.string.settings_plugin_manage_summary_new)
        } else {
            pluginRemover.isVisible = true
            pluginManager.summary = getString(
                R.string.settings_plugin_manage_summary_installed,
                Date(lastUpdated).format("yyyy-MM-dd HH:mm")
            )
        }
    }

    private fun fetchPluginFile(): Boolean {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/zip"
        }

        startActivityForResult(intent, REQUEST_SELECT_PLUGIN)
        return true
    }

    private fun onPluginSelected(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        val context = context ?: return
        val uri = data.data ?: return
        val iStream = context.contentResolver.openInputStream(uri) ?: return

        val manager = PluginManager(context)
        manager.install(iStream)

        Toast.makeText(context, R.string.settings_plugin_installing, Toast.LENGTH_SHORT)
            .show()
    }

    private fun askPluginRemoval(): Boolean {
        val activity = activity ?: return false

        AlertDialog.Builder(activity)
            .setTitle(R.string.settings_plugin_remove)
            .setMessage(R.string.settings_plugin_remove_confirmation)
            .setPositiveButton(R.string.settings_plugin_remove_confirmation_positive) { _, _ -> removePlugin() }
            .setNegativeButton(android.R.string.no, null)
            .show()

        return true
    }

    private fun removePlugin() {
        val context = context ?: return
        val manager = PluginManager(context)
        manager.uninstall()
    }

    private fun setupStylePref(preference: ListPreference) {
        val supportsAuto = UIUtils.supportsAutoStyleMode()
        val entries = if (supportsAuto) arrayOf(
            getString(R.string.settings_ui_theme_system),
            getString(R.string.settings_ui_theme_light),
            getString(R.string.settings_ui_theme_dark)
        )
        else arrayOf(
            getString(R.string.settings_ui_theme_light),
            getString(R.string.settings_ui_theme_dark)
        )

        val values = if (supportsAuto) arrayOf("0", "1", "2") else arrayOf("1", "2")

        preference.entries = entries
        preference.entryValues = values
    }

    private fun requestStorageAccess(requestCode: Int) {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
    }

    private fun hasStorageAccess(context: Context) = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUEST_STORAGE_EXPORT = 391
        private const val REQUEST_USER_AUTH = 392
        private const val REQUEST_SELECT_PLUGIN = 393

        const val PREF_UI_STYLE = "pref_ui_style"
    }
}