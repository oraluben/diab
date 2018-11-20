package it.diab.insulin.editor

import android.app.Application
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin
import it.diab.util.ScopedViewModel
import kotlinx.coroutines.launch

class EditorViewModel(owner: Application) : ScopedViewModel(owner) {
    var insulin: Insulin = Insulin()
    private val db = AppDatabase.getInstance(owner)

    fun setInsulin(uid: Long) {
        launch { insulin = db.insulin().getById(uid).firstOrNull() ?: Insulin() }
    }

    fun delete() {
        launch { db.insulin().delete(insulin) }
    }

    fun save() {
        launch { db.insulin().insert(insulin) }
    }
}
