package it.diab.db.dao

import android.arch.paging.DataSource
import android.arch.persistence.room.*
import it.diab.db.converters.TimeFrameConverter
import it.diab.db.entities.Insulin

@Dao
@TypeConverters(TimeFrameConverter::class)
interface InsulinDao {

    @get:Query("SELECT * FROM insulin")
    val all: DataSource.Factory<Int, Insulin>

    @get:Query("SELECT * FROM insulin")
    val allStatic: List<Insulin>

    @get:Query("SELECT * FROM insulin WHERE isBasal = 1")
    val basalInsulins: List<Insulin>

    @Query("SELECT * FROM insulin WHERE uid IN (:uids)")
    fun getById(vararg uids: Long): List<Insulin>

    @Query("SELECT * FROM insulin WHERE isBasal = :isBasal AND timeFrame = :timeFrame")
    fun getByTimeFrame(isBasal: Int, timeFrame: Int): List<Insulin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg insulins: Insulin)

    @Delete
    fun delete(insulin: Insulin)
}
