package it.diab.glucose.list

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.ScopedViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

            GlobalScope.launch(coroutineContext) { block() }
        }
    }

    fun getInsulin(id: Long): Insulin =
        insulins.firstOrNull { it.uid == id } ?: Insulin()
}
