/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.list

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.ScopedViewModel
import it.diab.util.extensions.getHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class GlucoseListViewModel(owner: Application) : ScopedViewModel(owner) {
    val pagedList: LiveData<PagedList<Glucose>>

    private val db = AppDatabase.getInstance(owner)
    private var insulins: List<Insulin> = ArrayList()

    init {
        pagedList = LivePagedListBuilder(db.glucose().pagedList, 20).build()
    }

    fun prepare(block: () -> Unit) {
        viewModelScope.launch {
            insulins = db.insulin().allStatic

            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun getInsulin(id: Long): Insulin =
        insulins.firstOrNull { it.uid == id } ?: Insulin()

    fun setHeader(
        resources: Resources,
        date: Date,
        format: SimpleDateFormat,
        block: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            val pairDef = async { date.getHeader(resources, Date(), format) }

            val header = pairDef.await()
            GlobalScope.launch(coroutineContext) { block(header.first, header.second) }
        }
    }
}
