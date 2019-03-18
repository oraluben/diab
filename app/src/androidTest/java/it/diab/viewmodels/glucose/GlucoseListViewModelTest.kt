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
import com.google.common.truth.Truth.assertThat
import it.diab.core.data.AppDatabase
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.util.extensions.insulin
import it.diab.util.extensions.get
import it.diab.util.extensions.getWeekDay
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

        assertThat(viewModel.getInsulin(test.uid))
            .isEqualTo(test)
    }

    @Test
    fun setHeader() {
        val a = Date()
        val b = Date()[-1]
        val c = Date()[-5]
        val d = Date()[-10]

        val format = SimpleDateFormat("yyyy-MM-dd")

        assertThat(viewModel.runSetHeader(a, format))
            .isEqualTo(TODAY)
        assertThat(viewModel.runSetHeader(b, format))
            .isEqualTo(YESTERDAY)
        assertThat(viewModel.runSetHeader(c, format))
            .isEqualTo(LAST_X.format(c.getWeekDay()))
        assertThat(viewModel.runSetHeader(d, format))
            .isEqualTo(format.format(d))
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