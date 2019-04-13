/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.settings.ui

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
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import it.diab.core.util.PluginManager
import it.diab.core.util.extensions.format
import it.diab.core.util.extensions.get
import it.diab.export.ExportService
import it.diab.settings.R
import it.diab.settings.widgets.ExportPreference
import java.util.Date

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: SharedPreferences
    private lateinit var pluginManager: PluginManager

    private lateinit var pluginManagerPref: Preference
    private lateinit var pluginRemoverPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        prefs = preferenceManager.sharedPreferences
        pluginManager = PluginManager(requireContext())

        val exportPluginData = findPreference("pref_export_ml_data") as ExportPreference
        exportPluginData.bind(object : ExportPreference.ExportPreferenceCallbacks {
            override fun getActivity() = activity
            override fun requestExport() { requestExport(REQUEST_ML_EXPORT) }
        })

        val exportXlsx = findPreference("pref_export_xlsx") as ExportPreference
        exportXlsx.bind(object : ExportPreference.ExportPreferenceCallbacks {
            override fun getActivity() = activity
            override fun requestExport() { requestExport(REQUEST_XLSX_EXPORT) }
        })

        val pluginCategory = findPreference("plugin_category") as PreferenceCategory
        pluginManagerPref = pluginCategory.findPreference("pref_plugin_manager")
        pluginRemoverPref = pluginCategory.findPreference("pref_plugin_remover")

        pluginManagerPref.setOnPreferenceClickListener { fetchPluginFile() }
        pluginRemoverPref.setOnPreferenceClickListener { askPluginRemoval() }

        prefs.registerOnSharedPreferenceChangeListener(this)
        updatePluginPrefs()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ML_EXPORT -> handleExportResult(REQUEST_ML_EXPORT)
            REQUEST_ML_AUTH -> handleUserAuthResult(REQUEST_ML_EXPORT, resultCode)
            REQUEST_XLSX_EXPORT -> handleExportResult(REQUEST_XLSX_EXPORT)
            REQUEST_XLSX_AUTH -> handleUserAuthResult(REQUEST_XLSX_EXPORT, resultCode)
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
        }
    }

    private fun requestExport(requestCode: Int) {
        val activity = activity ?: return

        if (!hasStorageAccess(activity)) {
            requestStorageAccess(requestCode)
            return
        }

        val keyguardManager = activity.getSystemService(KeyguardManager::class.java)
        val title = getString(R.string.export_ask_auth_title)
        val message = getString(R.string.export_ask_auth_message)
        val requestIntent = keyguardManager.createConfirmDeviceCredentialIntent(title, message)

        val authCode = when (requestCode) {
            REQUEST_ML_EXPORT -> REQUEST_ML_AUTH
            REQUEST_XLSX_EXPORT -> REQUEST_XLSX_AUTH
            else -> -1
        }

        if (requestIntent != null) {
            startActivityForResult(requestIntent, authCode)
            return
        }

        // No secure lock screen is set
        startExport(requestCode)
    }

    private fun startExport(requestCode: Int) {
        val activity = activity ?: return

        val action = when (requestCode) {
            REQUEST_ML_EXPORT -> ExportService.TARGET_CSV
            REQUEST_XLSX_EXPORT -> ExportService.TARGET_XLSX
            else -> -1
        }

        val intent = Intent(activity, ExportService::class.java)
        intent.putExtra(ExportService.EXPORT_TARGET, action)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent)
        } else {
            activity.startService(intent)
        }
    }

    private fun handleExportResult(requestCode: Int) {
        val activity = activity ?: return

        if (hasStorageAccess(activity)) {
            requestExport(requestCode)
            return
        }

        val title = when (requestCode) {
            REQUEST_ML_AUTH -> R.string.export_ask_ml_title
            REQUEST_XLSX_AUTH -> R.string.export_ask_xlsx_title
            else -> 0
        }

        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(R.string.export_ask_permission_message)
            .setPositiveButton(R.string.export_ask_permission_positive) { _, _ -> startExport(requestCode) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun handleUserAuthResult(requestCode: Int, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            startExport(requestCode)
            return
        }

        Toast.makeText(context, R.string.export_failed_auth, Toast.LENGTH_LONG).show()
    }

    private fun updatePluginPrefs() {
        val lastUpdated = prefs[PluginManager.LAST_UPDATE, 0L]

        if (lastUpdated == 0L) {
            pluginRemoverPref.isVisible = false
            pluginManagerPref.setSummary(R.string.settings_plugin_manage_summary_new)
        } else {
            pluginRemoverPref.isVisible = true
            pluginManagerPref.summary = getString(
                R.string.settings_plugin_manage_summary_installed,
                Date(lastUpdated).format("yyyy-MM-dd HH:mm")
            )
        }
    }

    private fun fetchPluginFile(): Boolean {
        startActivityForResult(pluginManager.getPickerIntent(), REQUEST_SELECT_PLUGIN)
        return true
    }

    private fun onPluginSelected(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        val context = context ?: return
        val uri = data.data ?: return
        val iStream = context.contentResolver.openInputStream(uri) ?: return

        pluginManager.install(iStream)
        Toast.makeText(context, R.string.settings_plugin_installing, Toast.LENGTH_SHORT)
            .show()
    }

    private fun askPluginRemoval(): Boolean {
        val activity = activity ?: return false

        AlertDialog.Builder(activity)
            .setTitle(R.string.settings_plugin_remove)
            .setMessage(R.string.settings_plugin_remove_confirmation)
            .setPositiveButton(R.string.settings_plugin_remove_confirmation_positive) { _, _ -> pluginManager.uninstall() }
            .setNegativeButton(android.R.string.no, null)
            .show()

        return true
    }

    private fun requestStorageAccess(requestCode: Int) {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
    }

    private fun hasStorageAccess(context: Context) = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUEST_ML_EXPORT = 391
        private const val REQUEST_ML_AUTH = 392
        private const val REQUEST_SELECT_PLUGIN = 393
        private const val REQUEST_XLSX_EXPORT = 394
        private const val REQUEST_XLSX_AUTH = 395
    }
}