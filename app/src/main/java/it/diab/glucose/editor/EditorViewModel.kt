package it.diab.glucose.editor

import android.app.Application
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.DateUtils
import it.diab.util.ScopedViewModel
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.firstIf
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Date

class EditorViewModel(owner: Application) : ScopedViewModel(owner) {
    var glucose = Glucose()

    private val db: AppDatabase = AppDatabase.getInstance(owner)

    lateinit var insulins: List<Insulin>
    lateinit var basalInsulins: List <Insulin>
    lateinit var previousWeek: List<Glucose>

    fun prepare(block: () -> Unit) {
        viewModelScope.launch {
            val defAll = async { db.insulin().allStatic }

            val defBasal = async { db.insulin().basalInsulins }

            val defPrevious = async {
                val initialDate = glucose.date.time
                val currentFrame = Date(initialDate).asTimeFrame()

                db.glucose().getInDateRangeWithTimeFrame(initialDate - DateUtils.WEEK,
                    initialDate, currentFrame.toInt())
            }

            insulins = defAll.await()
            basalInsulins = defBasal.await()
            previousWeek = defPrevious.await()

            GlobalScope.launch(coroutineContext) { block() }
        }
    }

    fun setGlucose(uid: Long) {
        viewModelScope.launch { glucose = db.glucose().getById(uid).firstIf({ uid >= 0 }, Glucose()) }
    }

    fun save() {
        viewModelScope.launch { db.glucose().insert(glucose) }
    }

    fun getInsulin(uid: Long) =
        insulins.firstOrNull { it.uid == uid } ?: Insulin()

    fun hasPotentialBasal() =
        basalInsulins.any { it.timeFrame == glucose.timeFrame }

    fun getInsulinByTimeFrame() =
        insulins.firstOrNull { it.timeFrame == glucose.timeFrame } ?: Insulin()

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, onPostExecute: () -> Unit) {
        viewModelScope.launch {
            glucose.insulinId0 = insulin.uid
            glucose.insulinValue0 = value
            db.glucose().insert(glucose)

            GlobalScope.launch(coroutineContext) { onPostExecute() }
        }
    }
}