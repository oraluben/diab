/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util

import it.diab.core.data.entities.TimeFrame
import it.diab.core.util.extensions.asTimeFrame
import it.diab.core.util.extensions.format
import it.diab.core.util.extensions.getCalendar
import it.diab.core.util.extensions.getHour
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateExtTest {

    @Test
    fun asTimeFrame() {
        val calendar = Calendar.getInstance()

        calendar[Calendar.HOUR_OF_DAY] = 8
        assertEquals(TimeFrame.MORNING, calendar.time.asTimeFrame())

        calendar[Calendar.HOUR_OF_DAY] = 18
        assertEquals(TimeFrame.AFTERNOON, calendar.time.asTimeFrame())

        calendar[Calendar.HOUR_OF_DAY] = 0
        assertEquals(TimeFrame.NIGHT, calendar.time.asTimeFrame())
    }

    @Test
    fun format() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1971)
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
        }

        assertEquals("1971-01-01", calendar.time.format("yyyy-MM-dd"))

        assertEquals("10:30", calendar.time.format("HH:mm"))
    }

    @Test
    fun getCalendar() {
        val calendar = Calendar.getInstance()
        val date = calendar.time
        assertEquals(calendar, date.getCalendar())
    }

    @Test
    fun getHour() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
        }

        assertEquals(10, calendar.time.getHour())
    }
}