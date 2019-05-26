/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.diab.data.entities.Glucose
import it.diab.data.entities.Insulin
import it.diab.data.plugin.PluginManager
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel internal constructor(
    private val glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ViewModel() {
    var glucose = Glucose()
        private set

    var isEditMode = false

    lateinit var insulins: List<Insulin>

    private lateinit var basalInsulins: List<Insulin>
    private lateinit var pluginManager: PluginManager

    fun prepare(pManager: PluginManager, block: () -> Unit) {
        viewModelScope.launch {
            runPrepare(this, pManager)
            block()
        }
    }

    fun setGlucose(uid: Long, block: () -> Unit) {
        viewModelScope.launch {
            runSetGlucose(uid)
            block()
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
            block(PluginManager.NO_MODEL)
        }
    }

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, block: () -> Unit) {
        viewModelScope.launch {
            runApplySuggestion(value, insulin)
            withContext(Dispatchers.Main) { block() }
        }
    }

    @VisibleForTesting
    suspend fun runPrepare(scope: CoroutineScope, pManager: PluginManager) {
        val defAll = scope.async(IO) { insulinRepository.getInsulins() }
        val defBasal = scope.async(IO) { insulinRepository.getBasals() }

        pluginManager = pManager
        insulins = defAll.await()
        basalInsulins = defBasal.await()
    }

    @VisibleForTesting
    suspend fun runSetGlucose(uid: Long) = withContext(IO) {
        glucose = glucoseRepository.getById(uid)
    }

    @VisibleForTesting
    suspend fun runSave() = withContext(IO) {
        glucoseRepository.insert(glucose)
    }

    @VisibleForTesting
    suspend fun runApplySuggestion(value: Float, insulin: Insulin) = withContext(IO) {
        glucose.insulinId0 = insulin.uid
        glucose.insulinValue0 = value
        glucoseRepository.insert(glucose)
    }
}