package it.diab.db.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.*
import it.diab.db.converters.DateConverter
import it.diab.db.entities.Glucose

@Dao
@TypeConverters(DateConverter::class)
interface GlucoseDao {

    @get:Query("SELECT * FROM glucose ORDER BY date DESC")
    val all: LiveData<List<Glucose>>

    @get:Query("SELECT * FROM glucose ORDER BY date DESC")
    val allStatic: List<Glucose>

    @get:Query("SELECT * FROM glucose ORDER BY date DESC")
    val pagedList: DataSource.Factory<Int, Glucose>

    @Query("SELECT * FROM glucose WHERE uid IN (:uids)")
    fun getById(vararg uids: Long): List<Glucose>

    @Query("SELECT * FROM glucose WHERE date >= :minTime AND date <= :maxTime ORDER BY date DESC")
    fun getInDateRange(minTime: Long, maxTime: Long): List<Glucose>

    @Query("SELECT * FROM glucose WHERE date >= :minTime AND date <= :maxTime AND timeFrame = :timeFrame ORDER BY date DESC")
    fun getInDateRangeWithTimeFrame(minTime: Long, maxTime: Long, timeFrame: Int): List<Glucose>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg glucose: Glucose)

    @Delete
    fun delete(glucose: Glucose)
}
