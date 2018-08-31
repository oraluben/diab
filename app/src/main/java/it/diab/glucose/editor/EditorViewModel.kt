package it.diab.glucose.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.db.runOnDbThread
import it.diab.util.DateUtils
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.firstIf
import it.diab.util.timeFrame.TimeFrame
import java.util.Date

class EditorViewModel(owner: Application) : AndroidViewModel(owner) {
    var glucose = Glucose()

    private val db: AppDatabase = AppDatabase.getInstance(owner)

    val insulins: List<Insulin>
        get() = runOnDbThread<List<Insulin>> { db.insulin().allStatic }

    val basalInsulins: List <Insulin>
        get() = runOnDbThread<List<Insulin>> { db.insulin().basalInsulins }

    val previousWeek: List<Glucose>
        get() = runOnDbThread<List<Glucose>> {
            val initialDate = glucose.date.time
            val currentFrame = Date(initialDate).asTimeFrame()

            db.glucose().getInDateRangeWithTimeFrame(initialDate - DateUtils.WEEK,
                    initialDate, currentFrame.toInt())
        }

    fun setGlucose(uid: Long) {
        glucose = runOnDbThread<Glucose> { db.glucose().getById(uid).firstIf({ uid >= 0 }, Glucose()) }
    }

    fun save() {
        runOnDbThread { db.glucose().insert(glucose) }
    }

    fun getInsulin(uid: Long) = runOnDbThread<Insulin> {
        db.insulin().getById(uid).firstIf({ uid >= 0 }, Insulin())
    }

    fun hasPotentialBasal(glucose: Glucose) = runOnDbThread<Boolean> {
        db.insulin().getByTimeFrame(1, glucose.timeFrame.toInt()).isNotEmpty()
    }

    fun getInsulinByTimeFrame(timeFrame: TimeFrame) = runOnDbThread<Insulin> {
        val insulins = db.insulin().getByTimeFrame(0, timeFrame.toInt())

        insulins.firstIf({ it.isNotEmpty() }, Insulin())
    }

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, onPostExecute: () -> Unit) {
        runOnDbThread({
            glucose.insulinId0 = insulin.uid
            glucose.insulinValue0 = value
            db.glucose().insert(glucose)
        }, onPostExecute)
    }
}