package it.diab.db.converters

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import it.diab.util.timeFrame.TimeFrame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class TimeFrameConverterTest {

    @Test
    fun convertToInt() {
        val orig = TimeFrame.LATE_MORNING
        val test = TimeFrameConverter().toInt(orig)!!
        assert(orig.toInt() == test)
    }

    @Test
    fun convertToTimeFrame() {
        val orig = 1
        val test = TimeFrameConverter().toTimeFrame(orig)!!
        assert(orig == test.toInt())
    }
}