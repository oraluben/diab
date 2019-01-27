/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util

import it.diab.core.util.extensions.getCalendar
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateExtTest {

    @Test
    fun getCalendar() {
        val calendar = Calendar.getInstance()
        val date = calendar.time
        assertEquals(date.getCalendar(), calendar)
    }
}