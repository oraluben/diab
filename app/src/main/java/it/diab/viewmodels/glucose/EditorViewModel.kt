package it.diab.viewmodels.glucose

import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.insulin.ml.PluginManager
import it.diab.util.DateUtils
import it.diab.viewmodels.ScopedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditorViewModel internal constructor(
        private val glucoseRepository: GlucoseRepository,
        private val insulinRepository: InsulinRepository
): ScopedViewModel() {
    var glucose = Glucose()
        private set

    lateinit var insulins: List<Insulin>
    lateinit var basalInsulins: List<Insulin>

    lateinit var previousWeek: List<Glucose>

    private lateinit var pluginManager: PluginManager

    fun prepare(pManager: PluginManager, block: () -> Unit) {
        viewModelScope.launch {
            val defAll = async { insulinRepository.getInsulins() }
            val defBasal = async { insulinRepository.getBasals() }
            val defPrevious = async {
                val time = glucose.date.time
                val timeFrame = glucose.timeFrame

                glucoseRepository.getInDateRangeWithTimeFrame(
                        time - DateUtils.WEEK,
                        time,
                        timeFrame.toInt())
            }

            pluginManager = pManager
            insulins = defAll.await()
            basalInsulins = defBasal.await()
            previousWeek = defPrevious.await()

            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun setGlucose(uid: Long, block: () -> Unit) {
        viewModelScope.launch {
            glucose = glucoseRepository.getById(uid)

            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun save() {
        viewModelScope.launch { glucoseRepository.insert(glucose) }
    }

    fun getInsulin(uid: Long) = insulins.firstOrNull { it.uid == uid } ?: Insulin()

    fun hasPotentialBasal() = basalInsulins.any { it.timeFrame == glucose.timeFrame }

    fun getInsulinByTimeFrame() =
            insulins.firstOrNull { it.timeFrame == glucose.timeFrame } ?: Insulin()

    fun getInsulinSuggestion(block: (Float) -> Unit) {
        viewModelScope.launch {
            if (pluginManager.isInstalled()) {
                pluginManager.fetchSuggestion(glucose, block)
            } else {
                GlobalScope.launch(Dispatchers.Main) { block(PluginManager.NO_MODEL) }
            }
        }
    }

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, block: () -> Unit) {
        viewModelScope.launch {
            glucose.insulinId0 = insulin.uid
            glucose.insulinValue0 = value
            glucoseRepository.insert(glucose)

            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }
}