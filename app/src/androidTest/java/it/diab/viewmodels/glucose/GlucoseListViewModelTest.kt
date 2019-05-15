/*
 * Copyright (c) 2019 Bevilacqua Joey
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
import it.diab.data.AppDatabase
import it.diab.data.extensions.insulin
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GlucoseListViewModelTest {
    private lateinit var viewModel: GlucoseListViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Insert test data
        AppDatabase.getInstance(context).insulin()
            .insert(*TEST_DATA)

        viewModel = GlucoseListViewModel(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        ).apply {
            runPrepare()
        }
    }

    @Test
    fun getInsulin() {
        val test = TEST_DATA[0]

        assertEquals(test, viewModel.getInsulin(test.uid))
    }

    companion object {
        private val TEST_DATA = arrayOf(
            insulin {
                uid = 73
                name = "Foo"
            },
            insulin {
                uid = 42
                name = "Oof"
            }
        )
    }
}