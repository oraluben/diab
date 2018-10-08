package it.diab.util.extensions

fun String.upperCaseFirstChrar() =
    substring(0, 1).toUpperCase() + substring(1, length)