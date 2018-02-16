package it.diab.ui.graph

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import it.diab.R

class OverviewGraphView : LineChart {

    constructor(context: Context): super(context) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet, defs: Int): super(context, attrs, defs) {
        setup(context)
    }

    private fun setup(context: Context) {
        // Legend
        mLegend.isEnabled = false
        description.isEnabled = false

        // Zoom
        setPinchZoom(false)
        isDoubleTapToZoomEnabled = false
        setScaleEnabled(false)

        // Axis
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.isEnabled = false

        axisLeft.setDrawLabels(false)
        axisLeft.setDrawAxisLine(false)
        axisRight.setDrawLabels(false)
        axisRight.setDrawAxisLine(false)

        // Marker
        marker = OverviewMarker(context)

        // Offset
        val horizontalOffset = context.resources.getDimension(R.dimen.overview_graph_offset)
        setExtraOffsets(horizontalOffset, 0f, horizontalOffset, 0f)
    }
}