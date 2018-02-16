package it.diab.util.extensions

import it.diab.util.timeFrame.TimeFrame
import java.util.*

fun Date.getCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

fun Date.asTimeFrame(): TimeFrame {
    val hour = getHour()

    return when (hour) {
        in 7..9 -> TimeFrame.MORNING
        in 10..11 -> TimeFrame.LATE_MORNING
        in 12..13 -> TimeFrame.LUNCH
        in 14..18 -> TimeFrame.AFTERNOON
        in 19..20 -> TimeFrame.DINNER
        else -> TimeFrame.NIGHT
    }
}

fun Date.isToday(): Boolean {
    val calendar = getCalendar()
    val today = Calendar.getInstance()
    return calendar[Calendar.YEAR] == today[Calendar.YEAR] &&
            calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]
}

fun Date.getHour() = getCalendar()[Calendar.HOUR_OF_DAY]