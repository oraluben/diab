package it.diab.insulin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin

class InsulinViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<PagedList<Insulin>>
    private val db: AppDatabase = AppDatabase.getInstance(owner)

    init {
        list = LivePagedListBuilder(db.insulin().all, 5).build()
    }
}
