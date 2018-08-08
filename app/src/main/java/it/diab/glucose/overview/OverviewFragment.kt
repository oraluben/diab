package it.diab.glucose.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.MainFragment
import it.diab.ui.graph.OverviewGraphView
import it.diab.util.extensions.getAsMinutes
import it.diab.util.extensions.isToday

class OverviewFragment : MainFragment() {
    private lateinit var mChart: OverviewGraphView

    private lateinit var mViewModel: OverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this)[OverviewViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)
        mChart = view.findViewById(R.id.overview_chart)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.list.observe(this, Observer(this::update))
    }

    private fun update(today: List<Glucose>?) {
        if (today == null || today.isEmpty()) {
            return
        }

        val dataSets = ArrayList<ILineDataSet>()

        val average = mViewModel.getAverageLastWeek()
        if (average.isNotEmpty()) {
            val averageSet = getAverageDataSet(average)
            if (averageSet != null) {
                dataSets.add(averageSet)
            }
        }

        val todaySet = getTodayDataSet(today)
        if (todaySet != null) {
            dataSets.add(todaySet)
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
                .map { Entry(it.date.getAsMinutes(), it.value.toFloat()) }
                .distinctBy { it.x }

        if (entries.isEmpty() || context == null) {
            return null
        }

        val color = ContextCompat.getColor(context!!, R.color.graph_overview_today)
        val dataSet = LineDataSet(entries, "")

        dataSet.setCircleColor(color)
        dataSet.setCircleColorHole(color)
        dataSet.color = color
        dataSet.highLightColor = color
        dataSet.valueTextSize = resources.getDimension(R.dimen.overview_graph_text)
        dataSet.valueFormatter = IValueFormatter { value, _, _, _ -> "%.0f".format(value) }
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        return dataSet
    }

    private fun getAverageDataSet(data: HashMap<Int, Float>): LineDataSet? {
        val entries = data
                .filterNot { it.value == 0f || Float.NaN.equals(it.value) }
                .map { Entry(it.key * 60f, it.value) }
                .sortedBy { it.x }

        if (entries.isEmpty() || context == null) {
            return null
        }

        val color = ContextCompat.getColor(context!!, R.color.graph_overview_average)
        val dataSet = LineDataSet(entries, "")

        dataSet.color = color
        dataSet.valueTextSize = 0f
        dataSet.isHighlightEnabled = false
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawCircles(false)
        return dataSet
    }

    override fun getTitle() = R.string.fragment_overview
}