/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.ui.graph

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import it.diab.R

@SuppressLint("ViewConstructor")
class OverviewMarker(context: Context) : MarkerView(context, R.layout.item_overview_graph_marker) {
    private val mTextView = findViewById<TextView>(R.id.item_overview_graph_marker_text)
    private var mOffset: MPPointF? = null

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val h = Math.floor(e.x / 60.toDouble())
            val m = e.x % 60
            val text = BASE.format(h, m)
            mTextView.text = text
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        if (mOffset == null) {
            mOffset = MPPointF((width / -2f), height * -1f)
        }

        return mOffset
    }

    companion object {
        private const val BASE = "%1\$02.0f:%2\$02.0f"
    }
}