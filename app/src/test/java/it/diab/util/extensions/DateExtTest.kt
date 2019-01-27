/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.extensions

import it.diab.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DateExtTest {

    @Test
    fun get() {
        val now = System.currentTimeMillis()
        val diff = 2
        assertEquals(Date(now)[diff].time, now + (DateUtils.DAY * diff))
    }

    @Test
    fun isToday() {
        val a = Date()
        val b = Calendar.getInstance().apply {
            this[Calendar.YEAR] -= 1
        }.time
        val c = Calendar.getInstance().apply {
            this[Calendar.DAY_OF_YEAR] += 3
        }.time

        assertEquals(a.isToday(), true)
        assertEquals(b.isToday(), false)
        assertEquals(c.isToday(), false)
    }
}