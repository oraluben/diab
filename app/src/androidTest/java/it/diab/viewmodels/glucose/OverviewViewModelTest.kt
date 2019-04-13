/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import it.diab.core.data.AppDatabase
import it.diab.core.data.entities.TimeFrame
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.util.extensions.glucose
import it.diab.viewmodels.overview.OverviewViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OverviewViewModelTest {

    private lateinit var viewModel: OverviewViewModel

    private val testTimeFrame = TimeFrame.MORNING
    private val glucoseValues = arrayOf(69, 99, 301, 132)
    private val glucoseList = Array(4) { i ->
        glucose {
            value = glucoseValues[i]
            timeFrame = testTimeFrame
        }
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = ApplicationProvider.getApplicationContext<Context>()
        viewModel = OverviewViewModel(GlucoseRepository.getInstance(context))
    }

    @Test
    fun getAverageLastWeek() = runBlocking {
        val pair = viewModel.runGetDataSets(glucoseList.asList())

        assertEquals(glucoseValues.average().toFloat(), pair.second[testTimeFrame.toInt()].y)
    }
}