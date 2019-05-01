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
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.data.extensions.insulin
import it.diab.util.extensions.get
import it.diab.util.extensions.getWeekDay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date

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
            setDateStrings(TODAY, YESTERDAY, LAST_X)
        }
    }

    @Test
    fun getInsulin() {
        val test = TEST_DATA[0]

        assertEquals(test, viewModel.getInsulin(test.uid))
    }

    @Test
    fun setHeader() {
        val a = Date()
        val b = Date()[-1]
        val c = Date()[-5]
        val d = Date()[-10]

        val format = SimpleDateFormat("yyyy-MM-dd")

        assertEquals(TODAY, viewModel.runSetHeader(a, format))
        assertEquals(YESTERDAY, viewModel.runSetHeader(b, format))
        assertEquals(LAST_X.format(c.getWeekDay()), viewModel.runSetHeader(c, format))
        assertEquals(format.format(d), viewModel.runSetHeader(d, format))
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

        private const val TODAY = "today"
        private const val YESTERDAY = "yesterday"
        private const val LAST_X = "last %1\$s"
    }
}