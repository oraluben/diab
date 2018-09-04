package it.diab.util.extensions

import it.diab.db.entities.Insulin

fun insulin(block: Insulin.() -> Unit) = Insulin().apply(block)