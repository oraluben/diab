/*
 * Copyright (c) 2019 Bevilacqua Joey
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
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import it.diab.R
import it.diab.core.util.PreferencesUtil
import it.diab.core.util.event.Event
import it.diab.data.entities.GlucoseWithInsulin
import it.diab.holders.GlucoseHolder
import it.diab.holders.GlucoseHolderCallbacks
import it.diab.holders.HeaderHolder
import it.diab.holders.MainHolder
import it.diab.ui.ShiftedAdapter
import it.diab.ui.models.DataSetsModel
import it.diab.ui.models.LastGlucoseModel
import it.diab.util.UIUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainAdapter(
    private val context: Context,
    private val callbacks: Callbacks
) : ShiftedAdapter<GlucoseWithInsulin, MainHolder>(CONFIG, 1), GlucoseHolderCallbacks {

    private val _openGlucose = MutableLiveData<Event<Long>>()
    val openGlucose: LiveData<Event<Long>> = _openGlucose

    // Store the these for better performance
    private val lowIndicator by lazy { buildIndicator(R.color.glucoseIndicator_low) }
    private val highIndicator by lazy { buildIndicator(R.color.glucoseIndicator_high) }
    private val highThreshold by lazy { PreferencesUtil.getGlucoseHighThreshold(context) }
    private val lowThreshold by lazy { PreferencesUtil.getGlucoseLowThreshold(context) }

    private val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == VIEW_HEADER) {
            HeaderHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
            )
        } else {
            GlucoseHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_glucose, parent, false),
                this
            )
        }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        if (holder is HeaderHolder) {
            bindHeader(holder)
        } else if (holder is GlucoseHolder) {
            bindGlucose(holder, position)
        }
    }

    private fun bindHeader(holder: HeaderHolder) {
        holder.bind(
            callbacks.getLastGlucose(),
            callbacks.getDataSets()
        )
    }

    private fun bindGlucose(holder: GlucoseHolder, position: Int) {
        val item = getItem(position)
        if (item == null) {
            holder.onLoading()
        } else {
            holder.onBind(item)
        }
    }

    override fun fetchHourText(date: Date): String {
        return hourFormat.format(date)
    }

    override fun getIndicator(value: Int) = when {
        value < lowThreshold -> lowIndicator
        value > highThreshold -> highIndicator
        else -> null
    }

    override fun onClick(uid: Long) {
        _openGlucose.value = Event(uid)
    }

    override fun getItemViewType(position: Int) = if (position == 0) VIEW_HEADER else VIEW_GLUCOSE

    private fun buildIndicator(@ColorRes colorId: Int): Drawable? {
        val resources = context.resources
        val color = ContextCompat.getColor(context, colorId)
        val size = resources.getDimensionPixelSize(R.dimen.item_glucose_indicator)
        return UIUtils.createRoundDrawable(resources, size, color)
    }

    interface Callbacks {
        fun getLastGlucose(): LastGlucoseModel
        fun getDataSets(): DataSetsModel
    }

    companion object {
        private val CONFIG =
            AsyncDifferConfig.Builder(object : DiffUtil.ItemCallback<GlucoseWithInsulin>() {
                override fun areContentsTheSame(oldItem: GlucoseWithInsulin, newItem: GlucoseWithInsulin) =
                    oldItem.glucose == newItem.glucose

                override fun areItemsTheSame(oldItem: GlucoseWithInsulin, newItem: GlucoseWithInsulin) =
                    oldItem.glucose.uid == newItem.glucose.uid
        }).build()

        private const val VIEW_HEADER = 0
        private const val VIEW_GLUCOSE = 1
    }
}
