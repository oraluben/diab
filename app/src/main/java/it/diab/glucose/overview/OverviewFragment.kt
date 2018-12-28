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
import android.widget.TextView
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
import it.diab.ui.MainFragment
import it.diab.ui.graph.OverviewGraphView
import it.diab.util.SystemUtil
import it.diab.util.extensions.isToday
import it.diab.viewmodels.glucose.OverviewViewModel
import it.diab.viewmodels.glucose.OverviewViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale


class OverviewFragment : MainFragment() {
    private lateinit var lastValueView: TextView
    private lateinit var lastDescView: TextView
    private lateinit var chart: OverviewGraphView

    private lateinit var viewModel: OverviewViewModel

    private val hourFormatter = SimpleDateFormat(" (HH:mm)", Locale.getDefault())

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
        lastValueView = view.findViewById(R.id.overview_last_value)
        lastDescView = view.findViewById(R.id.overview_last_desc)
        chart = view.findViewById(R.id.overview_chart)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.last.observe(this, Observer(this::updateLast))
        viewModel.list.observe(this, Observer(this::updateChart))
    }

    override fun getTitle() = R.string.fragment_overview

    private fun updateChart(data: List<Glucose>?) {
        if (data == null || data.isEmpty()) {
            return
        }

        viewModel.getDataSets(data, this::setDataSets)
    }

    private fun updateLast(data: List<Glucose>?) {
        if (data == null) {
            return
        }

        if (data.isEmpty()) {
            lastValueView.text = getString(R.string.overview_last_fallback)
            lastDescView.text = getString(R.string.overview_last_desc_fallback)
            return
        }

        val glucose = data[0]

        lastValueView.text = "${glucose.value}"
        lastDescView.text = getString(R.string.overview_last_desc,
                if (glucose.date.isToday()) hourFormatter.format(glucose.date)
                else "")
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

        chart.data = LineData(dataSets)
        chart.invalidate()
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