package it.diab.db.converters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun toDate(value: Long?): Date? =
            if (value == null) Date(0)
            else Date(value)

    @TypeConverter
    fun toLong(value: Date?): Long? = value?.time ?: 0L
}
