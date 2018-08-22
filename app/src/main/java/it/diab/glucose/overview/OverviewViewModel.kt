package it.diab.glucose.overview

import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.diab.BuildConfig
import it.diab.R
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Glucose
import it.diab.fit.FitActivity
import it.diab.insulin.InsulinActivity
import it.diab.util.DateUtils
import it.diab.util.bannerModel
import it.diab.util.extensions.get
import it.diab.util.extensions.set
import it.diab.util.extensions.toTimeFrame

class OverviewViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<List<Glucose>>

    private val db = AppDatabase.getInstance(owner)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(owner.applicationContext)

    init {
        list = db.glucose().all
    }

    fun getAverageLastWeek(): HashMap<Int, Float> {
        val map = HashMap<Int, Float>()
        val task = LoadAverageTask(db)
        task.execute()

        val result = task.get()
        for ((i, item) in result.withIndex()) {
            val timeFrame = i.toTimeFrame()
            map[timeFrame.reprHour] = item
        }

        return map
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

    private class LoadAverageTask(db: AppDatabase) : DatabaseTask<Unit, List<Float>>(db) {

        override fun doInBackground(vararg p0: Unit): List<Float> {
            val now = System.currentTimeMillis()
            val result = Array(6, { 0f })

            for (i in 0..5) {
                result[i] = getAverageForTimeFrame(now, i)
            }

            return result.asList()
        }

        private fun getAverageForTimeFrame(initialTime: Long, timeFrameIndex: Int): Float {
            val list = mDatabase.glucose()
                    .getInDateRangeWithTimeFrame(
                            initialTime - DateUtils.WEEK, initialTime, timeFrameIndex)

            return list.indices
                    .map { list[it].value.toFloat() }
                    .sum() / list.size
        }
    }

    companion object {
        private const val PREF_BANNER_INSULIN = "pref_banner_insulin"
        private const val PREF_BANNER_FIT = "pref_banner_fit"
    }
}
