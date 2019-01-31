/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util.extensions

import it.diab.core.data.entities.TimeFrame
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.asTimeFrame(): TimeFrame {
    val hour = getHour()

    return when (hour) {
        in 6..9 -> TimeFrame.MORNING
        in 10..11 -> TimeFrame.LATE_MORNING
        in 12..13 -> TimeFrame.LUNCH
        in 14..18 -> TimeFrame.AFTERNOON
        in 19..20 -> TimeFrame.DINNER
        else -> TimeFrame.NIGHT
    }
}

fun Date.format(format: String): String =
    SimpleDateFormat(format, Locale.getDefault()).format(this)

fun Date.getCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

fun Date.getHour() = getCalendar()[Calendar.HOUR_OF_DAY]