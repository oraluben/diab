/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.diab.data.entities.Insulin
import it.diab.data.repositories.InsulinRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel internal constructor(
    private val insulinRepository: InsulinRepository
) : ViewModel() {

    var insulin = Insulin()

    fun setInsulin(uid: Long, block: (Insulin) -> Unit) {
        viewModelScope.launch {
            insulin = runSetInsulin(uid)
            block(insulin)
        }
    }

    fun delete() {
        viewModelScope.launch { runDelete() }
    }

    fun save() {
        viewModelScope.launch { runSave() }
    }

    @VisibleForTesting
    suspend fun runSetInsulin(uid: Long) = withContext(IO) {
        insulinRepository.getById(uid)
    }

    @VisibleForTesting
    suspend fun runDelete() = withContext(IO) {
        insulinRepository.delete(insulin)
    }

    @VisibleForTesting
    suspend fun runSave() = withContext(IO) {
        insulinRepository.insert(insulin)
    }
}