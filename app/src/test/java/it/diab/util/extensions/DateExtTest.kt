/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class DateExtTest {

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
}