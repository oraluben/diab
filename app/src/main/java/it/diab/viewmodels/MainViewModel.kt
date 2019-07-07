/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels

import android.util.SparseArray
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import com.github.mikephil.charting.data.Entry
import it.diab.core.util.DateUtils
import it.diab.data.entities.Glucose
import it.diab.data.entities.TimeFrame
import it.diab.data.extensions.toTimeFrame
import it.diab.data.repositories.GlucoseRepository
import it.diab.ui.models.DataSetsModel
import it.diab.util.extensions.getAsMinutes
import it.diab.util.extensions.isToday
import it.diab.util.extensions.isZeroOrNan
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel internal constructor(
    private val glucoseRepository: GlucoseRepository
) : ViewModel() {

    val pagedList = LivePagedListBuilder(glucoseRepository.pagedList, 5).build()
    val liveList = glucoseRepository.all

    fun getDataSets(block: (DataSetsModel) -> Unit) {
        viewModelScope.launch {
            val end = System.currentTimeMillis()
            val start = end - DateUtils.WEEK
            val data = glucoseRepository.getInDateRange(start, end)
            val result = runGetDataSets(data)
            block(result)
        }
    }

    @VisibleForTesting
    suspend fun runGetDataSets(data: List<Glucose>): DataSetsModel = withContext(Default) {
        val todayDeferred = async { updateToday(data) }
        val avgDeferred = async { updateAverage(data) }

        DataSetsModel.Available(
            todayDeferred.await(),
            avgDeferred.await()
        )
    }

    private fun updateToday(list: List<Glucose>) =
        list.filter { it.date.isToday() }
            .sortedBy { it.date.time }
            .map { Entry(it.date.getAsMinutes(), it.value.toFloat()) }
            .distinctBy { it.x }

    private fun updateAverage(list: List<Glucose>): List<Entry> {
        val average = SparseArray<Float>()
        val size = TimeFrame.values().size - 2 // -1 because we start at 0 and -1 for "EXTRA"

        for (i in 0..size) {
            val timeFrame = i.toTimeFrame()
            val lastWeek = list.filter { it.timeFrame == timeFrame }
            val averageValue = lastWeek.sumBy { it.value } / lastWeek.size.toFloat()
            average.put(timeFrame.reprHour, averageValue)
        }

        val result = arrayListOf<Entry>()
        for (i in 0..size) {
            val index = i.toTimeFrame().reprHour
            val value = average.get(index)
            if (value.isZeroOrNan()) {
                continue
            }

            result.add(Entry(index * 60f, value))
        }
        return result.sortedBy { it.x }
    }
}