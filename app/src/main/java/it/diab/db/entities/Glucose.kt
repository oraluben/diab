package it.diab.db.entities

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntDef
import it.diab.db.converters.DateConverter
import java.util.*

@Entity(tableName = "glucose",
        indices = [Index(value = ["date", "uid"], unique = true)])
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
    }

    constructor(uid: Long, value: Int, date: Date, insulinId0: Long,
                insulinValue0: Float, insulinId1: Long, insulinValue1: Float,
                @EatLevel eatLevel: Int) {
        this.uid = uid
        this.value = value
        this.date = date
        this.insulinId0 = insulinId0
        this.insulinValue0 = insulinValue0
        this.insulinId1 = insulinId1
        this.insulinValue1 = insulinValue1
        this.eatLevel = eatLevel
    }

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
                other.eatLevel == eatLevel
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
