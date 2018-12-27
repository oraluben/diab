/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.overview

import android.os.Bundle
import android.preference.PreferenceManager
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
import it.diab.db.repositories.GlucoseRepository
import it.diab.fit.BaseFitHandler
import it.diab.ui.BannerView
import it.diab.ui.MainFragment
import it.diab.ui.graph.OverviewGraphView
import it.diab.util.SystemUtil
import it.diab.viewmodels.glucose.OverviewViewModel
import it.diab.viewmodels.glucose.OverviewViewModelFactory

class OverviewFragment : MainFragment() {
    private lateinit var mBanner: BannerView
    private lateinit var mChart: OverviewGraphView

    private lateinit var viewModel: OverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        val factory = OverviewViewModelFactory(GlucoseRepository.getInstance(context))
        viewModel = ViewModelProviders.of(this, factory)[OverviewViewModel::class.java]

        viewModel.prepare(
                PreferenceManager.getDefaultSharedPreferences(context),
                SystemUtil.getOverrideObject(BaseFitHandler::class.java, context,
                        R.string.config_class_fit_handler)
        )
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

        viewModel.list.observe(this, Observer(this::update))

        val model = viewModel.getBannerInfo()
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

        viewModel.getDataSets(data, this::setDataSets)
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
        val textColor = ContextCompat.getColor(context!!, R.color.textPrimary)

        return LineDataSet(entries, "").apply {
            setCircleColor(color)
            this.color = color
            highLightColor = color
            lineWidth = resources.getDimension(R.dimen.overview_graph_line_thickness)
            valueTextSize = resources.getDimension(R.dimen.overview_graph_text)
            valueTextColor = textColor
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