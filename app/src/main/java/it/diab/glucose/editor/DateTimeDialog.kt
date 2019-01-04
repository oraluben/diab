/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.editor

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import it.diab.R
import it.diab.util.DateUtils
import it.diab.util.UIUtils
import java.util.Calendar
import java.util.Date

class DateTimeDialog(
    private val activity: Activity,
    private val onSelected: (Long) -> Unit
) {

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

    fun show(date: Date) {
        calendar.time = date
        val dialog = BottomSheetDialog(activity)

        val inflater = activity.getSystemService(LayoutInflater::class.java)
        @SuppressLint("InflateParams")
        val view = inflater.inflate(R.layout.dialog_date_picker, null)

        view.findViewById<TextView>(R.id.edit_dialog_date_today).apply {
            setOnClickListener {
                dialog.dismiss()
                showTimePicker()
            }
        }
        view.findViewById<TextView>(R.id.edit_dialog_date_yesterday).apply {
            setOnClickListener {
                dialog.dismiss()
                calendar.timeInMillis = System.currentTimeMillis() - DateUtils.DAY
                showTimePicker()
            }
        }
        view.findViewById<TextView>(R.id.edit_dialog_date_pick).apply {
            setOnClickListener {
                dialog.dismiss()
                showDatePicker()
            }
        }

        dialog.setContentView(view)
        UIUtils.setWhiteNavBarIfNeeded(activity, dialog)
        dialog.show()
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
