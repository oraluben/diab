package it.diab.ui.graph

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.charts.LineChart
import it.diab.R

class OverviewGraphView : LineChart {

    constructor(context: Context) : super(context) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet, defs: Int) : super(context, attrs, defs) {
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
        xAxis.apply {
            setDrawAxisLine(false)
            setDrawGridLines(false)
            isEnabled = false
        }

        axisRight.apply {
            setDrawAxisLine(false)
            setDrawLabels(false)
        }

        axisLeft.apply {
            setDrawAxisLine(false)
            setDrawLabels(false)
        }

        // Marker
        marker = OverviewMarker(context)

        // Empty status
        setNoDataTextColor(R.color.colorAccentDark)

        // Offset
        val horizontalOffset = context.resources.getDimension(R.dimen.overview_graph_offset)
        setExtraOffsets(horizontalOffset, 0f, horizontalOffset, 0f)
    }
}
