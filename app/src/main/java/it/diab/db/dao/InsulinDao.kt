/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
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
    fun insert(vararg insulins: Insulin): Array<Long>

    @Delete
    fun delete(insulin: Insulin)
}
