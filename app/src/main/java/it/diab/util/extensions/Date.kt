/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.extensions

import android.content.res.Resources
import it.diab.R
import it.diab.core.util.extensions.format
import it.diab.core.util.extensions.getCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

operator fun Date.get(diff: Int): Date {
    val calendar = getCalendar()
    calendar.add(Calendar.DAY_OF_YEAR, diff)
    return calendar.time
}

fun Date.isToday(): Boolean {
    val calendar = getCalendar()
    val today = Calendar.getInstance()
    return calendar[Calendar.YEAR] == today[Calendar.YEAR] &&
        calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]
}

fun Date.getAsMinutes(): Float {
    return getCalendar().run {
        get(Calendar.HOUR_OF_DAY) * 60f + get(Calendar.MINUTE)
    }
}

fun Date.getWeekDay(): String = format("EEEE")

fun Date.diff(other: Date): Int {
    val a = getCalendar()
    val b = other.getCalendar()
    return (a[Calendar.YEAR] - b[Calendar.YEAR]) * 365 +
        a[Calendar.DAY_OF_YEAR] - b[Calendar.DAY_OF_YEAR]
}

fun Date.getHeader(res: Resources, comparedTo: Date, format: SimpleDateFormat): String {
    val diff = diff(comparedTo)
    return when {
        diff == 0 -> res.getString(R.string.time_today)
        diff == -1 -> res.getString(R.string.time_yesterday)
        diff > -7 -> res.getString(R.string.glucose_header_last, getWeekDay())
        else -> format.format(this)
    }
}
