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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import it.diab.R
import it.diab.glucose.export.ExportGlucoseService

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val exportData = findPreference("pref_export_data")
        exportData.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showExportDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_STORAGE_EXPORT -> handleExportResult()
            REQUEST_USER_AUTH -> handleUserAuthResult(resultCode)
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

    private fun requestStorageAccess(requestCode: Int) {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
    }

    private fun hasStorageAccess(context: Context) = ContextCompat.checkSelfPermission(context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED


    companion object {
        private const val REQUEST_STORAGE_EXPORT = 391
        private const val REQUEST_USER_AUTH = 392
    }
}