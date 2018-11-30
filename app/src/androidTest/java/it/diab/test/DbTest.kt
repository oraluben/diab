/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
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