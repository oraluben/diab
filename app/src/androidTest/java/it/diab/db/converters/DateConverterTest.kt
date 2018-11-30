/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.db.converters

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Date

@SmallTest
class DateConverterTest {
    private val converter = DateConverter()


    @Test
    fun convertToDate() {
        val now = System.currentTimeMillis()
        assertThat(converter.toDate(now)!!.time).isEqualTo(now)
    }

    @Test
    fun convertToLong() {
        val now = Date()
        assertThat(converter.toLong(now)).isEqualTo(now.time)
    }
}