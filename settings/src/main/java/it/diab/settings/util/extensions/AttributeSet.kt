/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.settings.util.extensions

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.StyleableRes

fun AttributeSet.getResource(resource: Int, @StyleableRes styleable: IntArray, context: Context): Int {
    val attrArray = context.obtainStyledAttributes(this, styleable)
    val value = TypedValue()
    attrArray.getValue(resource, value)
    attrArray.recycle()
    return value.resourceId
}