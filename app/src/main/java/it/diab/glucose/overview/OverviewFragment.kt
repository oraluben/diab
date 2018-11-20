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
import it.diab.ui.BannerView
import it.diab.ui.MainFragment
import it.diab.ui.graph.OverviewGraphView

class OverviewFragment : MainFragment() {
    private lateinit var mBanner: BannerView
    private lateinit var mChart: OverviewGraphView

    private lateinit var mViewModel: OverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this)[OverviewViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)
        mBanner = view.findViewById(R.id.overview_banner)
        mChart = view.findViewById(R.id.overview_chart)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.list.observe(this, Observer(this::update))

        val model = mViewModel.getBannerInfo()
        model?.apply {
            mBanner.setModel(this)
            mBanner.visibility = View.VISIBLE
        }
    }

    override fun getTitle() = R.string.fragment_overview

    private fun update(data: List<Glucose>?) {
        if (data == null || data.isEmpty()) {
            return
        }

        mViewModel.getDataSets(data, this::setDataSets)
    }

    private fun setDataSets(today: List<Entry>, average: List<Entry>) {
        val dataSets = ArrayList<ILineDataSet>()

        if (average.isNotEmpty()) {
            getAverageDataSet(average)?.let { dataSets.add(it) }
        }

        getTodayDataSet(today)?.let { dataSets.add(it) }

        if (dataSets.isEmpty()) {
            return
        }

        mChart.data = LineData(dataSets)
        mChart.invalidate()
    }

    private fun getTodayDataSet(entries: List<Entry>): LineDataSet? {
        if (entries.isEmpty() || context == null) {
            return null
        }

        val color = ContextCompat.getColor(context!!, R.color.graph_overview_today)

        return LineDataSet(entries, "").apply {
            setCircleColor(color)
            this.color = color
            highLightColor = color
            valueTextSize = resources.getDimension(R.dimen.overview_graph_text)
            valueFormatter = IValueFormatter { value, _, _, _ -> "%.0f".format(value) }
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
    }

    private fun getAverageDataSet(entries: List<Entry>): LineDataSet? {
        if (entries.isEmpty() || context == null) {
            return null
        }

        val color = ContextCompat.getColor(context!!, R.color.graph_overview_average)

        return LineDataSet(entries, "").apply {
            this.color = color
            valueTextSize = 0f
            isHighlightEnabled = false
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(false)
        }
    }
}