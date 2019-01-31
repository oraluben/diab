/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.github.mikephil.charting.data.Entry
import it.diab.core.data.entities.Glucose
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.override.BaseFitHandler
import it.diab.core.util.DateUtils
import it.diab.util.extensions.getAsMinutes
import it.diab.util.extensions.isToday
import it.diab.util.extensions.isZeroOrNan
import it.diab.core.util.extensions.toTimeFrame
import it.diab.core.data.entities.TimeFrame
import it.diab.core.viewmodels.ScopedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.collections.set

class OverviewViewModel internal constructor(
    private val glucoseRepository: GlucoseRepository
) : ScopedViewModel() {

    val list = glucoseRepository.all
    val last = glucoseRepository.last

    lateinit var fitHandler: BaseFitHandler
    private lateinit var prefs: SharedPreferences

    fun prepare(sPrefs: SharedPreferences, fHandler: BaseFitHandler) {
        prefs = sPrefs
        fitHandler = fHandler
    }

    fun getDataSets(block: (List<Entry>, List<Entry>) -> Unit) {
        viewModelScope.launch {
            val end = System.currentTimeMillis()
            val start = end - DateUtils.WEEK
            val data = glucoseRepository.getInDateRange(start, end)
            val pair = runGetDataSets(data)
            GlobalScope.launch(Dispatchers.Main) { block(pair.first, pair.second) }
        }
    }

    @VisibleForTesting
    suspend fun runGetDataSets(data: List<Glucose>): Pair<List<Entry>, List<Entry>> {
        val averageDef = async {
            val avg = HashMap<Int, Float>()

            val size = TimeFrame.values().size - 2 // -1 for the iterator and -1 for "EXTRA"
            for (i in 0..size) {
                val tf = i.toTimeFrame()

                val lastWeek = data.filter { it.timeFrame == tf }
                val avgVal = lastWeek.indices.map { lastWeek[it].value }.sum() /
                    lastWeek.size.toFloat()
                avg[tf.reprHour] = avgVal
            }

            avg.filterNot { it.value.isZeroOrNan() }
                .map { Entry(it.key * 60f, it.value) }
                .sortedBy { it.x }
        }

        val todayDef = async {
            data.sortedBy { it.date.time }
                .filter { it.date.isToday() }
                .map { Entry(it.date.getAsMinutes(), it.value.toFloat()) }
                .distinctBy { it.x }
        }

        val today = todayDef.await()
        val average = averageDef.await()

        return Pair(today, average)
    }
}