package it.diab.glucose

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.recyclerview.ViewHolderExt
import it.diab.util.extensions.asTimeFrame
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
        holder.onBind(mList!![position])
    }

    override fun getItemCount() = mList?.size ?: 0

    fun updateList(list: List<Glucose>?) {
        val result = DiffUtil.calculateDiff(GlucoseDiff(list ?: emptyList()))
        mList = list
        result.dispatchUpdatesTo(this)
    }

    inner class GlucoseHolder(private val mView: View) : ViewHolderExt(mView) {
        private val mIcon: ImageView = mView.findViewById(R.id.item_glucose_timezone)
        private val mTitle: TextView = mView.findViewById(R.id.item_glucose_value)
        private val mSummary: TextView = mView.findViewById(R.id.item_glucose_insulin)
        private val mHeavyIndicator: ImageView = mView.findViewById(R.id.item_glucose_heavy_meal)

        fun onBind(glucose: Glucose) {
            id = glucose.uid

            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            mTitle.text = String.format(Locale.getDefault(), "%1\$d (%2\$s)",
                    glucose.value, dateFormat.format(glucose.date))
            mIcon.setImageResource(glucose.date.asTimeFrame().icon)
            mHeavyIndicator.visibility = if (glucose.isHeavyMeal) View.VISIBLE else View.GONE

            mView.setOnClickListener { _ -> onItemClick(id) }

            // Optional - Insulin 0
            val uids = longArrayOf(glucose.insulinId0, glucose.insulinId1)
            if (uids[0] < 0 || mContext !is MainActivity) {
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
