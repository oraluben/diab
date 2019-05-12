/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.settings.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import it.diab.core.util.extensions.format
import it.diab.core.util.extensions.get
import it.diab.data.plugin.PluginManager
import it.diab.export.utils.SecureFilePickerHelper
import it.diab.settings.R
import it.diab.settings.widgets.ExportPreference
import java.util.Date

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    SecureFilePickerHelper.Callbacks {
    private lateinit var prefs: SharedPreferences
    private lateinit var pluginManager: PluginManager
    private lateinit var secureFilePicker: SecureFilePickerHelper

    private lateinit var pluginManagerPref: Preference
    private lateinit var pluginRemoverPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        prefs = preferenceManager.sharedPreferences
        pluginManager = PluginManager(requireContext())
        secureFilePicker = SecureFilePickerHelper(this, this)

        val exportPluginData = findPreference("pref_export_ml_data") as ExportPreference
        exportPluginData.bind(object : ExportPreference.Callbacks {
            override fun getActivity() = activity
            override fun startExport() { secureFilePicker.authenticate(SecureFilePickerHelper.ML) }
        })

        val exportXlsx = findPreference("pref_export_xlsx") as ExportPreference
        exportXlsx.bind(object : ExportPreference.Callbacks {
            override fun getActivity() = activity
            override fun startExport() { secureFilePicker.authenticate(SecureFilePickerHelper.XLSX) }
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
        if (requestCode == REQUEST_SELECT_PLUGIN) {
            onPluginSelected(resultCode, data)
            return
        }

        secureFilePicker.onResult(requestCode, resultCode, data)
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

    override fun onAuthentication(category: Int, result: SecureFilePickerHelper.AuthResult) {
        if (result == SecureFilePickerHelper.AuthResult.FAILURE) {
            Toast.makeText(context, R.string.export_failed_auth, Toast.LENGTH_LONG).show()
            return
        }

        secureFilePicker.pickDestination(category)
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

    companion object {
        private const val REQUEST_SELECT_PLUGIN = 393
    }
}