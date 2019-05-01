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
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import it.diab.R
import it.diab.data.entities.Glucose
import it.diab.core.util.PreferencesUtil
import it.diab.core.util.event.Event
import it.diab.holders.GlucoseHolder
import it.diab.holders.GlucoseHolderCallbacks
import it.diab.util.UIUtils
import it.diab.viewmodels.glucose.GlucoseListViewModel
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlucoseListAdapter(
    val context: Context,
    private val viewModel: GlucoseListViewModel
) : PagedListAdapter<Glucose, GlucoseHolder>(CALLBACK), GlucoseHolderCallbacks {

    private val _openGlucose = MutableLiveData<Event<Long>>()
    val openGlucose: LiveData<Event<Long>> = _openGlucose

    // Store the these for better performance
    private val lowIndicator by lazy { buildIndicator(R.color.glucose_indicator_low) }
    private val highIndicator by lazy { buildIndicator(R.color.glucose_indicator_high) }
    private val highThreshold by lazy { PreferencesUtil.getGlucoseHighThreshold(context) }
    private val lowThreshold by lazy { PreferencesUtil.getGlucoseLowThreshold(context) }

    private val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat(
        context.getString(R.string.glucose_header_month),
        Locale.getDefault()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        GlucoseHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_glucose, parent, false),
            this
        )

    override fun onBindViewHolder(holder: GlucoseHolder, position: Int) {
        val item = getItem(position)
        if (item == null) {
            holder.onLoading()
        } else {
            holder.onBind(item)
        }
    }

    override fun fetchHeaderText(date: Date, onFetch: (String, CoroutineScope) -> Unit) {
        viewModel.setHeader(date, dateFormat, onFetch)
    }

    override fun fetchHourText(date: Date, onFetch: (String, CoroutineScope) -> Unit) {
        val text = hourFormat.format(date)
        onFetch(text, viewModel.viewModelScope)
    }

    override fun getIndicator(value: Int) = when {
        value < lowThreshold -> lowIndicator
        value > highThreshold -> highIndicator
        else -> null
    }

    override fun getInsulinName(uid: Long) =
        viewModel.getInsulin(uid).name

    override fun onClick(uid: Long) {
        _openGlucose.value = Event(uid)
    }

    private fun buildIndicator(@ColorRes colorId: Int): Drawable? {
        val resources = context.resources
        val color = ContextCompat.getColor(context, colorId)
        val size = resources.getDimensionPixelSize(R.dimen.item_glucose_indicator)
        return UIUtils.createRoundDrawable(resources, size, color)
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
