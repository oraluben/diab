package it.diab.util.extensions

import android.content.Context
import android.support.annotation.DimenRes

fun Context.getDimen(@DimenRes dimenId: Int) = resources.getDimensionPixelSize(dimenId)