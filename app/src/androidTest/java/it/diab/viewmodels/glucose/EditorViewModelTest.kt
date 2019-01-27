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
import it.diab.core.data.AppDatabase
import it.diab.core.data.entities.Glucose
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.insulin.ml.PluginManager
import it.diab.core.util.extensions.glucose
import it.diab.core.util.extensions.insulin
import it.diab.core.data.timeframe.TimeFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
            assertThat(uid).isEqualTo(testGlucose.uid)
            assertThat(this).isEqualTo(testGlucose)
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
        assertThat(finalSize).isGreaterThan(initialSize)
    }

    @Test
    fun getInsulin() = runBlocking {
        viewModel.runPrepare(this, pluginManager)
        viewModel.getInsulin(testInsulin.uid).run {
            assertThat(uid).isEqualTo(testInsulin.uid)
            assertThat(this).isEqualTo(testInsulin)
        }
    }

    @Test
    fun hasPotentialBasal() = runBlocking {
        viewModel.runPrepare(this, pluginManager)

        viewModel.glucose.timeFrame = testBasal.timeFrame
        assertThat(viewModel.hasPotentialBasal()).isTrue()
    }

    @Test
    fun getInsulinByTimeFrame() = runBlocking {
        viewModel.runPrepare(this, pluginManager)
        viewModel.glucose.timeFrame = testInsulin.timeFrame

        assertThat(viewModel.getInsulinByTimeFrame().timeFrame)
            .isEqualTo(viewModel.glucose.timeFrame)
    }

    @Test
    fun applyInsulinSuggestion() = runBlocking {
        val test = 6.5f

        viewModel.runApplySuggestion(test, testInsulin)
        viewModel.glucose.run {
            assertThat(insulinValue0).isEqualTo(test)
            assertThat(insulinId0).isEqualTo(testInsulin.uid)
        }
    }

    @Test
    fun errors() {
        val a = 0
        val b = 1
        val c = 1 shl 1
        val d = 1 shl 2

        viewModel.setError(a)
        assertThat(viewModel.hasError(a)).isFalse()
        assertThat(viewModel.hasErrors()).isFalse()

        viewModel.setError(b)
        assertThat(viewModel.hasError(b)).isTrue()
        assertThat(viewModel.hasError(c)).isFalse()

        viewModel.setError(c)
        assertThat(viewModel.hasError(b)).isTrue()
        assertThat(viewModel.hasError(c)).isTrue()
        assertThat(viewModel.hasError(d)).isFalse()

        viewModel.clearError(b)
        assertThat(viewModel.hasError(b)).isFalse()
        assertThat(viewModel.hasError(c)).isTrue()
        assertThat(viewModel.hasError(d)).isFalse()

        assertThat(viewModel.hasErrors()).isTrue()

        viewModel.setError(d)
        assertThat(viewModel.hasError(b)).isFalse()
        assertThat(viewModel.hasError(c)).isTrue()
        assertThat(viewModel.hasError(d)).isTrue()

        viewModel.setError(b)
        assertThat(viewModel.hasError(b)).isTrue()
        assertThat(viewModel.hasError(c)).isTrue()
        assertThat(viewModel.hasError(d)).isTrue()
    }
}