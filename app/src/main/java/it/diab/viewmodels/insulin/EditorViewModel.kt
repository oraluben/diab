/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.insulin

import androidx.annotation.VisibleForTesting
import it.diab.core.data.entities.Insulin
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.viewmodels.ScopedViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditorViewModel internal constructor(
    private val insulinRepository: InsulinRepository
) : ScopedViewModel() {

    var insulin = Insulin()

    fun setInsulin(uid: Long, block: (Insulin) -> Unit) {
        viewModelScope.launch {
            insulin = runSetInsulin(uid)

            GlobalScope.launch { block(insulin) }
        }
    }

    fun delete() {
        viewModelScope.launch { runDelete() }
    }

    fun save() {
        viewModelScope.launch { runSave() }
    }

    @VisibleForTesting
    fun runSetInsulin(uid: Long): Insulin {
        return insulinRepository.getById(uid)
    }

    @VisibleForTesting
    suspend fun runDelete() {
        insulinRepository.delete(insulin)
    }

    @VisibleForTesting
    suspend fun runSave() {
        insulinRepository.insert(insulin)
    }
}