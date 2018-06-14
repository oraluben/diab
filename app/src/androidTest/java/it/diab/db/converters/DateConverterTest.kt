package it.diab.db.converters

import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
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
        Assert.assertEquals(orig, test.time)
    }

    @Test
    fun convertToLong() {
        val orig = Date()
        val test = DateConverter().toLong(orig)!!
        Assert.assertEquals(orig.time, test)
    }
}