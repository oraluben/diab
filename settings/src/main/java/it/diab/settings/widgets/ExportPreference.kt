/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.settings.widgets

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import it.diab.settings.R

class ExportPreference : DialogPreference {
    private lateinit var callbacks: ExportPreferenceCallbacks

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)

    override fun onClick() {
        showPromptDialog()
    }

    fun bind(callbacks: ExportPreferenceCallbacks) {
        this.callbacks = callbacks
    }

    private fun showPromptDialog(): Boolean {
        val activity = callbacks.getActivity() ?: return false

        AlertDialog.Builder(activity)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(R.string.export_ask_positive) { _, _ ->
                callbacks.requestExport()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        return true
    }

    interface ExportPreferenceCallbacks {

        /**
         * Get an activity in which display dialogs
         */
        fun getActivity(): Activity?

        /**
         * Export has been confirmed, proceed
         */
        fun requestExport()
    }
}