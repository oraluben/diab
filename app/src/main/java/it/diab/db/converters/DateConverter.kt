/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
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
