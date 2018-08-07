package it.diab.glucose.overview

import androidx.lifecycle.ViewModelProviders
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import it.diab.MainActivity
import it.diab.db.AppDatabase
import it.diab.util.extensions.asTimeFrame
import it.diab.util.timeFrame.TimeFrame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class OverviewViewModelTest {
    private var mViewModel: OverviewViewModel? = null

    private val mGlucoseValues = arrayOf(69, 99, 301, 132)
    private var mTestTimeFrame: TimeFrame? = null

    @Suppress("MemberVisibilityCanBePrivate")
    @get:Rule
    val testRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @Before
    fun setup() {
        mViewModel = ViewModelProviders.of(testRule.activity)[OverviewViewModel::class.java]

        AppDatabase.TEST_MODE = true

        val calendar = Calendar.getInstance()
        mTestTimeFrame = calendar.time.asTimeFrame()
    }

    @Test
    fun getAverageLastWeek() {
        val test = mViewModel!!.getAverageLastWeek()

        val supposedAverage = mGlucoseValues.average().toFloat()
        val result = test[mTestTimeFrame!!.toInt()]
        assert(result == supposedAverage)
    }
}