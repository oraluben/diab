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
import androidx.preference.Preference
import it.diab.settings.R
import it.diab.settings.util.extensions.getResource

class ExportPreference : Preference {
    private lateinit var callbacks: ExportPreferenceCallbacks

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet) :
        super(context, attrs) {
        init(attrs)
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    fun bind(callbacks: ExportPreferenceCallbacks) {
        this.callbacks = callbacks
    }

    private fun init(attrs: AttributeSet) {
        onPreferenceClickListener = OnPreferenceClickListener {
            showPromptDialog(attrs)
        }
    }

    private fun showPromptDialog(attrs: AttributeSet): Boolean {
        val activity = callbacks.getActivity() ?: return false
        val title = attrs.getResource(
            R.styleable.ExportPreference_promptDialogTitle,
            R.styleable.ExportPreference,
            context
        )
        val message = attrs.getResource(
            R.styleable.ExportPreference_promptDialogMessage,
            R.styleable.ExportPreference,
            context
        )

        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
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