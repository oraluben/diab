package it.diab.glucose

import android.arch.lifecycle.ViewModelProviders
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import it.diab.MainActivity
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.extensions.asTimeFrame
import it.diab.util.timeFrame.TimeFrame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class GlucoseViewModelTest {
    private var mViewModel: GlucoseViewModel? = null

    private val mGlucoseValues = arrayOf(89, 149, 201, 100)
    private val mData = Array(mGlucoseValues.size, { Glucose() })
    private var mTestTimeFrame: TimeFrame? = null
    private var mInsulin: Insulin? = null

    @Suppress("MemberVisibilityCanBePrivate")
    @get:Rule
    val testRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @Before
    fun setup() {
        mViewModel = ViewModelProviders.of(testRule.activity)[GlucoseViewModel::class.java]

        AppDatabase.TEST_MODE = true
        val db = AppDatabase.getInstance(testRule.activity)

        val calendar = Calendar.getInstance()
        mTestTimeFrame = calendar.time.asTimeFrame()

        for (i in 0..(mGlucoseValues.size - 1)) {
            calendar[Calendar.DAY_OF_YEAR] -= i
            mData[i] = Glucose(
                    i.toLong(),
                    mGlucoseValues[i],
                    calendar.time,
                    (-1..0).random().toLong(),
                    (0..10).random().toFloat(),
                    (-1..0).random().toLong(),
                    (0..5).random().toFloat(),
                    (0..2).random())

            db.glucose().insert(mData[i])
        }

        mInsulin = Insulin(0, "FooBar", mTestTimeFrame!!, false, false)
        db.insulin().insert(mInsulin!!)
    }

    @Test
    fun getInsulin() {
        val test = mViewModel!!.getInsulin(mInsulin!!.uid)
        assert(test.uid == mInsulin!!.uid)
        assert(test == mInsulin)
    }

    @Test
    fun getAverageLastWeek() {
        val test = mViewModel!!.getAverageLastWeek()

        val supposedAverage = mGlucoseValues.average().toFloat()
        val result = test[mTestTimeFrame!!.toInt()]
        assert(result == supposedAverage)
    }

    private fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start
}