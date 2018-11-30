/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.timeFrame

internal interface ITimeFrame {
    val icon: Int
    val string: Int
    val reprHour: Int

    fun toInt(): Int
}
