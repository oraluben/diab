/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.insulin

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import it.diab.db.AppDatabase
import it.diab.db.repositories.InsulinRepository
import it.diab.util.extensions.insulin
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditorViewModelTest {
    private lateinit var db: AppDatabase
    private lateinit var viewModel: EditorViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getInstance(context)
        viewModel = EditorViewModel(InsulinRepository.getInstance(context))
    }

    @Test
    fun setInsulin() {
        val insulin = insulin {
            uid = 81
            name = "FooBar"
        }.also { db.insulin().insert(it) }

        viewModel.setInsulin(insulin.uid) {
            viewModel.insulin.apply {
                assertThat(uid).isEqualTo(insulin.uid)
                assertThat(this).isEqualTo(insulin)
            }
        }
    }

    @Test
    fun delete() = runBlocking {
        val insulin = insulin {
            uid = 12
            name = "FooBar"
        }.also { db.insulin().insert(it) }

        viewModel.insulin = insulin
        viewModel.delete()

        delay(500)

        assertThat(db.insulin().getById(insulin.uid)).isEmpty()
    }

    @Test
    fun save()  {
        viewModel.setInsulin(-1) {
            runBlocking {
                val initialSize = db.insulin().getInsulins().size

                viewModel.insulin.apply {
                    name = "BarFoo"
                    timeFrame = TimeFrame.LATE_MORNING
                    isBasal = true
                }

                viewModel.save()

                delay(500)

                val finalSize = db.insulin().getInsulins().size

                assertThat(finalSize).isEqualTo(initialSize + 1)
            }
        }
    }
}