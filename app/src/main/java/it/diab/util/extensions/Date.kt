package it.diab.util.extensions

import android.content.res.Resources
import com.google.android.gms.fitness.data.HealthFields
import it.diab.R
import it.diab.util.timeFrame.TimeFrame
import java.text.SimpleDateFormat
import java.util.*

operator fun Date.get(diff: Int): Date {
    val calendar = getCalendar()
    calendar.add(Calendar.DAY_OF_YEAR , -1)
    return calendar.time
}


fun Date.getCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

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

fun Date.isToday(): Boolean {
    val calendar = getCalendar()
    val today = Calendar.getInstance()
    return calendar[Calendar.YEAR] == today[Calendar.YEAR] &&
            calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]
}

fun Date.getHour() = getCalendar()[Calendar.HOUR_OF_DAY]

fun Date.getAsMinutes() = getHour() * 60f + getCalendar()[Calendar.MINUTE]

fun Date.getWeekDay(): String = format("EEEE")

fun Date.diff(other: Date): Int {
    val a = getCalendar()
    val b = other.getCalendar()
    return (a[Calendar.YEAR] - b[Calendar.YEAR]) * 365 +
            a[Calendar.DAY_OF_YEAR] - b[Calendar.DAY_OF_YEAR]
}

fun Date.getHeader(res: Resources): Pair<String , String> {
    val diff = diff(Date())
    val title = when {
        diff == 0 -> res.getString(R.string.time_today)
        diff == -1 -> res.getString(R.string.time_yesterday)
        diff > -7 -> res.getString(R.string.time_last_x, getWeekDay())
        else -> res.getString(R.string.time_x_y_ago, diff / -7, getWeekDay())
    }
    val description = format(res.getString(R.string.time_day_month_short_format))

    return Pair(title, description)
}

fun Date.toFitMealRelation() = when (asTimeFrame().toInt()) {
    1,
    5 -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_AFTER_MEAL
    else -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL
}

fun Date.toFitSleepRelation() =
        if (asTimeFrame() == TimeFrame.MORNING) HealthFields.TEMPORAL_RELATION_TO_SLEEP_ON_WAKING
        else HealthFields.TEMPORAL_RELATION_TO_SLEEP_FULLY_AWAKE

fun Date.format(format: String) =
        SimpleDateFormat(format, Locale.getDefault()).format(this)
