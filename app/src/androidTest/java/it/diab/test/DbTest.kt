package it.diab.test

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import it.diab.db.AppDatabase
import org.junit.Before

abstract class DbTest {
    protected lateinit var db: AppDatabase

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    open fun setup() {
        AppDatabase.TEST_MODE = true

        db = AppDatabase.getInstance(context)
    }

}