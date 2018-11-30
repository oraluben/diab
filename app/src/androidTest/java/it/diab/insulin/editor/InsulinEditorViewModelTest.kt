/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.editor

import androidx.lifecycle.ViewModelProviders
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import it.diab.test.DbTest
import it.diab.util.extensions.insulin
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InsulinEditorViewModelTest : DbTest() {
    private lateinit var viewModel: EditorViewModel

    @Suppress("MemberVisibility")
    @get:Rule
    val rule = ActivityTestRule(EditorActivity::class.java)

    @Before
    override fun setup() {
        super.setup()

        viewModel = ViewModelProviders.of(rule.activity)[EditorViewModel::class.java]
    }

    @Test
    fun setInsulin() {
        val insulin = insulin {
            uid = 81
            name = "FooBar"
        }.also { db.insulin().insert(it) }

        viewModel.setInsulin(insulin.uid) {
            viewModel.insulin.run {
                assertThat(uid).isEqualTo(insulin.uid)
                assertThat(this).isEqualTo(insulin)
            }
        }
    }

    @Test
    fun delete() = runBlocking {
        val insulin = insulin {
            uid = 81
            name = "FooBar"
        }.also { db.insulin().insert(it) }

        viewModel.insulin = insulin

        viewModel.delete()

        delay(400)

        assertThat(db.insulin().getById(insulin.uid)).isEmpty()
    }


    @Test
    fun save() = runBlocking {
        val initialSize = db.insulin().allStatic.size

        viewModel.setInsulin(-1) {}

        viewModel.insulin.run {
            name = "barFoo"
            timeFrame = TimeFrame.LATE_MORNING
            isBasal = true
        }

        viewModel.save()

        delay(400)

        val finalSize = db.insulin().allStatic.size
        assertThat(finalSize).isEqualTo(initialSize + 1)
    }
}
