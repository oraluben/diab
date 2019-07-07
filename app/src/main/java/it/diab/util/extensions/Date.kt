/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.extensions

import it.diab.core.util.extensions.getCalendar
import java.util.Calendar
import java.util.Date

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
