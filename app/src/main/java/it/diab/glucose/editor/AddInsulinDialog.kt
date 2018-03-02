package it.diab.glucose.editor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.AppCompatSpinner
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.extensions.asTimeFrame
import java.util.*

@SuppressLint("InflateParams")
class AddInsulinDialog(private val mContext: Context,
                       private val glucose: Glucose,
                       private val isFirst: Boolean) {
    private val mView: View
    private val mNameSpinner: AppCompatSpinner
    private val mValueEditText: EditText
    private lateinit var insulins: Array<Insulin>

    init {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mView = inflater.inflate(R.layout.dialog_insulin_to_glucose, null)
        mNameSpinner = mView.findViewById(R.id.glucose_editor_insulin_spinner)
        mValueEditText = mView.findViewById(R.id.glucose_editor_insulin_value)
    }

    fun setInsulins(list: List<Insulin>) {
        insulins = list.toTypedArray()
        val names = Array(list.size, { "" })

        val now = (if (glucose.date.time == 0L) Date() else glucose.date).asTimeFrame()

        var spinnerPosition = -1
        val currentId = if (isFirst) glucose.insulinId0 else glucose.insulinId1
        val currentValue = if (isFirst) glucose.insulinValue0 else glucose.insulinValue1

        if (currentValue != 0f) {
            mValueEditText.setText(currentValue.toString())
        }

        for (i in insulins.indices) {
            names[i] = "${insulins[i].name} (${mContext.getString(insulins[i].timeFrame.string)})"

            if (spinnerPosition == -1 && (insulins[i].uid == currentId ||
                    insulins[i].timeFrame === now)) {
                spinnerPosition = i
            }
        }

        mNameSpinner.adapter = ArrayAdapter<String>(mContext,
                android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item, names)

        mNameSpinner.setSelection(if (spinnerPosition == -1) 0 else spinnerPosition)
    }

    fun show(onPositive: (Insulin, Float) -> Unit, onNeutral: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog.Builder(mContext)
                .setTitle(R.string.glucose_editor_insulin_add)
                .setView(mView)
                .setPositiveButton(R.string.add, { _, _ ->
                    val selected = insulins[mNameSpinner.selectedItemPosition]
                    val value = mValueEditText.text.toString().toFloatOrNull() ?: 0F

                    onPositive(selected, value)
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.remove, { _, _ -> onNeutral() })
                .setOnDismissListener { onDismiss() }
                .show()
    }
}