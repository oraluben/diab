package it.diab.glucose.overview

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Glucose
import it.diab.util.DateUtils
import it.diab.util.extensions.toTimeFrame

class OverviewViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<List<Glucose>>

    private val db = AppDatabase.getInstance(owner)

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
}
