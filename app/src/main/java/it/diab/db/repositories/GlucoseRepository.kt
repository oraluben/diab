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
import it.diab.db.dao.GlucoseDao
import it.diab.db.entities.Glucose
import it.diab.util.SingletonHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class GlucoseRepository private constructor(private val dao: GlucoseDao) {

    val all = dao.all

    val pagedList = dao.pagedList

    val last = dao.last

    @WorkerThread
    fun getById(uid: Long) = dao.getById(uid).firstOrNull() ?: Glucose()

    @WorkerThread
    fun getAllItems() = dao.getAllItems()

    @WorkerThread
    fun getInDateRange(minTime: Long, maxTime: Long) =
        dao.getInDateRange(minTime, maxTime)

    suspend fun insert(glucose: Glucose) = withContext(IO) { dao.insert(glucose) }

    suspend fun delete(glucose: Glucose) = withContext(IO) { dao.delete(glucose) }

    companion object : SingletonHolder<GlucoseRepository, Context>({
        GlucoseRepository(AppDatabase.getInstance(it).glucose())
    })
}