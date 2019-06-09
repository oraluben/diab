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
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.glucose.ui.models.InsulinDialogUiModel
import kotlinx.coroutines.launch

class InsulinDialogViewModel internal constructor(
    private val glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ViewModel() {

    private lateinit var insulins: List<Insulin>
    private lateinit var glucose: Glucose

    fun prepare(uid: Long, wantBasal: Boolean, block: (InsulinDialogUiModel) -> Unit) {
        viewModelScope.launch {
            runPrepare(uid, wantBasal)
            block(
                InsulinDialogUiModel(
                    glucose.timeFrame,
                    if (wantBasal) glucose.insulinValue1 else glucose.insulinValue0,
                    if (wantBasal) glucose.insulinId1 else glucose.insulinId0,
                    insulins
                )
            )
        }
    }

    fun setInsulin(index: Int, value: Float) {
        viewModelScope.launch {
            runSetInsulin(index, value)
        }
    }

    fun setBasal(index: Int, value: Float) {
        viewModelScope.launch {
            runSetBasal(index, value)
        }
    }

    fun removeInsulin() {
        viewModelScope.launch {
            runRemoveInsulin()
        }
    }

    fun removeBasal() {
        viewModelScope.launch {
            runRemoveBasal()
        }
    }

    fun hasNothing() = insulins.isEmpty()

    @VisibleForTesting
    suspend fun runPrepare(uid: Long, wantBasal: Boolean) {
        glucose = glucoseRepository.getById(uid)
        insulins = insulinRepository.getInsulins().filter { it.isBasal == wantBasal }
    }

    @VisibleForTesting
    suspend fun runSetInsulin(index: Int, value: Float) {
        glucoseRepository.insert(glucose.apply {
            insulinId0 = insulins[index].uid
            insulinValue0 = value
        })
    }

    @VisibleForTesting
    suspend fun runSetBasal(index: Int, value: Float) {
        glucoseRepository.insert(glucose.apply {
            insulinId1 = insulins[index].uid
            insulinValue1 = value
        })
    }

    @VisibleForTesting
    suspend fun runRemoveInsulin() {
        glucoseRepository.insert(glucose.apply {
            insulinId0 = 0L
            insulinValue0 = 0f
        })
    }

    @VisibleForTesting
    suspend fun runRemoveBasal() {
        glucoseRepository.insert(glucose.apply {
            insulinId1 = 0L
            insulinValue1 = 0f
        })
    }
}