package it.diab.glucose.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.DateUtils
import it.diab.util.extensions.asTimeFrame
import it.diab.util.timeFrame.TimeFrame
import java.util.Date

class EditorViewModel(owner: Application) : AndroidViewModel(owner) {
    var glucose = Glucose()

    private val db: AppDatabase = AppDatabase.getInstance(owner)

    val insulins: List<Insulin>
        get() {
            val task = GetInsulinListTask(db)
            task.execute()
            return task.get()
        }

    val basalInsulins: List<Insulin>
        get() {
            val task = GetBasalInsulinListTask(db)
            task.execute()
            return task.get()
        }

    val previousWeek: List<Glucose>
        get() {
            val task = GetGlucoseWeekListTask(db)
            task.execute(glucose.date.time)
            return task.get()
        }

    fun setGlucose(uid: Long) {
        glucose = if (uid < 0)
            Glucose()
        else {
            val task = GetGlucoseTask(db)
            task.execute(uid)
            task.get()
        }
    }

    fun save() {
        SaveTask(db).execute(glucose)
    }

    fun getInsulin(id: Long): Insulin {
        val task = GetInsulinTask(db)
        task.execute(id)
        return task.get()
    }

    fun hasPotentialBasal(glucose: Glucose): Boolean {
        val task = HasPotentialBasalTask(db)
        task.execute(glucose.timeFrame)
        return task.get()
    }

    fun getInsulinByTimeFrame(timeFrame: TimeFrame): Insulin {
        val task = GetInsulinByTimeFrameTask(db)
        task.execute(timeFrame)
        return task.get()
    }

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, onPostExecute: () -> Unit) {
        ApplyInsulinSuggestionTask(db, glucose, insulin.uid, onPostExecute).execute(value)
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
            val currentFrame = Date(initialDate).asTimeFrame()

            return mDatabase.glucose().getInDateRangeWithTimeFrame(
                    initialDate - DateUtils.WEEK, initialDate, currentFrame.toInt())
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
            return if (list.isEmpty()) Insulin() else list[0]
        }
    }

    class GetInsulinByTimeFrameTask(db: AppDatabase) : DatabaseTask<TimeFrame, Insulin>(db) {

        override fun doInBackground(vararg params: TimeFrame?): Insulin {
            val timeFrame = params[0]?.toInt() ?: return Insulin()
            val insulins = mDatabase.insulin().getByTimeFrame(0, timeFrame)

            return if (insulins.isEmpty()) Insulin() else insulins[0]
        }
    }

    class HasPotentialBasalTask(db: AppDatabase) : DatabaseTask<TimeFrame, Boolean>(db) {

        override fun doInBackground(vararg params: TimeFrame?): Boolean {
            val timeFrame = params[0]?.toInt() ?: return false
            return mDatabase.insulin().getByTimeFrame(1, timeFrame).isNotEmpty()
        }
    }

    class ApplyInsulinSuggestionTask(db: AppDatabase,
                                     private val glucose: Glucose,
                                     private val insulinUid: Long,
                                     private val onPost: () -> Unit):
            DatabaseTask<Float, Unit>(db) {

        override fun doInBackground(vararg params: Float?) {
            val suggestion = params[0] ?: return

            glucose.insulinId0 = insulinUid
            glucose.insulinValue0 = suggestion
            mDatabase.glucose().insert(glucose)
        }

        override fun onPostExecute(result: Unit?) {
            onPost()
        }
    }

    class SaveTask(db: AppDatabase) : DatabaseTask<Glucose, Unit>(db) {

        public override fun doInBackground(vararg params: Glucose?) {
            val glucose = params[0] ?: return
            mDatabase.glucose().insert(glucose)
        }
    }
}