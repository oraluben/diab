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
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.insulin.ml.PluginManager
import it.diab.util.extensions.glucose
import it.diab.util.extensions.insulin
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
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
                InsulinRepository.getInstance(context))
        pluginManager = PluginManager(context)

        db.glucose().insert(testGlucose)
        db.insulin().insert(testInsulin, testBasal)
    }

    @Test
    fun setGlucose() {
        viewModel.setGlucose(testGlucose.uid) {
            viewModel.glucose.apply {
                assertThat(uid).isEqualTo(testGlucose.uid)
                assertThat(this).isEqualTo(testGlucose)
            }
        }
    }

    @Test
    fun save() {
        viewModel.setGlucose(-1) {
            runBlocking {
                val initialSize = db.glucose().getInDateRange(0, System.currentTimeMillis()).size

                viewModel.glucose.apply {
                    value = 81
                    insulinId0 = 0
                    insulinValue0 = 10.5f
                    eatLevel = Glucose.MAX
                }

                viewModel.save()

                delay(500)

                val finalSize = db.glucose().getInDateRange(0, System.currentTimeMillis()).size
                assertThat(finalSize).isEqualTo(initialSize + 1)
            }
        }
    }

    @Test
    fun getInsulin() {
        viewModel.prepare(pluginManager) {
            viewModel.getInsulin(testInsulin.uid).run {
                assertThat(uid).isEqualTo(testInsulin.uid)
                assertThat(this).isEqualTo(testInsulin)
            }
        }
    }

    @Test
    fun hasPotentialBasal() {
        viewModel.prepare(pluginManager) {
            viewModel.glucose.timeFrame = testBasal.timeFrame
            assertThat(viewModel.hasPotentialBasal()).isTrue()
        }
    }

    @Test
    fun getInsulinByTimeFrame() {
        viewModel.prepare(pluginManager) {
            viewModel.glucose.timeFrame = testInsulin.timeFrame
            assertThat(viewModel.getInsulinByTimeFrame().timeFrame)
                    .isEqualTo(viewModel.glucose.timeFrame)
        }
    }

    @Test
    fun applyInsulinSugestion() {
        val test = 6.5f
        viewModel.applyInsulinSuggestion(test, testInsulin) {
            viewModel.glucose.apply {
                assertThat(insulinValue0).isEqualTo(test)
                assertThat(insulinId0).isEqualTo(testInsulin.uid)
            }
        }
    }
}