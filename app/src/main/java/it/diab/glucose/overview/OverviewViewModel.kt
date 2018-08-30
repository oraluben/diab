package it.diab.glucose.overview

import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.diab.BuildConfig
import it.diab.R
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.runOnDbThread
import it.diab.fit.FitActivity
import it.diab.insulin.InsulinActivity
import it.diab.util.DateUtils
import it.diab.util.bannerModel
import it.diab.util.extensions.get
import it.diab.util.extensions.set
import it.diab.util.extensions.toTimeFrame
import it.diab.util.timeFrame.TimeFrame

class OverviewViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<List<Glucose>>

    private val db = AppDatabase.getInstance(owner)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(owner.applicationContext)

    init {
        list = db.glucose().all
    }

    fun getAverageLastWeek() = runOnDbThread<HashMap<Int, Float>> {
        val map = HashMap<Int, Float>()
        val end = System.currentTimeMillis()
        val start = end - DateUtils.WEEK

        // Exclude TimeFrame.EXTRA
        val size = TimeFrame.values().size - 1
        for (i in 0..(size - 1)) {
            val timeFrame = i.toTimeFrame()

            val lastWeek = db.glucose().getInDateRangeWithTimeFrame(start, end, i)
            val average = lastWeek.indices
                    .map { lastWeek[it].value }
                    .sum() / lastWeek.size.toFloat()
            map[timeFrame.reprHour] = average
        }

        map
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

    companion object {
        private const val PREF_BANNER_INSULIN = "pref_banner_insulin"
        private const val PREF_BANNER_FIT = "pref_banner_fit"
    }
}
