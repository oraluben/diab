/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util

object DateUtils {
    private const val SECOND: Long = 1000 // Millisecond * 1000
    private const val MINUTE = 60 * SECOND
    private const val HOUR = 60 * MINUTE
    const val DAY = 24 * HOUR
    const val WEEK = 7 * DAY
}
