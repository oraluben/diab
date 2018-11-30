/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin

class InsulinViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<PagedList<Insulin>>
    private val db: AppDatabase = AppDatabase.getInstance(owner)

    init {
        list = LivePagedListBuilder(db.insulin().all, 5).build()
    }
}
