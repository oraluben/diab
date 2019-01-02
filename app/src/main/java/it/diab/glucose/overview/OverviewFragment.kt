/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.overview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityOptionsCompat
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
import it.diab.glucose.editor.EditorActivity
import it.diab.settings.SettingsActivity
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
    private lateinit var menuView: ImageView

    private lateinit var viewModel: OverviewViewModel

    private val hourFormatter = SimpleDateFormat(" (HH:mm)", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        val factory = OverviewViewModelFactory(GlucoseRepository.getInstance(context))
        viewModel = ViewModelProviders.of(this, factory)[OverviewViewModel::class.java]

        viewModel.prepare(
            PreferenceManager.getDefaultSharedPreferences(context),
            SystemUtil.getOverrideObject(
                BaseFitHandler::class.java, context,
                R.string.config_class_fit_handler
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)
        lastValueView = view.findViewById(R.id.overview_last_value)
        lastDescView = view.findViewById(R.id.overview_last_desc)
        chart = view.findViewById(R.id.overview_chart)
        menuView = view.findViewById(R.id.overview_menu)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.last.observe(this, Observer(this::updateLast))
        viewModel.list.observe(this, Observer(this::updateChart))

        setupMenu()
    }

    override fun getTitle() = R.string.fragment_overview

    override fun onEditor(view: View) {
        val activity = activity ?: return

        val intent = Intent(activity, EditorActivity::class.java)
        val optionsCompat = ActivityOptionsCompat
            .makeSceneTransitionAnimation(activity, view, view.transitionName)
        startActivity(intent, optionsCompat.toBundle())
    }

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
        lastDescView.text = getString(
            R.string.overview_last_desc,
            if (glucose.date.isToday()) hourFormatter.format(glucose.date)
            else ""
        )
    }

    @SuppressLint("RestrictedApi") // Needed for MenuPopupHelper
    private fun setupMenu() {
        val context = context ?: return
        val ctxWrapper = ContextThemeWrapper(context, R.style.AppTheme_PopupMenuOverlapAnchor)
        val popupMenu = PopupMenu(
            ctxWrapper, menuView, Gravity.NO_GRAVITY,
            R.attr.actionOverflowMenuStyle, 0
        ).apply {
            inflate(R.menu.menu_overview)

            if (viewModel.fitHandler.isEnabled) {
                menu.findItem(R.id.menu_fit).isVisible = true
            }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_fit -> onMenuFit()
                    R.id.menu_settings -> onMenuSettings()
                    else -> false
                }
            }
        }

        val menuHelper = MenuPopupHelper(ctxWrapper, popupMenu.menu as MenuBuilder, menuView)

        menuView.setOnClickListener { menuHelper.show() }
    }

    private fun onMenuFit(): Boolean {
        val context = context ?: return false
        viewModel.fitHandler.openFitActivity(context)
        return true
    }

    private fun onMenuSettings(): Boolean {
        val context = context ?: return false
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
        return true
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
        val context = context ?: return null

        if (entries.isEmpty()) {
            return null
        }

        val color = ContextCompat.getColor(context, R.color.graph_overview_today)
        val textColor = ContextCompat.getColor(context, R.color.textPrimary)

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
        val context = context ?: return null

        if (entries.isEmpty()) {
            return null
        }

        val color = ContextCompat.getColor(context, R.color.graph_overview_average)

        return LineDataSet(entries, "").apply {
            this.color = color
            valueTextSize = 0f
            isHighlightEnabled = false
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(false)
        }
    }
}
