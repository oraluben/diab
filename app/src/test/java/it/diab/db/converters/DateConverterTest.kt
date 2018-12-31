/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.db.converters

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class DateConverterTest {
    private val converter = DateConverter()

    @Test
    fun convertToDate() {
        val now = System.currentTimeMillis()
        assertEquals(converter.toDate(now)!!.time, now)
    }

    @Test
    fun convertToLong() {
        val now = Date()
        assertEquals(converter.toLong(now), now.time)
    }
}