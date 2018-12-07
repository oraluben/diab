/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.editor

import androidx.lifecycle.ViewModelProviders
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import it.diab.db.entities.Glucose
import it.diab.test.DbTest
import it.diab.util.extensions.glucose
import it.diab.util.extensions.insulin
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlucoseEditorViewModelTest : DbTest() {
    private lateinit var viewModel: EditorViewModel

    @Suppress("MemberVisibility")
    @get:Rule
    val rule = ActivityTestRule(EditorActivity::class.java)

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
    override fun setup() {
        super.setup()

        db.glucose().insert(testGlucose)
        db.insulin().run {
            insert(testInsulin)
            insert(testBasal)
        }

        viewModel = ViewModelProviders.of(rule.activity)[EditorViewModel::class.java]
    }

    @Test
    fun setGlucose() = runBlocking {
        viewModel.setGlucose(-1) {
            assertThat(viewModel.glucose.uid).isEqualTo(0)

            viewModel.setGlucose(testGlucose.uid) {
                viewModel.glucose.run {
                    assertThat(uid).isEqualTo(testGlucose.uid)
                    assertThat(this).isEqualTo(testGlucose)
                }
            }
        }
    }

    @Test
    fun save() = runBlocking {
        val initialSize = db.glucose().allStatic.size

        viewModel.setGlucose(-1) {}

        viewModel.glucose.apply {
            value = 173
            insulinId0 = 0
            insulinValue0 = 10.5f
            eatLevel = Glucose.MAX
        }

        viewModel.save()

        delay(500)

        val finalSize = db.glucose().allStatic.size
        assertThat(finalSize).isEqualTo(initialSize + 1)
    }

    @Test
    fun getInsulin() {
        viewModel.prepare {
            viewModel.getInsulin(testInsulin.uid).run {
                assertThat(uid).isEqualTo(testInsulin.uid)
                assertThat(this).isEqualTo(testInsulin)
            }
        }
    }

    @Test
    fun hasPotentialBasal() {
        viewModel.glucose.timeFrame = testBasal.timeFrame
        assertThat(viewModel.hasPotentialBasal()).isTrue()
    }

    @Test
    fun getInsulinByTimeFrame() {
        assertThat(viewModel.getInsulinByTimeFrame().timeFrame).isEqualTo(viewModel.glucose.timeFrame)
    }

    @Test
    fun applyInsulinSuggestion() {
        val test = 6.5f

        viewModel.applyInsulinSuggestion(test, testInsulin) {
            assertThat(viewModel.glucose.insulinValue0).isEqualTo(test)
        }
    }
}
