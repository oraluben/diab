/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import androidx.annotation.VisibleForTesting
import androidx.paging.LivePagedListBuilder
import it.diab.core.viewmodels.ScopedViewModel
import it.diab.data.entities.Insulin
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GlucoseListViewModel internal constructor(
    glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ScopedViewModel() {
    val pagedList = LivePagedListBuilder(glucoseRepository.pagedList, 5).build()
    val liveList = glucoseRepository.all

    private lateinit var insulins: List<Insulin>

    fun prepare(block: () -> Unit) {
        viewModelScope.launch {
            runPrepare()
            launch(Dispatchers.Main) { block() }
        }
    }

    fun getInsulin(uid: Long) = insulins.firstOrNull { it.uid == uid } ?: Insulin()

    @VisibleForTesting
    fun runPrepare() {
        insulins = insulinRepository.getInsulins()
    }
}