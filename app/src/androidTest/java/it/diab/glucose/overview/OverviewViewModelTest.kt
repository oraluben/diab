/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.overview

import androidx.lifecycle.ViewModelProviders
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import it.diab.MainActivity
import it.diab.db.AppDatabase
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.glucose
import it.diab.util.timeFrame.TimeFrame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class OverviewViewModelTest {
    private lateinit var viewModel: OverviewViewModel

    private val glucoseValues = arrayOf(69, 99, 301, 132)
    private val glucoseList = Array(4) { i -> glucose { value = glucoseValues[i] }}

    private var testTimeFrame: TimeFrame? = null

    @Suppress("MemberVisibility")
    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        viewModel = ViewModelProviders.of(rule.activity)[OverviewViewModel::class.java]

        AppDatabase.TEST_MODE = true

        val calendar = Calendar.getInstance()
        testTimeFrame = calendar.time.asTimeFrame()
    }

    @Test
    fun getAverageLastWeek() {
        viewModel.getDataSets(glucoseList.asList()) { _, average ->
            val supposedAverage = glucoseValues.average().toFloat()
            val result = average[testTimeFrame!!.toInt()].x
            assert(result == supposedAverage)
        }
    }
}