package it.diab.util.extensions

fun Float.isZeroOrNan() = 0f == this || isNaN()