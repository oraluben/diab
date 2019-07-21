/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
@file:JvmName("TimeUnit")

package it.diab.core.time

abstract class TimeUnit internal constructor(
    private val num: Long,
    private val milliSecRatio: Long = 1L
) {
    internal fun getValue() = num * milliSecRatio
}

class Seconds(num: Long) : TimeUnit(num, 1000L)

class Minutes(num: Long) : TimeUnit(num, 60L * 1000L)

class Hours(num: Long) : TimeUnit(num, 60L * 60L * 1000L)

class Days(num: Long) : TimeUnit(num, 24L * 60L * 60L * 1000L)