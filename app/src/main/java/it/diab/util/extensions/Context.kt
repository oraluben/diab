package it.diab.util.extensions

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.support.annotation.StyleableRes
import android.util.AttributeSet
import android.util.TypedValue

fun Context.getDimen(@DimenRes dimenId: Int) = resources.getDimensionPixelSize(dimenId)