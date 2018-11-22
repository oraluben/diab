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