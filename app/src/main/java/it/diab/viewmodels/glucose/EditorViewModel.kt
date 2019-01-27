/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import androidx.annotation.VisibleForTesting
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.Insulin
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.insulin.ml.PluginManager
import it.diab.core.viewmodels.ScopedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EditorViewModel internal constructor(
    private val glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ScopedViewModel() {
    var glucose = Glucose()
        private set

    var isEditMode = false

    lateinit var insulins: List<Insulin>

    private lateinit var basalInsulins: List<Insulin>
    private lateinit var pluginManager: PluginManager
    private var errorStatus = 0

    fun prepare(pManager: PluginManager, block: () -> Unit) {
        viewModelScope.launch {
            runPrepare(this, pManager)
            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun setGlucose(uid: Long, block: () -> Unit) {
        viewModelScope.launch {
            runSetGlucose(uid)
            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun save() {
        viewModelScope.launch { runSave() }
    }

    fun getInsulin(uid: Long) = insulins.firstOrNull { it.uid == uid } ?: Insulin()

    fun hasPotentialBasal() = basalInsulins.any { it.timeFrame == glucose.timeFrame }

    fun getInsulinByTimeFrame() =
        insulins.firstOrNull { it.timeFrame == glucose.timeFrame } ?: Insulin()

    fun getInsulinSuggestion(block: (Float) -> Unit) {
        if (pluginManager.isInstalled()) {
            viewModelScope.launch { pluginManager.fetchSuggestion(glucose, block) }
        } else {
            GlobalScope.launch(Dispatchers.Main) { block(PluginManager.NO_MODEL) }
        }
    }

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, block: () -> Unit) {
        viewModelScope.launch {
            runApplySuggestion(value, insulin)
            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun setError(value: Int) {
        errorStatus = errorStatus or value
    }

    fun clearError(value: Int) {
        errorStatus = errorStatus and value.inv()
    }

    fun hasError(value: Int) = errorStatus and value != 0

    fun hasErrors() = errorStatus != 0

    @VisibleForTesting
    suspend fun runPrepare(scope: CoroutineScope, pManager: PluginManager) {
        val defAll = scope.async { insulinRepository.getInsulins() }
        val defBasal = scope.async { insulinRepository.getBasals() }

        pluginManager = pManager
        insulins = defAll.await()
        basalInsulins = defBasal.await()
    }

    @VisibleForTesting
    fun runSetGlucose(uid: Long) {
        glucose = glucoseRepository.getById(uid)
    }

    @VisibleForTesting
    suspend fun runSave() {
        glucoseRepository.insert(glucose)
    }

    @VisibleForTesting
    suspend fun runApplySuggestion(value: Float, insulin: Insulin) {
        glucose.insulinId0 = insulin.uid
        glucose.insulinValue0 = value
        glucoseRepository.insert(glucose)
    }

    companion object {
        const val ERROR_VALUE = 1
        const val ERROR_DATE = 1 shl 1
    }
}