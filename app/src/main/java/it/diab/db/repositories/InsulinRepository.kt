/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.db.repositories

import android.content.Context
import androidx.annotation.WorkerThread
import it.diab.db.AppDatabase
import it.diab.db.dao.InsulinDao
import it.diab.db.entities.Insulin
import it.diab.util.SingletonHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class InsulinRepository private constructor(private val dao: InsulinDao) {

    val all = dao.all

    @WorkerThread
    fun getInsulins() = dao.getInsulins()

    @WorkerThread
    fun getBasals() = dao.getBasals()

    @WorkerThread
    fun getById(uid: Long) = dao.getById(uid).firstOrNull() ?: Insulin()

    suspend fun insert(insulin: Insulin) = withContext(IO) { dao.insert(insulin) }

    suspend fun delete(insulin: Insulin) = withContext(IO) { dao.delete(insulin) }

    companion object : SingletonHolder<InsulinRepository, Context>({
        InsulinRepository(AppDatabase.getInstance(it).insulin())
    })
}