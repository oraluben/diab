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
import it.diab.core.data.AppDatabase
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.TimeFrame
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.util.PluginManager
import it.diab.core.util.extensions.glucose
import it.diab.core.util.extensions.insulin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditorViewModelTest {

    private lateinit var db: AppDatabase
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
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        viewModel = EditorViewModel(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        )
        pluginManager = PluginManager(context)

        db.glucose().insert(testGlucose)
        db.insulin().insert(testInsulin, testBasal)
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

        val initialSize = db.glucose().getInDateRange(0, System.currentTimeMillis()).size

        viewModel.glucose.apply {
            value = 81
            insulinId0 = 0
            insulinValue0 = 10.5f
            eatLevel = Glucose.MAX
        }

        viewModel.runSave()

        delay(500)

        val finalSize = db.glucose().getInDateRange(0, System.currentTimeMillis()).size
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

    @Test
    fun errors() {
        val a = 0
        val b = 1
        val c = 1 shl 1
        val d = 1 shl 2

        viewModel.setError(a)
        assertFalse(viewModel.hasError(a))
        assertFalse(viewModel.hasErrors())

        viewModel.setError(b)
        assertTrue(viewModel.hasError(b))
        assertFalse(viewModel.hasError(c))

        viewModel.setError(c)
        assertTrue(viewModel.hasError(b))
        assertTrue(viewModel.hasError(c))
        assertFalse(viewModel.hasError(d))

        viewModel.clearError(b)
        assertFalse(viewModel.hasError(b))
        assertTrue(viewModel.hasError(c))
        assertFalse(viewModel.hasError(d))

        assertTrue(viewModel.hasErrors())

        viewModel.setError(d)
        assertFalse(viewModel.hasError(b))
        assertTrue(viewModel.hasError(c))
        assertTrue(viewModel.hasError(d))

        viewModel.setError(b)
        assertTrue(viewModel.hasError(b))
        assertTrue(viewModel.hasError(c))
        assertTrue(viewModel.hasError(d))
    }
}