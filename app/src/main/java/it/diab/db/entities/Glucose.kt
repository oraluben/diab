package it.diab.db.entities

import android.arch.persistence.room.*
import android.os.Parcel
import android.os.Parcelable
import it.diab.db.converters.DateConverter
import java.util.*

@Entity(tableName = "glucose",
        indices = [Index(value = ["date", "uid"], unique = true)])
class Glucose : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    @ColumnInfo(name = KEY_VALUE)
    var value: Int = 0
    @ColumnInfo(name = KEY_DATE)
    @TypeConverters(DateConverter::class)
    var date: Date = Date()
    @ColumnInfo(name = KEY_HEAVY_MEAL)
    var isHeavyMeal: Boolean = false
    @ColumnInfo(name = KEY_INSULIN_ID_0)
    var insulinId0: Long = -1
    @ColumnInfo(name = KEY_INSULIN_VALUE_0)
    var insulinValue0: Float = 0f
    @ColumnInfo(name = KEY_INSULIN_ID_1)
    var insulinId1: Long = -1
    @ColumnInfo(name = KEY_INSULIN_VALUE_1)
    var insulinValue1: Float = 0f

    @Ignore
    constructor()

    @Ignore
    constructor(input: Parcel) {
        uid = input.readLong()
        value = input.readInt()
        date = Date(input.readLong())
        isHeavyMeal = input.readByte().toInt() == 0
        insulinId0 = input.readLong()
        insulinValue0 = input.readFloat()
        insulinId1 = input.readLong()
        insulinValue1 = input.readFloat()
    }

    constructor(uid: Long, value: Int, date: Date, heavyMeal: Boolean, insulinId0: Long,
                insulinValue0: Float, insulinId1: Long, insulinValue1: Float) {
        this.uid = uid
        this.value = value
        this.date = date
        this.isHeavyMeal = heavyMeal
        this.insulinId0 = insulinId0
        this.insulinValue0 = insulinValue0
        this.insulinId1 = insulinId1
        this.insulinValue1 = insulinValue1
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Glucose) {
            return false
        }

        return other.value == value && other.date.time == date.time
    }

    override fun hashCode() = super.hashCode() + 1

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(uid)
        parcel.writeInt(value)
        parcel.writeLong(date.time)
        parcel.writeByte(if (isHeavyMeal) 1 else 0)
        parcel.writeLong(insulinId0)
        parcel.writeFloat(insulinValue0)
        parcel.writeLong(insulinId1)
        parcel.writeFloat(insulinValue1)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Glucose> {
        private const val KEY_VALUE = "value"
        private const val KEY_DATE = "date"
        private const val KEY_HEAVY_MEAL = "heavyMeal"
        private const val KEY_INSULIN_ID_0 = "insulinId0"
        private const val KEY_INSULIN_VALUE_0 = "insulinValue0"
        private const val KEY_INSULIN_ID_1 = "insulinId1"
        private const val KEY_INSULIN_VALUE_1 = "insulinValue1"

        override fun createFromParcel(parcel: Parcel) = Glucose(parcel)
        override fun newArray(size: Int): Array<Glucose?> = arrayOfNulls(size)
    }
}
