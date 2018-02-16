package it.diab.db.converters

import android.arch.persistence.room.TypeConverter
import java.util.*

class DateConverter {

    @TypeConverter
    fun toDate(value: Long?): Date? =
            if (value == null) Date(0)
            else Date(value)

    @TypeConverter
    fun toLong(value: Date?): Long? = value?.time ?: 0L
}
