package it.diab.insulin

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin

class InsulinViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<List<Insulin>>
    private val mDatabase: AppDatabase = AppDatabase.getInstance(owner)

    init {
        list = mDatabase.insulin().all
    }
}
