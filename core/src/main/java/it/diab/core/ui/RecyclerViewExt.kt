/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.ui

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.diab.core.R

class RecyclerViewExt : RecyclerView {
    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style) {
        setup(context, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        val gridLayout: Boolean
        val gridColumns: Int

        if (attrs == null) {
            gridLayout = false
            gridColumns = 1
        } else {
            val array = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewExt, 0, 0)
            gridLayout = array.getBoolean(R.styleable.RecyclerViewExt_gridLayout, false)
            gridColumns = array.getInt(R.styleable.RecyclerViewExt_gridColumns, 1)
            array.recycle()
        }

        itemAnimator = DefaultItemAnimator()
        layoutManager = if (gridLayout)
            GridLayoutManager(context, gridColumns)
        else
            LinearLayoutManager(context)
    }
}
