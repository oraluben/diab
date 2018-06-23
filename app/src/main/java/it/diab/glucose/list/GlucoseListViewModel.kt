package it.diab.glucose.list

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin

class GlucoseListViewModel(owner: Application) : AndroidViewModel(owner) {
    val pagedList: LiveData<PagedList<Glucose>>

    private val db = AppDatabase.getInstance(owner)

    init {
        pagedList = LivePagedListBuilder(db.glucose().pagedList, 20).build()
    }

    fun getInsulin(id: Long): Insulin {
        val task = FetchInsulinTask(db)
        task.execute(id)
        return task.get()
    }

    private class FetchInsulinTask(db: AppDatabase) : DatabaseTask<Long, Insulin>(db) {

        override fun doInBackground(vararg params: Long?): Insulin {
            val list = mDatabase.insulin().getById(params[0] ?: -1)

            return if (list.isEmpty()) Insulin() else list[0]
        }
    }
}
