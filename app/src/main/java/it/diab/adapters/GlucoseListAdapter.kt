/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.diab.R
import it.diab.core.data.entities.Glucose
import it.diab.core.util.PreferencesUtil
import it.diab.core.util.event.Event
import it.diab.core.util.extensions.setPrecomputedText
import it.diab.util.UIUtils
import it.diab.util.extensions.diff
import it.diab.viewmodels.glucose.GlucoseListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlucoseListAdapter(
    val context: Context,
    private val viewModel: GlucoseListViewModel
) : PagedListAdapter<Glucose, GlucoseListAdapter.GlucoseHolder>(CALLBACK) {

    private val _openGlucose = MutableLiveData<Event<Long>>()
    val openGlucose: LiveData<Event<Long>> = _openGlucose

    // Store the these for better performance
    private val lowIndicator = getIndicator(R.color.glucose_indicator_low)
    private val highIndicator = getIndicator(R.color.glucose_indicator_high)
    private val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat(
        context.getString(
            R.string.glucose_header_month
        ), Locale.getDefault()
    )
    private val highThreshold = PreferencesUtil.getGlucoseHighThreshold(context)
    private val lowThreshold = PreferencesUtil.getGlucoseLowThreshold(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GlucoseHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_glucose, parent, false)
        )

    override fun onBindViewHolder(holder: GlucoseHolder, position: Int) {
        val item = getItem(position)
        if (item == null) {
            holder.clear()
        } else {
            holder.onBind(item)
        }
    }

    private fun shouldInsertHeader(position: Int): Boolean {
        if (position == 0) {
            return true
        }

        val item = getItem(position) ?: return false
        val previous = getItem(position - 1) ?: return false

        val a = previous.date
        val b = item.date
        return b.diff(Date()) != 0 && a.diff(b) > 0
    }

    private fun getIndicator(@ColorRes colorId: Int): Drawable? {
        val resources = context.resources
        val color = ContextCompat.getColor(context, colorId)
        val size = resources.getDimensionPixelSize(R.dimen.item_glucose_indicator)
        return UIUtils.createRoundDrawable(resources, size, color)
    }

    inner class GlucoseHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon = view.findViewById<ImageView>(R.id.item_glucose_timezone)
        private val title = view.findViewById<TextView>(R.id.item_glucose_value)
        private val summary = view.findViewById<TextView>(R.id.item_glucose_insulin)
        private val indicator = view.findViewById<ImageView>(R.id.item_glucose_status)

        private val header = view.findViewById<ConstraintLayout>(R.id.item_glucose_header)
        private val headerTitle = view.findViewById<TextView>(R.id.item_glucose_header_title)

        fun onBind(glucose: Glucose) {
            itemView.visibility = View.VISIBLE

            bindHeader(glucose)
            bindValue(glucose)

            icon.setImageResource(glucose.timeFrame.icon)

            itemView.setOnClickListener { _openGlucose.value = Event(glucose.uid) }

            bindInsulins(glucose)
        }

        fun clear() {
            itemView.visibility = View.INVISIBLE
        }

        private fun bindHeader(glucose: Glucose) {
            val shouldShowHeader = shouldInsertHeader(adapterPosition)
            header.visibility = if (shouldShowHeader) View.VISIBLE else View.GONE

            if (shouldShowHeader) {
                viewModel.setHeader(glucose.date, dateFormat) { date ->
                    headerTitle.setPrecomputedText(date, viewModel.viewModelScope)
                }
            }
        }

        private fun bindValue(glucose: Glucose) {
            title.setPrecomputedText(
                "%1\$d (%2\$s)".format(glucose.value, hourFormat.format(glucose.date)),
                viewModel.viewModelScope
            )

            // High / low indicator
            val indicatorDrawable = when {
                glucose.value > highThreshold -> highIndicator
                glucose.value < lowThreshold -> lowIndicator
                else -> null
            }

            if (indicatorDrawable == null) {
                indicator.visibility = View.GONE
            } else {
                indicator.setImageDrawable(indicatorDrawable)
                indicator.visibility = View.VISIBLE
            }
        }

        private fun bindInsulins(glucose: Glucose) {
            val builder = StringBuilder()

            val insulinId = glucose.insulinId0
            val basalId = glucose.insulinId1

            if (insulinId >= 0) {
                builder.append(glucose.insulinValue0)
                    .append(" ")
                    .append(viewModel.getInsulin(insulinId).name)

                if (basalId >= 0) {
                    builder.append(", ")
                }
            }

            if (basalId >= 0) {
                builder.append(glucose.insulinValue1)
                    .append(" ")
                    .append(viewModel.getInsulin(basalId).name)
            }

            if (builder.isEmpty()) {
                summary.visibility = View.GONE
                return
            }

            summary.setPrecomputedText(builder.toString(), viewModel.viewModelScope)
            summary.visibility = View.VISIBLE
        }
    }

    companion object {
        private val CALLBACK = object : DiffUtil.ItemCallback<Glucose>() {
            override fun areContentsTheSame(oldItem: Glucose, newItem: Glucose) =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Glucose, newItem: Glucose) =
                oldItem.uid == newItem.uid
        }
    }
}
