package it.diab.util.timeFrame

internal interface ITimeFrame {
    val icon: Int
    val string: Int
    val reprHour: Int

    fun toInt(): Int
}
