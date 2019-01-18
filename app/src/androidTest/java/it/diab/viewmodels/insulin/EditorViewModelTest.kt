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
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditorViewModelTest {
    private lateinit var repository: InsulinRepository
    private lateinit var viewModel: EditorViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = InsulinRepository.getInstance(context)
        viewModel = EditorViewModel(repository)
    }

    @Test
    fun setInsulin() = runBlocking {
        val insulin = insulin {
            uid = 12
            name = "FooBar"
        }

        repository.insert(insulin)

        val result = viewModel.runSetInsulin(insulin.uid)
        assertThat(result.uid).isEqualTo(insulin.uid)
        assertThat(result).isEqualTo(insulin)
    }

    @Test
    fun delete() = runBlocking {
        val insulin = insulin {
            uid = 12
            name = "FooBar"
        }

        repository.insert(insulin)

        viewModel.insulin = insulin
        viewModel.runDelete()

        assertThat(repository.getById(insulin.uid).uid).isNotEqualTo(insulin.uid)
    }

    @Test
    fun save() = runBlocking {
        viewModel.runSetInsulin(-1)

        val testUid = 12L
        assertThat(repository.getById(testUid).uid).isNotEqualTo(testUid)

        viewModel.insulin.apply {
            uid = testUid
            name = "BarFoo"
            timeFrame = TimeFrame.LATE_MORNING
            isBasal = true
        }

        viewModel.runSave()

        assertThat(repository.getById(testUid).uid).isEqualTo(testUid)
    }
}