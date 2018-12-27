/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import android.content.Intent
import android.content.SharedPreferences
import com.github.mikephil.charting.data.Entry
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.fit.BaseFitHandler
import it.diab.insulin.InsulinActivity
import it.diab.util.DateUtils
import it.diab.util.extensions.*
import it.diab.util.timeFrame.TimeFrame
import it.diab.viewmodels.ScopedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class OverviewViewModel internal constructor(
        private val glucoseRepository: GlucoseRepository
) : ScopedViewModel() {

    val list = glucoseRepository.all

    private lateinit var prefs: SharedPreferences
    private lateinit var fitHandler: BaseFitHandler

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

    fun getBannerInfo() = when {
        prefs[PREF_BANNER_INSULIN, true] -> getInsulinBanner()
        prefs[PREF_BANNER_FIT, true] -> getFitBanner()
        else -> null
    }

    private fun getInsulinBanner() = bannerModel {
        title = R.string.banner_insulin_add
        positiveText = R.string.banner_insulin_positive
        onPositive = { it.context.startActivity(Intent(it.context, InsulinActivity::class.java)) }
        onAction = { prefs[PREF_BANNER_INSULIN] = false }
    }

    private fun getFitBanner() = bannerModel {
        title = R.string.banner_fit_integration
        icon = R.drawable.ic_fitness_logo
        positiveText = R.string.banner_fit_positive
        negativeText = R.string.banner_negative
        onPositive = { fitHandler.openFitActivity(it.context) }
        onAction = { prefs[PREF_BANNER_FIT] = false }
    }

    companion object {
        private const val PREF_BANNER_INSULIN = "pref_banner_insulin"
        private const val PREF_BANNER_FIT = "pref_banner_fit"
    }
}