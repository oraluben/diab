package it.diab.glucose.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.db.runOnDbThread

class GlucoseListViewModel(owner: Application) : AndroidViewModel(owner) {
    val pagedList: LiveData<PagedList<Glucose>>

    private val db = AppDatabase.getInstance(owner)

    init {
        pagedList = LivePagedListBuilder(db.glucose().pagedList, 20).build()
    }

    fun getInsulin(id: Long) = runOnDbThread<Insulin> {
        val results = db.insulin().getById(id)
        if (results.isEmpty())
            Insulin()
        else
            results[0]
    }
}
