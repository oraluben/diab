package it.diab.util

object DateUtils {
    private var SECOND: Long = 1000 // Millisecond * 1000
    private var MINUTE = 60 * SECOND
    private var HOUR = 60 * MINUTE
    var DAY = 24 * HOUR
    var WEEK = 7 * DAY
}
