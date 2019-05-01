/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.data.extensions

import it.diab.data.entities.TimeFrame
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
}