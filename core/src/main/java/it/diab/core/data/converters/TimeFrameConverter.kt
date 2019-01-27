/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.data.converters

import androidx.room.TypeConverter
import it.diab.core.data.timeframe.TimeFrame

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
