/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.timeFrame

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

import it.diab.R

enum class TimeFrame : ITimeFrame {
    EXTRA {
        @DrawableRes
        override val icon = R.drawable.ic_time_extra
        @StringRes
        override val string = R.string.time_extra
        override val reprHour = -1
        override fun toInt() = -1
    },

    MORNING {
        @DrawableRes
        override val icon = R.drawable.ic_time_morning
        @StringRes
        override val string = R.string.time_morning
        override val reprHour = 6
        override fun toInt() = 0
    },

    LATE_MORNING {
        @DrawableRes
        override val icon = R.drawable.ic_time_morning
        @StringRes
        override val string = R.string.time_late_morning
        override val reprHour = 10
        override fun toInt() = 1
    },

    LUNCH {
        @DrawableRes
        override val icon = R.drawable.ic_time_lunch
        @StringRes
        override val string = R.string.time_lunch
        override val reprHour = 13
        override fun toInt() = 2
    },

    AFTERNOON {
        @DrawableRes
        override val icon = R.drawable.ic_time_afternoon
        @StringRes
        override val string = R.string.time_afternoon
        override val reprHour = 16
        override fun toInt() = 3
    },

    DINNER {
        @DrawableRes
        override val icon = R.drawable.ic_time_dinner
        @StringRes
        override val string = R.string.time_dinner
        override val reprHour = 19
        override fun toInt() = 4
    },

    NIGHT {
        @DrawableRes
        override val icon = R.drawable.ic_time_night
        @StringRes
        override val string = R.string.time_night
        override val reprHour = 22
        override fun toInt() = 5
    }
}
