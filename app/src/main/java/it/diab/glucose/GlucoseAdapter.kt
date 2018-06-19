package it.diab.glucose

import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedListAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.recyclerview.ViewHolderExt
import it.diab.util.UIUtils
import it.diab.util.extensions.diff
import it.diab.util.extensions.getHeader
import java.text.SimpleDateFormat
import java.util.*

class GlucoseAdapter(private val mContext: Context, private val onItemClick: (Long) -> Unit) :
        PagedListAdapter<Glucose, GlucoseAdapter.GlucoseHolder>(CALLBACK) {

    private lateinit var mActivityViewModel: GlucoseViewModel

    // Store the these for better performance
    private val mLowIndicator = getIndicator(R.color.glucose_indicator_low)
    private val mHighIndicator = getIndicator(R.color.glucose_indicator_high)
    private val mHourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val mDateFormat = SimpleDateFormat(mContext.getString(
            R.string.time_day_month_short_format), Locale.getDefault())
    private val mToday = Date()

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
        private val mLayout = view.findViewById<RelativeLayout>(R.id.item_glucose_layout)
        private val mIcon = view.findViewById<ImageView>(R.id.item_glucose_timezone)
        private val mTitle = view.findViewById<TextView>(R.id.item_glucose_value)
        private val mSummary = view.findViewById<TextView>(R.id.item_glucose_insulin)
        private val mIndicator = view.findViewById<ImageView>(R.id.item_glucose_status)

        private val mHeaderLayout = view.findViewById<RelativeLayout>(R.id.item_glucose_header)
        private val mHeaderTitle = view.findViewById<TextView>(R.id.item_glucose_header_title)
        private val mHeaderDesc = view.findViewById<TextView>(R.id.item_glucose_header_description)

        fun onBind(glucose: Glucose, position: Int) {
            id = glucose.uid

            val resources = mContext.resources

            // Header
            val shouldShowHeader = shouldInsertHeader(position)
            mHeaderLayout.visibility = if (shouldShowHeader) View.VISIBLE else View.GONE

            if (shouldShowHeader) {
                val headerContent = glucose.date.getHeader(resources!!, mToday, mDateFormat)
                mHeaderTitle.text = headerContent.first
                mHeaderDesc.text = headerContent.second
            }

            // Content
            mTitle.text = String.format(Locale.getDefault(), "%1\$d (%2\$s)",
                    glucose.value, mHourFormat.format(glucose.date))
            mIcon.setImageResource(glucose.timeFrame.icon)

            mLayout.setOnClickListener { _ -> onItemClick(id) }

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

            if (!::mActivityViewModel.isInitialized) {
                mActivityViewModel = ViewModelProviders.of(mContext)[GlucoseViewModel::class.java]
            }

            val builder = StringBuilder()

            builder.append(glucose.insulinValue0)
                    .append(" ")
                    .append(mActivityViewModel.getInsulin(uids[0]).name)

            // Optional - Insulin 1
            if (uids[1] >= 0) {
                builder.append(", ")
                        .append(glucose.insulinValue1)
                        .append(" ")
                        .append(mActivityViewModel.getInsulin(uids[1]).name)
            }

            mSummary.text = builder.toString()
            mSummary.visibility = View.VISIBLE
        }

        fun clear() {
            mHeaderLayout.visibility = View.GONE
            mLayout.visibility = View.GONE
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
