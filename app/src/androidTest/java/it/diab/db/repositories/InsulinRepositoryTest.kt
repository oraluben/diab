package it.diab.db.repositories

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import it.diab.db.AppDatabase
import it.diab.util.extensions.insulin
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class InsulinRepositoryTest {

    private lateinit var repository: InsulinRepository

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = InsulinRepository.getInstance(context)
    }

    @Test
    fun getBasals() = runBlocking {
        // Clear old test data
        repository.getInsulins().forEach { repository.delete(it) }

        val data = Array(6) {
            insulin {
                isBasal = it % 2 == 0
                name = "Insulin $it"
            }
        }

        data.forEach { repository.insert(it) }

        assertThat(repository.getBasals().size).isEqualTo(data.size / 2)
    }

    @Test
    fun insert() = runBlocking {
        val insulin = insulin {
            uid = 12
            name = "FooBar"
        }

        repository.insert(insulin)
        repository.getById(insulin.uid).run {
            assertThat(uid).isEqualTo(insulin.uid)
            assertThat(this).isEqualTo(insulin)
        }
    }

    @Test
    fun delete() = runBlocking {
        val insulin = insulin {
            uid = 12
            name = "FooBar"
        }

        repository.insert(insulin)
        assertThat(repository.getById(insulin.uid).uid).isEqualTo(insulin.uid)

        repository.delete(insulin)
        assertThat(repository.getById(insulin.uid).uid).isNotEqualTo(insulin.uid)
    }
}