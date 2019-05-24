/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import it.diab.core.util.DateUtils
import it.diab.glucose.R
import java.util.Calendar
import java.util.Date

class DateTimeDialog(
    private val activity: Activity,
    private val onSelected: (Long) -> Unit
) : DialogInterface.OnClickListener {

    private var calendar = Calendar.getInstance()

    private val timePickerCallback = { _: View, hour: Int, minute: Int ->
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = minute

        onSelected(calendar.timeInMillis)
    }

    private val datePickerCallback = { _: View, year: Int, month: Int, day: Int ->
        calendar.set(year, month, day)

        showTimePicker()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            0 -> {
                dialog?.dismiss()
                showTimePicker()
            }
            1 -> {
                dialog?.dismiss()
                calendar.timeInMillis = System.currentTimeMillis() - DateUtils.DAY
                showTimePicker()
            }
            2 -> {
                dialog?.dismiss()
                showDatePicker()
            }
        }
    }

    fun show(date: Date) {
        calendar.time = date

        val options = activity.resources.run {
            arrayOf(
                getString(R.string.time_today),
                getString(R.string.time_yesterday),
                getString(R.string.glucose_editor_date_pick)
            )
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.glucose_editor_time_dialog)
            .setItems(options, this)
            .show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            activity,
            R.style.AppTheme_DatePickerDialog,
            timePickerCallback,
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            true
        ).show()
    }

    private fun showDatePicker() {
        DatePickerDialog(
            activity,
            R.style.AppTheme_DatePickerDialog,
            datePickerCallback,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }
}
