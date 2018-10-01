package it.diab.test

import java.util.Random

fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start
