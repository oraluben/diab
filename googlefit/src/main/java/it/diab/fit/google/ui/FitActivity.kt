/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fit.google.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import it.diab.fit.google.R
import it.diab.fit.google.viewmodels.FitViewModel
import it.diab.fit.google.viewmodels.FitViewModelFactory

class FitActivity : AppCompatActivity() {

    private lateinit var viewModel: it.diab.fit.google.viewmodels.FitViewModel

    private lateinit var coordinator: CoordinatorLayout
    private lateinit var headerText: TextView
    private lateinit var connectButton: AppCompatButton
    private lateinit var disconnectButton: AppCompatButton
    private lateinit var deleteAllButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = FitViewModelFactory()
        viewModel = ViewModelProviders.of(this, factory)[FitViewModel::class.java]

        setContentView(R.layout.activity_fit)

        findViewById<Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
            setNavigationIcon(R.drawable.ic_toolbar_back)
            setNavigationOnClickListener { finish() }
        }

        coordinator = findViewById(R.id.coordinator)
        headerText = findViewById(R.id.fit_header_text)
        connectButton = findViewById(R.id.fit_connect_button)
        disconnectButton = findViewById(R.id.fit_disconnect_button)
        deleteAllButton = findViewById(R.id.fit_delete_all_button)

        connectButton.setOnClickListener {
            viewModel.connect(
                this,
                FitActivity.GOOGLE_FIT_REQUEST_CODE
            )
        }
        disconnectButton.setOnClickListener { confirmDisconnect() }
        deleteAllButton.setOnClickListener { confirmDelete() }

        setupUi(viewModel.isConnected())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GOOGLE_FIT_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    setupUi(true)
                } else {
                    showSnack(R.string.fit_login_error)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onDisconnect() {
        viewModel.disconnect(this)

        showSnack(R.string.fit_disconnect_success)
        setupUi(false)
    }

    private fun onDeleteAll() {
        viewModel.deleteAllData(this,
            { showSnack(R.string.fit_delete_success) },
            { showSnack(R.string.fit_delete_failure) })
    }

    private fun showSnack(@StringRes message: Int) {
        Snackbar.make(coordinator, getString(message), Snackbar.LENGTH_LONG)
            .show()
    }

    private fun confirmDisconnect() {
        AlertDialog.Builder(this)
            .setTitle(R.string.fit_disconnect_confim_title)
            .setMessage(R.string.fit_disconnect_confim_message)
            .setPositiveButton(R.string.fit_disconnect_confim_positive) { _, _ -> onDisconnect() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.fit_delete_confirm_title)
            .setMessage(R.string.fit_delete_confirm_message)
            .setPositiveButton(R.string.fit_delete_confim_positive) { _, _ -> onDeleteAll() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun setupUi(isConnected: Boolean) {
        headerText.setText(if (isConnected) R.string.fit_status_connected else R.string.fit_status_prompt)
        connectButton.visibility = if (isConnected) View.GONE else View.VISIBLE
        deleteAllButton.isEnabled = isConnected
        disconnectButton.isEnabled = isConnected
    }

    companion object {
        private const val GOOGLE_FIT_REQUEST_CODE = 281
    }
}