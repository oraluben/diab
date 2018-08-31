package it.diab.util.extensions

fun <T> List<T>.firstIf(condition: (List<T>) -> Boolean, fallback: T) =
    if (condition(this))
        firstOrNull() ?: fallback
    else
        fallback