/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.data.repositories

import android.content.Context
import androidx.annotation.WorkerThread
import it.diab.core.data.AppDatabase
import it.diab.core.data.dao.Hba1cDao
import it.diab.core.data.entities.Hba1c
import it.diab.core.util.SingletonHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class Hba1cRepository private constructor(private val hba1cDao: Hba1cDao) {

    val all = hba1cDao.all

    @WorkerThread
    fun getAllItems() = hba1cDao.getAllItems()

    suspend fun insert(item: Hba1c) = withContext(IO) { hba1cDao.insert(item) }

    suspend fun delete(item: Hba1c) = withContext(IO) { hba1cDao.delete(item) }

    companion object : SingletonHolder<Hba1cRepository, Context>({
        Hba1cRepository(AppDatabase.getInstance(it).hba1c())
    })
}