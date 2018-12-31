/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import android.content.res.Resources
import androidx.paging.LivePagedListBuilder
import it.diab.db.entities.Insulin
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.util.extensions.getHeader
import it.diab.viewmodels.ScopedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class GlucoseListViewModel internal constructor(
    glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ScopedViewModel() {
    val pagedList = LivePagedListBuilder(glucoseRepository.pagedList, 5).build()

    private lateinit var insulins: List<Insulin>

    fun prepare(block: () -> Unit) {
        viewModelScope.launch {
            insulins = insulinRepository.getInsulins()

            GlobalScope.launch(Dispatchers.Main) { block() }
        }
    }

    fun getInsulin(uid: Long) = insulins.firstOrNull { it.uid == uid } ?: Insulin()

    fun setHeader(
        res: Resources,
        date: Date,
        format: SimpleDateFormat,
        block: (String) -> Unit
    ) {
        viewModelScope.launch {
            val text = date.getHeader(res, Date(), format)
            GlobalScope.launch(Dispatchers.Main) { block(text) }
        }
    }
}