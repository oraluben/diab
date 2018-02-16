package it.diab.insulin

import android.content.Context
import android.content.Intent
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.diab.R
import it.diab.db.entities.Insulin
import it.diab.insulin.editor.EditorActivity
import it.diab.ui.recyclerview.ViewHolderExt
import java.util.*

class InsulinAdapter(private val mContext: Context, list: List<Insulin>?):
        RecyclerView.Adapter<InsulinAdapter.InsulinHolder>() {
    private var mList = list ?: ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinHolder {
        return InsulinHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_insulin, parent, false))
    }

    override fun onBindViewHolder(holder: InsulinHolder, position: Int) {
        if (position == mList.size) {
            holder.onBind(mContext)
        } else {
            holder.onBind(mContext, mList[position])
        }
    }

    override fun getItemCount() = mList.size + 1

    fun updateList(list: List<Insulin>) {
        val result = DiffUtil.calculateDiff(InsulinDiff(list))
        mList = list
        result.dispatchUpdatesTo(this)
    }

    class InsulinHolder(view: View): ViewHolderExt(view) {
        private val mView: View = view.findViewById(R.id.item_insulin_view)
        private val mAddView: View = view.findViewById(R.id.item_insulin_add)
        private val mTitle: TextView = view.findViewById(R.id.item_insulin_name)
        private val mSummary: TextView = view.findViewById(R.id.item_insulin_time_zone)

        fun onBind(context: Context, insulin: Insulin) {
            id = insulin.uid

            mTitle.text = insulin.name
            mSummary.text = context.getString(insulin.timeFrame.string)

            mView.setOnClickListener { _ ->
                val intent = Intent(context, EditorActivity::class.java)
                intent.putExtra(EditorActivity.EXTRA_UID, insulin.uid)
                context.startActivity(intent)
            }
        }

        fun onBind(context: Context) {
            mView.visibility = View.GONE
            mAddView.visibility = View.VISIBLE
            mAddView.setOnClickListener { _ ->
                context.startActivity(Intent(context, EditorActivity::class.java)) }
        }
    }

    private inner class InsulinDiff(private val mNew: List<Insulin>): DiffUtil.Callback() {
        private val mOld: List<Insulin>? = mList

        override fun getOldListSize(): Int {
            return mOld?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return mNew.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOld!![oldItemPosition].uid == mNew[newItemPosition].uid
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return mOld!![oldItemPosition] == mNew[newItemPosition]
        }
    }
} 
