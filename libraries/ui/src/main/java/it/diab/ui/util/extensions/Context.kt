/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.ui.util.extensions

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.AttrRes
import androidx.core.content.res.TypedArrayUtils

@SuppressLint("RestrictedApi")
@AttrRes
fun Context.getAttr(attr: Int, fallbackAttr: Int) = TypedArrayUtils.getAttr(
    this, attr, fallbackAttr
)