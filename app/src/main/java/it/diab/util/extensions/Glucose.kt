package it.diab.util.extensions

import it.diab.db.entities.Glucose

fun glucose(block: Glucose.() -> Unit) = Glucose().apply(block)