/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import androidx.annotation.VisibleForTesting
import androidx.paging.LivePagedListBuilder
import it.diab.core.data.entities.Insulin
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.viewmodels.ScopedViewModel
import it.diab.util.extensions.diff
import it.diab.util.extensions.getWeekDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class GlucoseListViewModel internal constructor(
    glucoseRepository: GlucoseRepository,
    private val insulinRepository: InsulinRepository
) : ScopedViewModel() {
    val pagedList = LivePagedListBuilder(glucoseRepository.pagedList, 5).build()

    private lateinit var insulins: List<Insulin>

    private var yesterday = ""
    private var today = ""
    private var lastX = "%1\$s"

    fun prepare(block: () -> Unit) {
        viewModelScope.launch {
            runPrepare()
            launch(Dispatchers.Main) { block() }
        }
    }

    fun getInsulin(uid: Long) = insulins.firstOrNull { it.uid == uid } ?: Insulin()

    fun setHeader(
        date: Date,
        format: SimpleDateFormat,
        block: (String, CoroutineScope) -> Unit
    ) {
        viewModelScope.launch {
            val text = runSetHeader(date, format)
            block(text, viewModelScope)
        }
    }

    fun setDateStrings(
        today: String,
        yesterday: String,
        lastX: String
    ) {
        this.yesterday = yesterday
        this.today = today
        this.lastX = lastX
    }

    @VisibleForTesting
    fun runPrepare() {
        insulins = insulinRepository.getInsulins()
    }

    @VisibleForTesting
    fun runSetHeader(date: Date, format: SimpleDateFormat): String {
        val diff = date.diff(Date())
        return when {
            diff == 0 -> today
            diff == -1 -> yesterday
            diff > -7 -> lastX.format(date.getWeekDay())
            else -> format.format(date)
        }
    }
}