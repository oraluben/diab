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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DateExtTest {

    @Test
    fun get() {
        val now = System.currentTimeMillis()
        val diff = 2
        assertEquals(now + (DateUtils.DAY * diff), Date(now)[diff].time)
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

        assertTrue(a.isToday())
        assertFalse(b.isToday())
        assertFalse(c.isToday())
    }

    @Test
    fun getAsMinutes() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
        }

        assertEquals(10 * 60f + 30, calendar.time.getAsMinutes())
    }

    @Test
    fun diff() {
        val a = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000)
            set(Calendar.DAY_OF_YEAR, 1)
        }

        val b = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000)
            set(Calendar.DAY_OF_YEAR, 3)
        }

        assertEquals(-2, a.time.diff(b.time))

        b[Calendar.YEAR] -= 1
        b[Calendar.DAY_OF_YEAR] -= 8
        assertEquals(371, a.time.diff(b.time))
    }
}