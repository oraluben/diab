package it.diab.insulin.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin
import it.diab.db.runOnDbThread

class EditorViewModel(owner: Application) : AndroidViewModel(owner) {
    var insulin: Insulin = Insulin()
    private val db = AppDatabase.getInstance(owner)

    fun setInsulin(uid: Long) {
        if (uid < 0) {
            insulin = Insulin()
            return
        }

        insulin = runOnDbThread<Insulin> { db.insulin().getById(uid)[0] }
    }

    fun delete(insulin: Insulin) {
        runOnDbThread { db.insulin().delete(insulin) }
    }

    fun save() {
        runOnDbThread { db.insulin().insert(insulin) }
    }
}
