/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import it.diab.data.entities.TimeFrame
import it.diab.data.extensions.glucose
import it.diab.data.extensions.insulin
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.ui.models.DataSetsModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    private lateinit var viewModel: MainViewModel

    private val testTimeFrame = TimeFrame.MORNING
    private val glucoseValues = arrayOf(69, 99, 301, 132)
    private val glucoseArray = Array(4) { i ->
        glucose {
            value = glucoseValues[i]
            timeFrame = testTimeFrame
        }
    }
    private val insulinArray = arrayOf(
        insulin {
            uid = 73
            name = "Foo"
        },
        insulin {
            uid = 42
            name = "Oof"
        }
    )

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Insert test data
        InsulinRepository.getInstance(context).apply {
            setDebugMode()
            insert(insulinArray[0])
            insert(insulinArray[1])
        }

        viewModel = MainViewModel(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        ).apply {
            runPrepare()
        }
    }

    @Test
    fun getInsulin() {
        val test = insulinArray[0]

        Assert.assertEquals(test, viewModel.getInsulin(test.uid))
    }

    @Test
    fun getAverageLastWeek() = runBlocking {
        val model = viewModel.runGetDataSets(glucoseArray.asList())

        Assert.assertTrue(model is DataSetsModel.Available)
        model as DataSetsModel.Available
        Assert.assertEquals(
            glucoseValues.average().toFloat(),
            model.average[testTimeFrame.toInt()].y
        )
    }
}