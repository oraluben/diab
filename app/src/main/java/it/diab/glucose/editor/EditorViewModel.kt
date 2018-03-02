package it.diab.glucose.editor

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.DateUtils
import it.diab.util.extensions.asTimeFrame
import java.util.*
import java.util.concurrent.ExecutionException

class EditorViewModel(owner: Application) : AndroidViewModel(owner) {
    internal var glucose = Glucose()
    private val mDatabase: AppDatabase = AppDatabase.getInstance(owner)

    internal val insulins: List<Insulin>
        get() {
            var list = emptyList<Insulin>()
            val task = GetInsulinListTask(mDatabase)
            task.execute()
            try {
                list = task.get()
            } catch (ignored: InterruptedException) {
            } catch (ignored: ExecutionException) {
            }

            return list
        }

    internal val basalInsulins: List<Insulin>
        get() {
            var list = emptyList<Insulin>()
            val task = GetBasalInsulinListTask(mDatabase)
            task.execute()
            try {
                list = task.get()
            } catch (ignored: InterruptedException) {
            } catch (ignored: ExecutionException) {
            }

            return list
        }

    internal val previousWeek: List<Glucose>
        get() {
            val time = glucose.date.time

            val task = GetGlucoseWeekListTask(mDatabase)
            task.execute(time)
            return try {
                task.get()
            } catch (e: InterruptedException) {
                emptyList()
            } catch (e: ExecutionException) {
                emptyList()
            }
        }

    internal fun setGlucose(uid: Long) {
        if (uid < 0) {
            glucose = Glucose()
            return
        }

        val task = GetGlucoseTask(mDatabase)
        task.execute(uid)
        glucose = try {
            task.get()
        } catch (e: InterruptedException) {
            Glucose()
        } catch (e: ExecutionException) {
            Glucose()
        }
    }

    internal fun save() {
        SaveTask(mDatabase).execute(glucose)
    }

    internal fun getInsulin(id: Long): Insulin {
        val task = GetInsulinTask(mDatabase)
        task.execute(id)
        return try {
            task.get()
        } catch (e: InterruptedException) {
            Insulin()
        } catch (e: ExecutionException) {
            Insulin()
        }
    }

    class GetGlucoseTask(db: AppDatabase) : DatabaseTask<Long, Glucose>(db) {

        override fun doInBackground(vararg params: Long?): Glucose {
            return try {
                mDatabase.glucose().getById(params[0] ?: -1)[0]
            } catch (e: IndexOutOfBoundsException) {
                Glucose()
            }

        }
    }

    class GetGlucoseWeekListTask(db: AppDatabase) : DatabaseTask<Long, List<Glucose>>(db) {

        override fun doInBackground(vararg params: Long?): List<Glucose> {
            val initialDate = params[0] ?: System.currentTimeMillis()
            val list = mDatabase.glucose()
                    .getInDateRange(initialDate - DateUtils.WEEK, initialDate)
                    .toMutableList()

            val currentFrame = Date(initialDate).asTimeFrame().toInt()
            list.removeIf { it -> it.date.asTimeFrame().toInt() != currentFrame }
            return list
        }
    }

    class GetInsulinListTask(db: AppDatabase) : DatabaseTask<Unit, List<Insulin>>(db) {

        public override fun doInBackground(vararg params: Unit): List<Insulin> {
            return mDatabase.insulin().allStatic
        }
    }

    class GetBasalInsulinListTask(db: AppDatabase) : DatabaseTask<Unit, List<Insulin>>(db) {
        override fun doInBackground(vararg params: Unit?): List<Insulin> {
            return mDatabase.insulin().basalInsulins
        }
    }

    class GetInsulinTask(db: AppDatabase) : DatabaseTask<Long, Insulin>(db) {

        override fun doInBackground(vararg params: Long?): Insulin {
            val list = mDatabase.insulin().getById(params[0] ?: -1)
            return if (list.isNotEmpty()) list[0] else Insulin()
        }
    }

    class SaveTask(db: AppDatabase) : DatabaseTask<Glucose, Unit>(db) {

        public override fun doInBackground(vararg params: Glucose) {
            val glucose = params[0]
            mDatabase.glucose().insert(glucose)
        }
    }
}