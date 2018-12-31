/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.insulin

import it.diab.db.entities.Insulin
import it.diab.db.repositories.InsulinRepository
import it.diab.viewmodels.ScopedViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditorViewModel internal constructor(
    private val insulinRepository: InsulinRepository
) : ScopedViewModel() {

    var insulin = Insulin()

    fun setInsulin(uid: Long, block: (Insulin) -> Unit) {
        viewModelScope.launch {
            insulin = insulinRepository.getById(uid)

            GlobalScope.launch { block(insulin) }
        }
    }

    fun delete() {
        viewModelScope.launch { insulinRepository.delete(insulin) }
    }

    fun save() {
        viewModelScope.launch { insulinRepository.insert(insulin) }
    }
}