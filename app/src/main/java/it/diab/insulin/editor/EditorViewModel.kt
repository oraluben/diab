/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.editor

import android.app.Application
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin
import it.diab.util.ScopedViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditorViewModel(owner: Application) : ScopedViewModel(owner) {
    var insulin: Insulin = Insulin()
    private val db = AppDatabase.getInstance(owner)

    fun setInsulin(uid: Long, onInsulinSet: (Insulin) -> Unit) {
        viewModelScope.launch {
            insulin = db.insulin().getById(uid).firstOrNull() ?: Insulin()

            GlobalScope.launch { onInsulinSet(insulin) }
        }
    }

    fun delete() {
        viewModelScope.launch { db.insulin().delete(insulin) }
    }

    fun save() {
        viewModelScope.launch { db.insulin().insert(insulin) }
    }
}
