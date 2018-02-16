package it.diab.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.MainFragment
import it.diab.ui.graph.OverviewGraphView
import it.diab.util.extensions.getHour
import it.diab.util.extensions.isToday

class OverviewFragment : MainFragment() {
    private lateinit var mChart: OverviewGraphView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)
        mChart = view.findViewById(R.id.overview_chart)
        return view
    }

    override fun update(data: List<Glucose>?) = update(data, null)

    fun update(today: List<Glucose>?, average: HashMap<Int, Float>?) {
        if (activity == null || today == null || today.isEmpty()) {
            return
        }

        val dataSets = ArrayList<ILineDataSet>()
        val todaySet = getTodayDataSet(today)
        if (todaySet != null) {
            dataSets.add(todaySet)
        }

        if (average != null && average.isNotEmpty()) {
            val averageSet = getAverageDataSet(average)
            if (averageSet != null) {
                dataSets.add(averageSet)
            }
        }

        if (dataSets.isEmpty()) {
            return
        }

        mChart.data = LineData(dataSets)
        mChart.invalidate()
    }

    private fun getTodayDataSet(data: List<Glucose>): LineDataSet? {
        val entries = data
                .sortedBy { it.date.time }
                .filter { it.date.isToday() }
                .map { Entry(it.date.getHour() * 60f, it.value.toFloat()) }
                .distinctBy { it.x }

        if (entries.isEmpty()) {
            return null
        }

        val color = resources.getColor(R.color.graph_overview_today, resources.newTheme())
        val dataSet = LineDataSet(entries, "")

        dataSet.setCircleColor(color)
        dataSet.setCircleColorHole(color)
        dataSet.color = color
        dataSet.valueTextSize = resources.getDimension(R.dimen.overview_graph_text)
        return dataSet
    }

    private fun getAverageDataSet(data: HashMap<Int, Float>): LineDataSet? {
        val entries = data
                .filter { it.value != 0f }
                .map { Entry(it.key * 60f, it.value) }
                .sortedBy { it.x }

        if (entries.isEmpty()) {
            return null
        }

        val color = resources.getColor(R.color.graph_overview_average, resources.newTheme())
        val dataSet = LineDataSet(entries, "")

        dataSet.setCircleColor(color)
        dataSet.setCircleColorHole(color)
        dataSet.color = color
        dataSet.valueTextSize = 0f
        dataSet.isHighlightEnabled = false
        return dataSet

    }

    override fun getTitle() = R.string.fragment_overview
}