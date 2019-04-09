/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.data.repositories

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import it.diab.core.data.AppDatabase
import it.diab.core.util.extensions.hba1c
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class Hba1cRepositoryTest {

    private lateinit var repository: Hba1cRepository

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = Hba1cRepository.getInstance(context)
    }

    @Test
    fun insert() = runBlocking {
        val item = hba1c {
            uid = 12
            value = 12.3f
        }

        repository.insert(item)
        val inRepo = repository.getById(item.uid)

        assertThat(inRepo).isEqualTo(item)
        assertThat(inRepo?.uid ?: -1).isEqualTo(item.uid)
    }

    @Test
    fun delete() = runBlocking {
        val item = hba1c {
            uid = 12
            value = 12.3f
        }

        repository.insert(item)
        assertThat(repository.getById(item.uid)).isNotNull()

        repository.delete(item)
        assertThat(repository.getById(item.uid)).isNotEqualTo(item.uid)
    }

    private fun Hba1cRepository.getById(uid: Long) = getAllItems().firstOrNull { it.uid == uid }
}