package it.diab.db.converters

import android.arch.persistence.room.TypeConverter

import it.diab.util.timeFrame.TimeFrame

class TimeFrameConverter {

    @TypeConverter
    fun toInt(value: TimeFrame?): Int? = value?.toInt() ?: TimeFrame.EXTRA.toInt()

    @TypeConverter
    fun toTimeFrame(value: Int?): TimeFrame? =
            if (value == null) TimeFrame.EXTRA
            else when (value) {
                0 -> TimeFrame.MORNING
                1 -> TimeFrame.LATE_MORNING
                2 -> TimeFrame.LUNCH
                3 -> TimeFrame.AFTERNOON
                4 -> TimeFrame.DINNER
                5 -> TimeFrame.NIGHT
                else -> TimeFrame.EXTRA
            }
}
