package it.diab.insulin

import android.arch.paging.PagedListAdapter
import android.content.Context
import android.content.Intent
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.diab.R
import it.diab.db.entities.Insulin
import it.diab.insulin.editor.EditorActivity
import it.diab.ui.recyclerview.ViewHolderExt

class InsulinAdapter(private val mContext: Context) :
        PagedListAdapter<Insulin, InsulinAdapter.InsulinHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinHolder {
        return InsulinHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_insulin, parent, false))
    }

    override fun onBindViewHolder(holder: InsulinHolder, position: Int) {
        if (position == itemCount - 1) {
            holder.onBind(mContext)
        } else {
            val item = getItem(position)
            if (item == null) {
                holder.clear()
            } else {
                holder.onBind(mContext, item)
            }
        }
    }

    override fun getItemCount() = super.getItemCount() + 1

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

        fun clear() {
            mView.visibility = View.GONE
            mView.setOnClickListener {  }
            mAddView.visibility = View.GONE
            mAddView.setOnClickListener {  }
        }
    }

    companion object {
        private val CALLBACK = object : DiffUtil.ItemCallback<Insulin>() {
            override fun areContentsTheSame(oldItem: Insulin, newItem: Insulin) =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: Insulin, newItem: Insulin) =
                    oldItem.uid == newItem.uid
        }
    }
}
