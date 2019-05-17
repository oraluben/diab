/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import it.diab.data.entities.Glucose
import it.diab.data.entities.Insulin
import it.diab.glucose.R
import it.diab.glucose.util.InsulinSelector
import it.diab.glucose.util.VibrationUtil
import it.diab.ui.util.UIUtils

class AddInsulinDialog(
    private val activity: Activity,
    private val glucose: Glucose,
    private val isBasal: Boolean
) {
    private val dialog = BottomSheetDialog(activity)

    private val nameSpinner: AppCompatSpinner
    private val valueEditText: EditText
    private val addButton: MaterialButton
    private val removeButton: MaterialButton

    private lateinit var insulins: Array<Insulin>

    init {
        val inflater = activity.getSystemService(LayoutInflater::class.java)
        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.dialog_insulin_to_glucose, null)

        nameSpinner = view.findViewById(R.id.glucose_editor_insulin_spinner)
        valueEditText = view.findViewById(R.id.glucose_editor_insulin_value)
        addButton = view.findViewById(R.id.glucose_editor_insulin_add)
        removeButton = view.findViewById(R.id.glucose_editor_insulin_remove)

        dialog.setContentView(view)
    }

    fun setInsulins(list: List<Insulin>) {
        insulins = list.toTypedArray()

        val currentValue = if (isBasal) glucose.insulinValue1 else glucose.insulinValue0
        if (currentValue != 0f) {
            valueEditText.setText(currentValue.toString())
        }

        val names = Array(list.size) { i ->
            "${insulins[i].name} (${activity.getString(insulins[i].timeFrame.string)})"
        }

        nameSpinner.adapter = ArrayAdapter<String>(
            activity,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, names
        )

        val spinnerPosition = InsulinSelector(glucose.timeFrame).run {
            if (isBasal) {
                suggestBasal(insulins, glucose.insulinId1)
            } else {
                suggestInsulin(insulins, glucose.insulinId0)
            }
        }

        nameSpinner.setSelection(if (spinnerPosition == -1) 0 else spinnerPosition)
    }

    fun show(onAdd: (Insulin, Float) -> Unit, onRemove: () -> Unit) {
        if (nameSpinner.adapter.isEmpty) {
            VibrationUtil.vibrateForError(activity)
            Toast.makeText(activity, R.string.glucose_editor_no_insulin, Toast.LENGTH_LONG).show()
            return
        }

        addButton.setOnClickListener {
            val selected = insulins[nameSpinner.selectedItemPosition]
            val value = valueEditText.text.toString().toFloatOrNull() ?: 0F

            onAdd(selected, value)
            dialog.dismiss()
        }
        removeButton.setOnClickListener {
            onRemove()
            dialog.dismiss()
        }

        UIUtils.setWhiteNavBarIfNeeded(activity, dialog)
        dialog.show()
    }
}
