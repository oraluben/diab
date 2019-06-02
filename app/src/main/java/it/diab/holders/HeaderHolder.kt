/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.holders

import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import it.diab.R
import it.diab.data.entities.Glucose
import it.diab.ui.graph.DataSetFactory
import it.diab.ui.graph.OverviewGraphView
import it.diab.ui.models.DataSetsModel
import it.diab.ui.models.LastGlucoseModel
import it.diab.util.extensions.isToday
import java.text.SimpleDateFormat
import java.util.Locale

class HeaderHolder(view: View) : MainHolder(view) {
    private val lastValueText: TextView = itemView.findViewById(R.id.header_last_value)
    private val lastDescriptionText: TextView = itemView.findViewById(R.id.header_last_desc)
    private val overviewGraph: OverviewGraphView = itemView.findViewById(R.id.header_chart)

    private val hourFormatter by lazy {
        SimpleDateFormat(" (HH:mm)", Locale.getDefault())
    }

    fun bind(lastGlucoseModel: LastGlucoseModel, dataSetsModel: DataSetsModel) {
        onLastChanged(lastGlucoseModel)
        onGraphDataSetChanged(dataSetsModel)
    }

    private fun onLastChanged(model: LastGlucoseModel) {
        when (model) {
            is LastGlucoseModel.Empty -> setLastEmpty()
            is LastGlucoseModel.Available -> setLastAvailable(model.glucose)
        }
    }

    private fun onGraphDataSetChanged(model: DataSetsModel) {
        when (model) {
            is DataSetsModel.Empty -> setGraphEmpty()
            is DataSetsModel.Loading -> setGraphLoading()
            is DataSetsModel.Available -> setGraphAvailable(model.average, model.today)
        }
    }

    private fun setLastEmpty() {
        lastValueText.setText(R.string.overview_last_fallback)
        lastDescriptionText.setText(R.string.overview_last_desc_fallback)
    }

    private fun setLastAvailable(glucose: Glucose) {
        val context = itemView.context

        lastValueText.text = glucose.value.toString()
        lastDescriptionText.text = context.getString(
            R.string.overview_last_desc,
            if (glucose.date.isToday()) hourFormatter.format(glucose.date)
            else ""
        )
    }

    private fun setGraphEmpty() {
        overviewGraph.apply {
            data = null
            setNoDataText(R.string.overview_graph_empty)
        }
    }

    private fun setGraphLoading() {
        overviewGraph.apply {
            data = null
            setNoDataText(R.string.overview_graph_loading)
        }
    }

    private fun setGraphAvailable(average: List<Entry>, today: List<Entry>) {
        val context = itemView.context
        val factory = DataSetFactory(context)
        val list = arrayListOf<ILineDataSet>()
        factory.getToday(today)?.let { list.add(it) }
        factory.getAverage(average)?.let { list.add(it) }

        if (list.isEmpty()) {
            return
        }

        overviewGraph.apply {
            data = LineData(list)
            invalidate()
        }
    }
}