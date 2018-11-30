/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.overview

import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.data.Entry
import it.diab.BuildConfig
import it.diab.R
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.fit.FitActivity
import it.diab.insulin.InsulinActivity
import it.diab.util.DateUtils
import it.diab.util.ScopedViewModel
import it.diab.util.extensions.bannerModel
import it.diab.util.extensions.get
import it.diab.util.extensions.getAsMinutes
import it.diab.util.extensions.isToday
import it.diab.util.extensions.isZeroOrNan
import it.diab.util.extensions.set
import it.diab.util.extensions.toTimeFrame
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class OverviewViewModel(owner: Application) : ScopedViewModel(owner) {
    val list: LiveData<List<Glucose>>

    private val db = AppDatabase.getInstance(owner)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(owner.applicationContext)

    init {
        list = db.glucose().all
    }

    fun getDataSets(data: List<Glucose>, onCompleted: (List<Entry>, List<Entry>) -> Unit) {
        viewModelScope.launch { fetchDataSets(data, onCompleted) }
    }

    fun getBannerInfo() = when {
        prefs[PREF_BANNER_INSULIN, true] -> getInsulinBanner()
        BuildConfig.HAS_FIT && prefs[PREF_BANNER_FIT, true] -> getFitBanner()
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
        icon = R.drawable.ic_google_fit
        positiveText = R.string.banner_fit_positive
        negativeText = R.string.banner_negative
        onPositive = { it.context.startActivity(Intent(it.context, FitActivity::class.java)) }
        onAction = { prefs[PREF_BANNER_FIT] = false }
    }

    private suspend fun fetchDataSets(data: List<Glucose>, onCompleted: (List<Entry>, List<Entry>) -> Unit) =
        viewModelScope.launch {
            // Get average entries
            val averageDeferred = async {
                val average = HashMap<Int, Float>()
                val end = System.currentTimeMillis()
                val start = end - DateUtils.WEEK

                val size = TimeFrame.values().size - 1
                for (i in 0..(size - 1)) {
                    val timeFrame = i.toTimeFrame()

                    val lastWeek = db.glucose().getInDateRangeWithTimeFrame(start, end, i)
                    val avgVal = lastWeek.indices.map { lastWeek[it].value }.sum() / lastWeek.size.toFloat()
                    average[timeFrame.reprHour] = avgVal
                }

                average.filterNot { it.value.isZeroOrNan() }
                    .map { Entry(it.key * 60f, it.value) }
                    .sortedBy { it.x }
            }

            // Get today entries
            val todayDeferred = async {
                data.sortedBy { it.date.time }
                    .filter { it.date.isToday() }
                    .map { Entry(it.date.getAsMinutes(), it.value.toFloat()) }
                    .distinctBy { it.x }
            }

            val averageEntries = averageDeferred.await()
            val todayEntries = todayDeferred.await()

            GlobalScope.launch(coroutineContext) { onCompleted(todayEntries, averageEntries) }
        }

    companion object {
        private const val PREF_BANNER_INSULIN = "pref_banner_insulin"
        private const val PREF_BANNER_FIT = "pref_banner_fit"
    }
}
