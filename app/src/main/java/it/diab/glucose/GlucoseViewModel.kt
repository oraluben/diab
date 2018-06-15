package it.diab.glucose

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import it.diab.db.AppDatabase
import it.diab.db.DatabaseTask
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.DateUtils
import it.diab.util.extensions.toTimeFrame
import java.util.concurrent.ExecutionException

class GlucoseViewModel(owner: Application) : AndroidViewModel(owner) {
    val list: LiveData<List<Glucose>>
    val pagedList: LiveData<PagedList<Glucose>>
    private val mDatabase = AppDatabase.getInstance(owner)

    init {
        list = mDatabase.glucose().all
        pagedList = LivePagedListBuilder(mDatabase.glucose().pagedList, 20).build()
    }

    fun getInsulin(id: Long): Insulin {
        val task = FetchInsulinTask(mDatabase)
        task.execute(id)
        return try {
            task.get()
        } catch (e: InterruptedException) {
            Insulin()
        } catch (e: ExecutionException) {
            Insulin()
        }
    }

    fun getAverageLastWeek(): HashMap<Int, Float> {
        val map = HashMap<Int, Float>()
        val task = LoadAverageTask(mDatabase)
        task.execute()

        return try {
            val result = task.get()
            for ((i, item) in result.withIndex()) {
                val timeFrame = i.toTimeFrame()
                map[timeFrame.reprHour] = item
            }

            map
        } catch (e: InterruptedException) {
            map
        } catch (e: ExecutionException) {
            map
        }
    }

    private class FetchInsulinTask(db: AppDatabase) : DatabaseTask<Long, Insulin>(db) {

        override fun doInBackground(vararg params: Long?): Insulin {
            val list = mDatabase.insulin().getById(params[0] ?: -1)

            return if (list.isEmpty()) Insulin() else list[0]
        }
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
