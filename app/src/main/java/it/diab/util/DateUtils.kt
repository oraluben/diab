/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util

object DateUtils {
    private var SECOND: Long = 1000 // Millisecond * 1000
    private var MINUTE = 60 * SECOND
    private var HOUR = 60 * MINUTE
    var DAY = 24 * HOUR
    var WEEK = 7 * DAY
}
