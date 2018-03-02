package it.diab.util.extensions

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.StyleableRes
import android.util.AttributeSet
import android.util.TypedValue

@ColorInt
fun Context.getColorAttr(attrs: AttributeSet? = null,
                         @StyleableRes attribute: Int,
                         @StyleableRes styleable: IntArray,
                         @ColorInt default: Int = Color.BLACK): Int {
    val array =
            if (attrs == null) getAttrsArray(attribute)
            else obtainStyledAttributes(attrs, styleable, 0, 0)
    return array.getColor(attribute, default)
            .also { array.recycle() }
}

fun Context.getStringAttr(attrs: AttributeSet? = null,
                          @StyleableRes attribute: Int,
                          @StyleableRes styleable: IntArray,
                          default: String = ""): String {
    val array =
            if (attrs == null) getAttrsArray(attribute)
            else obtainStyledAttributes(attrs, styleable, 0, 0)
    return array.getString(attribute) ?: default
            .also { array.recycle() }
}

fun Context.getFloatAttr(attrs: AttributeSet? = null,
                         @StyleableRes attribute: Int,
                         @StyleableRes styleable: IntArray,
                         default: Float = 100f): Float {
    val array =
            if (attrs == null) getAttrsArray(attribute)
            else obtainStyledAttributes(attrs, styleable, 0, 0)
    return array.getFloat(attribute, default)
            .also { array.recycle() }
}

private fun Context.getAttrsArray(vararg attrs: Int) =
        obtainStyledAttributes(TypedValue().data, attrs)
