/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.data.repositories

import androidx.test.platform.app.InstrumentationRegistry
import it.diab.data.AppDatabase
import it.diab.core.util.DateUtils
import it.diab.data.extensions.glucose
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class GlucoseRepositoryTest {

    private lateinit var repository: GlucoseRepository

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = GlucoseRepository.getInstance(context)
    }

    @Test
    fun getInDateRange() = runBlocking {
        val daysRange = 3
        val baseValue = 42

        val end = System.currentTimeMillis()
        val start = end - (DateUtils.DAY * daysRange)

        // Clear old test data
        repository.getInDateRange(start, end).forEach {
            repository.delete(it)
        }

        val data = Array(7) {
            glucose {
                value = baseValue * (it + 1)
                date = Date(end - (DateUtils.DAY * it))
            }
        }

        data.forEach { repository.insert(it) }

        val list = repository.getInDateRange(start, end)
        assertEquals(daysRange + 1, list.size)
        assertEquals(baseValue * 2, list[1].value)
    }

    @Test
    fun insert() = runBlocking {
        val glucose = glucose {
            uid = 12
            value = 120
        }

        repository.insert(glucose)
        repository.getById(glucose.uid).run {
            assertEquals(glucose.uid, uid)
            assertEquals(glucose, this)
        }
    }

    @Test
    fun delete() = runBlocking {
        val glucose = glucose {
            uid = 12
            value = 120
        }

        repository.insert(glucose)
        assertEquals(glucose.uid, repository.getById(glucose.uid).uid)

        repository.delete(glucose)
        assertNotEquals(glucose.uid, repository.getById(glucose.uid).uid)
    }
}