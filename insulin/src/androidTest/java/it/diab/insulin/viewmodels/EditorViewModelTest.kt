/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import it.diab.data.AppDatabase
import it.diab.data.entities.TimeFrame
import it.diab.data.repositories.InsulinRepository
import it.diab.data.extensions.insulin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
        assertEquals(insulin.uid, result.uid)
        assertEquals(insulin, result)
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

        assertNotEquals(insulin.uid, repository.getById(insulin.uid).uid)
    }

    @Test
    fun save() = runBlocking {
        viewModel.runSetInsulin(-1)

        val testUid = 12L
        assertNotEquals(testUid, repository.getById(testUid).uid)

        viewModel.insulin.apply {
            uid = testUid
            name = "BarFoo"
            timeFrame = TimeFrame.LATE_MORNING
            isBasal = true
        }

        viewModel.runSave()

        assertEquals(testUid, repository.getById(testUid).uid)
    }
}