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
import com.google.common.truth.Truth.assertThat
import it.diab.db.AppDatabase
import it.diab.db.repositories.GlucoseRepository
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.glucose
import it.diab.util.timeFrame.TimeFrame
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class OverviewViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var viewModel: OverviewViewModel

    private val glucoseValues = arrayOf(69, 99, 301, 132)
    private val glucoseList = Array(4) { i -> glucose { value = glucoseValues[i] } }

    private lateinit var testTimeFrame: TimeFrame

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        viewModel = OverviewViewModel(GlucoseRepository.getInstance(context))

        testTimeFrame = Calendar.getInstance().time.asTimeFrame()
    }

    @Test
    fun getAverageLastWeek() {
        viewModel.getDataSets(glucoseList.asList()) { _, avg ->
            assertThat(avg[testTimeFrame.toInt()].x).isEqualTo(glucoseValues.average().toFloat())
        }
    }
}