package it.diab.db.entities

import androidx.room.*
import it.diab.db.converters.TimeFrameConverter
import it.diab.util.timeFrame.TimeFrame

@Entity(tableName = "insulin")
class Insulin {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    @ColumnInfo(name = "name")
    var name: String = ""
    @ColumnInfo(name = "timeFrame")
    @TypeConverters(TimeFrameConverter::class)
    var timeFrame: TimeFrame = TimeFrame.EXTRA
    @ColumnInfo(name = "isBasal")
    var isBasal: Boolean = false
    @ColumnInfo(name = "hasHalfUnits")
    var hasHalfUnits: Boolean = false

    @Ignore
    constructor()

    constructor(uid: Long, name: String, timeFrame: TimeFrame,
                isBasal: Boolean, hasHalfUnits: Boolean) {
        this.uid = uid
        this.name = name
        this.timeFrame = timeFrame
        this.isBasal = isBasal
        this.hasHalfUnits = hasHalfUnits
    }

    fun setTimeFrame(timeFrame: Int) {
        this.timeFrame = TimeFrameConverter().toTimeFrame(timeFrame) ?: TimeFrame.EXTRA
    }

    fun getDisplayedString(value: Float) = "%1\$s %2\$s".format(name, "%.1f".format(value))

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Insulin) {
            return false
        }

        return other.name == name && other.timeFrame.toInt() == timeFrame.toInt()
    }

    override fun hashCode() = super.hashCode() + 1

    override fun toString() = "$name: $uid, ${timeFrame.toInt()}, $isBasal, $hasHalfUnits"
}

// DSL

fun insulin(block: Insulin.() -> Unit) = Insulin().apply(block)