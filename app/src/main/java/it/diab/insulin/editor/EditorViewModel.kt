package it.diab.insulin.editor

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Insulin
import java.util.concurrent.ExecutionException

class EditorViewModel(owner: Application) : AndroidViewModel(owner) {
    var insulin: Insulin = Insulin()
    private val mDatabase = AppDatabase.getInstance(owner)

    fun setInsulin(uid: Long) {
        if (uid < 0) {
            insulin = Insulin()
        }

        val task = FetchTask(mDatabase)
        task.execute(uid)
        insulin = try {
            task.get()
        } catch (e: InterruptedException) {
            Insulin()
        } catch (e: ExecutionException) {
            Insulin()
        }

    }

    fun delete(insulin: Insulin) {
        DeleteTask(mDatabase).execute(insulin)
    }

    fun save() {
        SaveTask(mDatabase).execute(insulin)
    }

    private class FetchTask(db: AppDatabase) : DatabaseTask<Long, Insulin>(db) {

        override fun doInBackground(vararg params: Long?): Insulin {
            return try {
                mDatabase.insulin().getById(params[0] ?: -1)[0]
            } catch (e: IndexOutOfBoundsException) {
                Insulin()
            }
        }
    }

    private class DeleteTask(db: AppDatabase):
            DatabaseTask<Insulin, Unit>(db) {

        override fun doInBackground(vararg params: Insulin?) {
            val arg = params[0] ?: return
            mDatabase.insulin().delete(arg)
        }
    }

    private class SaveTask(db: AppDatabase) : DatabaseTask<Insulin, Unit>(db) {

        public override fun doInBackground(vararg params: Insulin?) {
            val arg = params[0] ?: return
            mDatabase.insulin().insert(arg)
        }
    }
} 
