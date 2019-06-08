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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.diab.data.entities.Glucose
import it.diab.data.entities.Insulin
import it.diab.data.entities.TimeFrame
import it.diab.data.extensions.asTimeFrame
import it.diab.data.plugin.PluginManager
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Date

class EditorViewModel internal constructor(
    private val glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ViewModel() {

    private val _uid = MutableLiveData<Long>()
    private var _glucose = Transformations.switchMap(_uid) { uid ->
        glucoseRepository.getByIdLive(uid)
    }
    val glucose = Transformations.map(_glucose) { glucose ->
        glucose?.firstOrNull() ?: Glucose()
    }

    var isEditMode = false
    var date = Date()
    var value = 0
    var eatLevel = 1

    private lateinit var insulins: List<Insulin>
    private lateinit var basalInsulins: List<Insulin>
    private lateinit var pluginManager: PluginManager

    fun prepare(uid: Long, pManager: PluginManager, block: () -> Unit) {
        viewModelScope.launch {
            runPrepare(uid, pManager)
            block()
        }
    }

    fun save() {
        viewModelScope.launch { runSave() }
    }

    fun getInsulin(uid: Long) = insulins.firstOrNull { it.uid == uid } ?: Insulin()

    fun hasPotentialBasal(): Boolean {
        val targetTimeFrame = glucose.value?.timeFrame ?: TimeFrame.EXTRA
        return basalInsulins.any { it.timeFrame == targetTimeFrame }
    }

    fun getInsulinByTimeFrame(): Insulin {
        val targetTimeFrame = glucose.value?.timeFrame ?: TimeFrame.EXTRA
        return insulins.firstOrNull { it.timeFrame == targetTimeFrame } ?: Insulin()
    }

    fun getInsulinSuggestion(block: (Float) -> Unit) {
        val target = glucose.value ?: Glucose()
        if (pluginManager.isInstalled()) {
            viewModelScope.launch { pluginManager.fetchSuggestion(target, block) }
        } else {
            block(PluginManager.NO_MODEL)
        }
    }

    fun applyInsulinSuggestion(value: Float, insulin: Insulin, block: () -> Unit) {
        viewModelScope.launch {
            runApplySuggestion(value, insulin)
            block()
        }
    }

    @VisibleForTesting
    suspend fun runPrepare(uid: Long, pManager: PluginManager) {
        val defAll = viewModelScope.async(IO) { insulinRepository.getInsulins() }
        val defBasal = viewModelScope.async(IO) { insulinRepository.getBasals() }

        _uid.value = uid

        val staticGlucose = glucoseRepository.getById(uid)
        isEditMode = uid <= 0L
        value = staticGlucose.value
        date = staticGlucose.date
        eatLevel = staticGlucose.eatLevel

        pluginManager = pManager
        insulins = defAll.await()
        basalInsulins = defBasal.await()
    }

    @VisibleForTesting
    suspend fun runSave() {
        val toSave = glucose.value ?: return
        toSave.value = value
        toSave.date = date
        toSave.timeFrame = date.asTimeFrame()
        toSave.eatLevel = eatLevel
        glucoseRepository.insert(toSave)
    }

    @VisibleForTesting
    suspend fun runApplySuggestion(value: Float, insulin: Insulin) {
        val toSave = glucose.value ?: return

        toSave.insulinId0 = insulin.uid
        toSave.insulinValue0 = value
        glucoseRepository.insert(toSave)
    }
}