package it.diab.db.converters

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class DateConverterTest {

    @Test
    fun convertToDate() {
        val orig = System.currentTimeMillis()
        val test = DateConverter().toDate(orig)!!
        assert(orig == test.time)
    }

    @Test
    fun convertToLong() {
        val orig = Date()
        val test = DateConverter().toLong(orig)!!
        assert(orig.time == test)
    }
}