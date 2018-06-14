package it.diab.insulin

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin

class InsulinViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<PagedList<Insulin>>
    private val mDatabase: AppDatabase = AppDatabase.getInstance(owner)

    init {
        list = LivePagedListBuilder(mDatabase.insulin().all, 5).build()
    }
}
