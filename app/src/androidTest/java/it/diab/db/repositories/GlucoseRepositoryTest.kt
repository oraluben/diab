package it.diab.db.repositories

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import it.diab.db.AppDatabase
import it.diab.util.DateUtils
import it.diab.util.extensions.glucose
import kotlinx.coroutines.runBlocking
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
        assertThat(list.size).isEqualTo(daysRange + 1)
        assertThat(list[1].value).isEqualTo(baseValue * 2)
    }

    @Test
    fun insert() = runBlocking {
        val glucose = glucose {
            uid = 12
            value = 120
        }

        repository.insert(glucose)
        repository.getById(glucose.uid).run {
            assertThat(uid).isEqualTo(glucose.uid)
            assertThat(this).isEqualTo(glucose)
        }
    }

    @Test
    fun delete() = runBlocking {
        val glucose = glucose {
            uid = 12
            value = 120
        }

        repository.insert(glucose)
        assertThat(repository.getById(glucose.uid).uid).isEqualTo(glucose.uid)

        repository.delete(glucose)
        assertThat(repository.getById(glucose.uid)).isNotEqualTo(glucose.uid)
    }
}