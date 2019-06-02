/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.data.repositories

import android.content.Context
import it.diab.core.util.SingletonHolder
import it.diab.data.AppDatabase
import it.diab.data.dao.GlucoseDao
import it.diab.data.entities.Glucose
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class GlucoseRepository private constructor(private val dao: GlucoseDao) : BaseRepository() {

    val all = dao.all

    val pagedList = dao.pagedList

    val last = dao.last

    suspend fun getById(uid: Long) = withContext(IO) {
        dao.getById(uid).firstOrNull() ?: Glucose()
    }

    suspend fun getAllItems() = withContext(IO) {
        dao.getAllItems()
    }

    suspend fun getInDateRange(start: Long, end: Long) = withContext(IO) {
        dao.getInDateRange(start, end)
    }

    suspend fun insert(glucose: Glucose) = withContext(IO) {
        dao.insert(glucose)
    }

    suspend fun delete(glucose: Glucose) = withContext(IO) {
        dao.delete(glucose)
    }

    companion object : SingletonHolder<GlucoseRepository, Context>({
        GlucoseRepository(AppDatabase.getInstance(it).glucose())
    })
}