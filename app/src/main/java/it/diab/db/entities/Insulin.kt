package it.diab.db.entities

import android.arch.persistence.room.*
import it.diab.db.converters.TimeFrameConverter
import it.diab.util.timeFrame.TimeFrame

@Entity(tableName = "insulin")
class Insulin {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    @ColumnInfo(name = KEY_NAME)
    var name: String = ""
    @ColumnInfo(name = KEY_TIMEFRAME)
    @TypeConverters(TimeFrameConverter::class)
    var timeFrame: TimeFrame = TimeFrame.EXTRA

    @Ignore
    constructor()

    constructor(uid: Long, name: String, timeFrame: TimeFrame) {
        this.uid = uid
        this.name = name
        this.timeFrame = timeFrame
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

    override fun toString() = "$name: $uid, ${timeFrame.toInt()}"

    companion object {
        const val KEY_UID = "uid"
        const val KEY_NAME = "name"
        const val KEY_TIMEFRAME = "timeFrame"
    }
}
