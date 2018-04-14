package it.diab.db.converters

import android.support.test.runner.AndroidJUnit4
import it.diab.util.timeFrame.TimeFrame
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimeFrameConverterTest {

    @Test
    fun convertToInt() {
        val orig = TimeFrame.LATE_MORNING
        val test = TimeFrameConverter().toInt(orig)!!
        Assert.assertEquals(orig.toInt(), test)
    }

    @Test
    fun convertToTimeFrame() {
        val orig = 1
        val test = TimeFrameConverter().toTimeFrame(orig)!!
        Assert.assertEquals(orig, test.toInt())
    }
}