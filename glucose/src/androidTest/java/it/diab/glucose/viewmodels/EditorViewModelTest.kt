/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import it.diab.data.entities.Glucose
import it.diab.data.entities.TimeFrame
import it.diab.data.extensions.glucose
import it.diab.data.extensions.insulin
import it.diab.data.plugin.PluginManager
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditorViewModelTest {

    private lateinit var glucoseRepo: GlucoseRepository
    private lateinit var viewModel: EditorViewModel
    private lateinit var pluginManager: PluginManager

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testGlucose = glucose {
        uid = 15
        value = 99
        insulinValue1 = 3.5f
        eatLevel = Glucose.LOW
        timeFrame = TimeFrame.DINNER
    }
    private val testInsulin = insulin {
        uid = 1
        name = "FooBar"
        timeFrame = TimeFrame.DINNER
        hasHalfUnits = true
    }
    private val testBasal = insulin {
        uid = 2
        name = "BarFoo"
        isBasal = true
    }

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        viewModel = EditorViewModel(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        )
        pluginManager = PluginManager(context)

        glucoseRepo = GlucoseRepository.getInstance(context).apply {
            setDebugMode()
            insert(testGlucose)
        }

        InsulinRepository.getInstance(context).apply {
            setDebugMode()
            insert(testInsulin)
            insert(testBasal)
        }

        // setup is required to be "void"
        Unit
    }

    @Test
    fun setGlucose() = runBlocking {
        viewModel.runSetGlucose(testGlucose.uid)
        viewModel.glucose.run {
            assertEquals(testGlucose.uid, uid)
            assertEquals(testGlucose, this)
        }
    }

    @Test
    fun save() = runBlocking {
        viewModel.runSetGlucose(-1)

        val initialSize = glucoseRepo.getInDateRange(0, System.currentTimeMillis()).size

        viewModel.glucose.apply {
            value = 81
            insulinId0 = 0
            insulinValue0 = 10.5f
            eatLevel = Glucose.MAX
        }

        viewModel.runSave()

        delay(500)

        val finalSize = glucoseRepo.getInDateRange(0, System.currentTimeMillis()).size
        assertTrue(finalSize > initialSize)
    }

    @Test
    fun getInsulin() = runBlocking {
        viewModel.runPrepare(this, pluginManager)
        viewModel.getInsulin(testInsulin.uid).run {
            assertEquals(testInsulin.uid, uid)
            assertEquals(testInsulin, this)
        }
    }

    @Test
    fun hasPotentialBasal() = runBlocking {
        viewModel.runPrepare(this, pluginManager)

        viewModel.glucose.timeFrame = testBasal.timeFrame
        assertTrue(viewModel.hasPotentialBasal())
    }

    @Test
    fun getInsulinByTimeFrame() = runBlocking {
        viewModel.runPrepare(this, pluginManager)
        viewModel.glucose.timeFrame = testInsulin.timeFrame

        assertEquals(viewModel.glucose.timeFrame, viewModel.getInsulinByTimeFrame().timeFrame)
    }

    @Test
    fun applyInsulinSuggestion() = runBlocking {
        val test = 6.5f

        viewModel.runApplySuggestion(test, testInsulin)
        viewModel.glucose.run {
            assertEquals(test, insulinValue0)
            assertEquals(testInsulin.uid, insulinId0)
        }
    }
}