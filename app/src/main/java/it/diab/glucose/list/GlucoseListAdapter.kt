/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.list

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
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.recyclerview.ViewHolderExt
import it.diab.util.UIUtils
import it.diab.util.extensions.diff
import it.diab.util.extensions.setPrecomputedText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlucoseListAdapter(private val mContext: Context, private val onItemClick: (Long) -> Unit) :
        PagedListAdapter<Glucose, GlucoseListAdapter.GlucoseHolder>(CALLBACK) {

    private lateinit var viewModel: GlucoseListViewModel

    // Store the these for better performance
    private val mLowIndicator = getIndicator(R.color.glucose_indicator_low)
    private val mHighIndicator = getIndicator(R.color.glucose_indicator_high)
    private val mHourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val mDateFormat = SimpleDateFormat(mContext.getString(
            R.string.time_day_month_short_format), Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            GlucoseHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_glucose, parent, false))

    override fun onBindViewHolder(holder: GlucoseHolder, position: Int) {
        val item = getItem(position)
        if (item == null) {
            holder.clear()
        } else {
            holder.onBind(item, position)
        }
    }

    private fun shouldInsertHeader(position: Int): Boolean {
        if (position == 0) {
            return false
        }

        val item = getItem(position) ?: return false
        val previous = getItem(position - 1) ?: return false

        val a = previous.date
        val b = item.date
        return b.diff(Date()) != 0 && a.diff(b) > 0
    }

    private fun getIndicator(@ColorRes colorId: Int): Drawable? {
        val resources = mContext.resources
        val color = ContextCompat.getColor(mContext, colorId)
        val size = resources.getDimensionPixelSize(R.dimen.item_glucose_indicator)
        return UIUtils.createRoundDrawable(resources, size, color)
    }

    inner class GlucoseHolder(view: View) : ViewHolderExt(view) {
        private val mLayout = view.findViewById<ConstraintLayout>(R.id.item_glucose_layout)
        private val mIcon = view.findViewById<ImageView>(R.id.item_glucose_timezone)
        private val mTitle = view.findViewById<TextView>(R.id.item_glucose_value)
        private val mSummary = view.findViewById<TextView>(R.id.item_glucose_insulin)
        private val mIndicator = view.findViewById<ImageView>(R.id.item_glucose_status)

        private val mHeaderLayout = view.findViewById<ConstraintLayout>(R.id.item_glucose_header)
        private val mHeaderTitle = view.findViewById<TextView>(R.id.item_glucose_header_title)
        private val mHeaderDesc = view.findViewById<TextView>(R.id.item_glucose_header_description)

        fun onBind(glucose: Glucose, position: Int) {

            // The viewModel must be ready in order to bind the viewHolder
            if (!::viewModel.isInitialized) {
                viewModel = ViewModelProviders.of(mContext as MainActivity)[GlucoseListViewModel::class.java]
                viewModel.prepare { onBind(glucose, position) }
                return
            }

            id = glucose.uid

            val resources = mContext.resources

            // Header
            val shouldShowHeader = shouldInsertHeader(position)
            mHeaderLayout.visibility = if (shouldShowHeader) View.VISIBLE else View.GONE

            if (shouldShowHeader) {
                viewModel.setHeader(resources!!, glucose.date, mDateFormat) { title, desc ->
                    mHeaderTitle.setPrecomputedText(title, viewModel.viewModelScope)
                    mHeaderDesc.setPrecomputedText(desc, viewModel.viewModelScope)
                }
            }

            // Content
            mTitle.setPrecomputedText("%1\$d (%2\$s)".format(glucose.value, mHourFormat.format(glucose.date)),
                viewModel.viewModelScope)

            mIcon.setImageResource(glucose.timeFrame.icon)

            mLayout.setOnClickListener { onItemClick(id) }

            val indicatorDrawable = when {
                glucose.value > 180 -> mHighIndicator
                glucose.value < 70 -> mLowIndicator
                else -> null
            }

            if (indicatorDrawable == null) {
                mIndicator.visibility = View.GONE
            } else {
                mIndicator.setImageDrawable(indicatorDrawable)
                mIndicator.visibility = View.VISIBLE
            }

            // Optional - Insulin 0
            val uids = longArrayOf(glucose.insulinId0, glucose.insulinId1)
            if (uids[0] < 0 || mContext !is MainActivity) {
                mSummary.visibility = View.GONE
                return
            }

            bindInsulins(glucose, uids)
        }

        fun clear() {
            itemView.visibility = View.GONE
        }

        private fun bindInsulins(glucose: Glucose, uids: LongArray) {
            val builder = StringBuilder()

            builder.append(glucose.insulinValue0)
                .append(" ")
                .append(viewModel.getInsulin(uids[0]).name)

            // Optional - Insulin 1
            if (uids[1] >= 0) {
                builder.append(", ")
                    .append(glucose.insulinValue1)
                    .append(" ")
                    .append(viewModel.getInsulin(uids[1]).name)
            }

            mSummary.setPrecomputedText(builder.toString(), viewModel.viewModelScope)
            mSummary.visibility = View.VISIBLE
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
