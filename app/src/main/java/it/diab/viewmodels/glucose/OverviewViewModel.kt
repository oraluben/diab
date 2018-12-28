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
import com.github.mikephil.charting.data.Entry
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.fit.BaseFitHandler
import it.diab.util.DateUtils
import it.diab.util.extensions.getAsMinutes
import it.diab.util.extensions.isToday
import it.diab.util.extensions.isZeroOrNan
import it.diab.util.extensions.toTimeFrame
import it.diab.util.timeFrame.TimeFrame
import it.diab.viewmodels.ScopedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.distinctBy
import kotlin.collections.filter
import kotlin.collections.filterNot
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.sortedBy
import kotlin.collections.sum

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

    fun getDataSets(data: List<Glucose>, block: (List<Entry>, List<Entry>) -> Unit) {
        viewModelScope.launch {
            val averageDef = async {
                val avg = HashMap<Int, Float>()
                val end = System.currentTimeMillis()
                val start = end - DateUtils.WEEK

                val size = TimeFrame.values().size - 2 // -1 for the iterator and -1 for "EXTRA"
                for (i in 0..size) {
                    val tf = i.toTimeFrame()

                    val lastWeek = glucoseRepository.getInDateRangeWithTimeFrame(start, end, i)
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

            GlobalScope.launch(Dispatchers.Main) { block(today, average) }
        }
    }
}