package it.diab.fit

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import it.diab.R
import it.diab.util.extensions.setDiabUi

class FitActivity : AppCompatActivity() {

    private lateinit var mViewModel: FitViewModel

    private lateinit var mCoordinator: CoordinatorLayout
    private lateinit var mHeaderText: TextView
    private lateinit var mConnectButton: AppCompatButton
    private lateinit var mDisconnectButton: AppCompatButton
    private lateinit var mDeleteAllButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this)[FitViewModel::class.java]

        setContentView(R.layout.activity_fit)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener { _ -> finish() }

        mCoordinator = findViewById(R.id.coordinator)
        mHeaderText = findViewById(R.id.fit_header_text)
        mConnectButton = findViewById(R.id.fit_connect_button)
        mDisconnectButton = findViewById(R.id.fit_disconnect_button)
        mDeleteAllButton = findViewById(R.id.fit_delete_all_button)

        mConnectButton.setOnClickListener {
            mViewModel.connect(this, GOOGLE_FIT_REQUEST_CODE)
        }
        mDisconnectButton.setOnClickListener { confirmDisconnect() }
        mDeleteAllButton.setOnClickListener { confirmDelete() }

        setupUi(mViewModel.isConnected())
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
        mViewModel.disconnect(this)

        showSnack(R.string.fit_disconnect_success)
        setupUi(false)
    }

    private fun onDeleteAll() {
        mViewModel.deleteAllData(this,
                { showSnack(R.string.fit_delete_success) },
                { showSnack(R.string.fit_delete_failure) })
    }

    private fun showSnack(@StringRes message: Int) {
        Snackbar.make(mCoordinator, getString(message), Snackbar.LENGTH_LONG)
                .setDiabUi(this)
                .show()
    }

    private fun confirmDisconnect() {
        AlertDialog.Builder(this)
                .setTitle(R.string.fit_disconnect_confim_title)
                .setMessage(R.string.fit_disconnect_confim_message)
                .setPositiveButton(R.string.fit_disconnect_confim_positive,
                        { _, _ -> onDisconnect() })
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
                .setTitle(R.string.fit_delete_confirm_title)
                .setMessage(R.string.fit_delete_confirm_message)
                .setPositiveButton(R.string.fit_delete_confim_positive,
                        { _, _ -> onDeleteAll() })
                .setNegativeButton(R.string.cancel, null)
                .show()

    }

    private fun setupUi(isConnected: Boolean) {
        mHeaderText.setText(if (isConnected) R.string.fit_status_connected else R.string.fit_status_prompt)
        mConnectButton.visibility = if (isConnected) View.GONE else View.VISIBLE
        mDeleteAllButton.isEnabled = isConnected
        mDisconnectButton.isEnabled = isConnected
    }

    companion object {
        private const val GOOGLE_FIT_REQUEST_CODE = 281
    }
}