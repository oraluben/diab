/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import it.diab.db.converters.DateConverter
import it.diab.db.converters.TimeFrameConverter
import it.diab.util.extensions.toTimeFrame
import it.diab.util.timeFrame.TimeFrame
import java.util.Date

@Entity(
    tableName = "glucose",
    indices = [Index(value = ["date", "uid"], unique = true)]
)
class Glucose : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    @ColumnInfo(name = "value")
    var value: Int = 0
    @ColumnInfo(name = "date")
    @TypeConverters(DateConverter::class)
    var date: Date = Date()
    @ColumnInfo(name = "insulinId0")
    var insulinId0: Long = -1
    @ColumnInfo(name = "insulinValue0")
    var insulinValue0: Float = 0f
    @ColumnInfo(name = "insulinId1")
    var insulinId1: Long = -1
    @ColumnInfo(name = "insulinValue1")
    var insulinValue1: Float = 0f
    @EatLevel
    @ColumnInfo(name = "eatLevel")
    var eatLevel: Int = MEDIUM
    @ColumnInfo(name = "timeFrame")
    @TypeConverters(TimeFrameConverter::class)
    var timeFrame: TimeFrame = TimeFrame.EXTRA

    @Ignore
    constructor()

    @Ignore
    constructor(input: Parcel) {
        uid = input.readLong()
        value = input.readInt()
        date = Date(input.readLong())
        insulinId0 = input.readLong()
        insulinValue0 = input.readFloat()
        insulinId1 = input.readLong()
        insulinValue1 = input.readFloat()
        eatLevel = input.readInt()
        timeFrame = input.readInt().toTimeFrame()
    }

    constructor(
        uid: Long,
        value: Int,
        date: Date,
        insulinId0: Long,
        insulinValue0: Float,
        insulinId1: Long,
        insulinValue1: Float,
        @EatLevel eatLevel: Int,
        timeFrame: TimeFrame
    ) {
        this.uid = uid
        this.value = value
        this.date = date
        this.insulinId0 = insulinId0
        this.insulinValue0 = insulinValue0
        this.insulinId1 = insulinId1
        this.insulinValue1 = insulinValue1
        this.eatLevel = eatLevel
        this.timeFrame = timeFrame
    }

    override fun toString() = "Glucose $uid: value: $value, date: $date, " +
        "insulin0: {$insulinId0, $insulinValue0}, insulin1: {$insulinId1, $insulinValue1}, " +
        "eat: $eatLevel, timeFrame: $timeFrame"

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Glucose) {
            return false
        }

        return other.value == value &&
            other.date.time == date.time &&
            other.insulinId0 == insulinId0 &&
            other.insulinValue0 == insulinValue0 &&
            other.insulinId1 == insulinId1 &&
            other.insulinValue1 == insulinValue1 &&
            other.eatLevel == eatLevel &&
            other.timeFrame == timeFrame
    }

    override fun hashCode() = super.hashCode() + 1

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(uid)
        parcel.writeInt(value)
        parcel.writeLong(date.time)
        parcel.writeLong(insulinId0)
        parcel.writeFloat(insulinValue0)
        parcel.writeLong(insulinId1)
        parcel.writeFloat(insulinValue1)
        parcel.writeInt(eatLevel)
        parcel.writeInt(timeFrame.toInt())
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Glucose> {

        @IntDef(LOW, MEDIUM, HIGH, MAX)
        @Retention(AnnotationRetention.SOURCE)
        annotation class EatLevel

        const val LOW = 0
        const val MEDIUM = 1
        const val HIGH = 2
        const val MAX = 3

        override fun createFromParcel(parcel: Parcel) = Glucose(parcel)
        override fun newArray(size: Int): Array<Glucose?> = arrayOfNulls(size)
    }
}
