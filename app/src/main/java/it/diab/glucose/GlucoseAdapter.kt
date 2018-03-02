package it.diab.glucose

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.recyclerview.ViewHolderExt
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.diff
import it.diab.util.extensions.getHeader
import java.text.SimpleDateFormat
import java.util.*

class GlucoseAdapter(private val mContext: Context?, list: List<Glucose>?,
                     private val onItemClick: (Long) -> Unit) :
        RecyclerView.Adapter<GlucoseAdapter.GlucoseHolder>() {
    private var mList: List<Glucose>? = list ?: emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            GlucoseHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_glucose, parent, false))

    override fun onBindViewHolder(holder: GlucoseHolder, position: Int) {
        holder.onBind(mList!![position], position)
    }

    override fun getItemCount() = mList?.size ?: 0

    fun updateList(list: List<Glucose>?) {
        val result = DiffUtil.calculateDiff(GlucoseDiff(list ?: emptyList()))
        mList = list
        result.dispatchUpdatesTo(this)
    }

    private fun shouldInsertHeader(position: Int): Boolean {
        if (position == 0) {
            return false
        }

        if (mList == null) {
            return false
        }

        val a = mList!![position - 1].date
        val b = mList!![position].date

        return b.diff(Date()) != 0 && a.diff(b) > 0
    }

    inner class GlucoseHolder(view: View) : ViewHolderExt(view) {
        private val mLayout = view.findViewById<RelativeLayout>(R.id.item_glucose_layout)
        private val mIcon = view.findViewById<ImageView>(R.id.item_glucose_timezone)
        private val mTitle = view.findViewById<TextView>(R.id.item_glucose_value)
        private val mSummary = view.findViewById<TextView>(R.id.item_glucose_insulin)

        private val mHeaderLayout = view.findViewById<LinearLayout>(R.id.item_glucose_header)
        private val mHeaderTitle = view.findViewById<TextView>(R.id.item_glucose_header_title)
        private val mHeaderDesc = view.findViewById<TextView>(R.id.item_glucose_header_description)


        fun onBind(glucose: Glucose, position: Int) {
            id = glucose.uid

            // Header
            val shouldShowHeader = shouldInsertHeader(position)
            mHeaderLayout.visibility = if (shouldShowHeader) View.VISIBLE else View.GONE

            if (shouldShowHeader && mContext != null) {
                val headerContent = glucose.date.getHeader(mContext.resources)
                mHeaderTitle.text = headerContent.first
                mHeaderDesc.text = headerContent.second
            }

            // Content
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            mTitle.text = String.format(Locale.getDefault(), "%1\$d (%2\$s)",
                    glucose.value, dateFormat.format(glucose.date))
            mIcon.setImageResource(glucose.date.asTimeFrame().icon)

            mLayout.setOnClickListener { _ -> onItemClick(id) }
            mLayout.setBackgroundColor(ContextCompat.getColor(mContext!!, when {
                glucose.value > 180 -> R.color.glucose_background_high
                glucose.value < 70 -> R.color.glucose_background_low
                else -> android.R.color.transparent
            }))

            // Optional - Insulin 0
            val uids = longArrayOf(glucose.insulinId0, glucose.insulinId1)
            if (uids[0] < 0 || mContext !is MainActivity) {
                mSummary.visibility = View.GONE
                return
            }

            val viewModel = ViewModelProviders.of(mContext).get(GlucoseViewModel::class.java)
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

            mSummary.text = builder.toString()
            mSummary.visibility = View.VISIBLE
        }
    }

    inner class GlucoseDiff(private val mNew: List<Glucose>) : DiffUtil.Callback() {
        private val mOld = mList

        override fun getOldListSize() = mOld?.size ?: 0

        override fun getNewListSize() = mNew.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            mOld!![oldItemPosition].uid == mNew[newItemPosition].uid

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                mOld!![oldItemPosition] == mNew[newItemPosition]
    }
}
