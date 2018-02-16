package it.diab.util.extensions

import it.diab.util.timeFrame.TimeFrame

fun Int.toTimeFrame() = when (this) {
    0 -> TimeFrame.MORNING
    1 -> TimeFrame.LATE_MORNING
    2 -> TimeFrame.LUNCH
    3 -> TimeFrame.AFTERNOON
    4 -> TimeFrame.DINNER
    5 -> TimeFrame.NIGHT
    else -> TimeFrame.EXTRA
}