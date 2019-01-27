/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.diab.R
import it.diab.core.data.entities.Insulin
import it.diab.core.util.event.Event

class InsulinAdapter : PagedListAdapter<Insulin, InsulinAdapter.InsulinHolder>(CALLBACK) {

    private val _editInsulin = MutableLiveData<Event<Long>>()
    internal val editInsulin: LiveData<Event<Long>> = _editInsulin

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinHolder {
        return InsulinHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_insulin, parent, false)
        )
    }

    override fun getItemCount() = super.getItemCount() + 1

    override fun onBindViewHolder(holder: InsulinHolder, position: Int) {
        if (position == itemCount - 1) {
            // Last item: add new insulin
            holder.onBind()
            return
        }

        val item = getItem(position)
        if (item == null) {
            holder.clear()
        } else {
            holder.onBind(item)
        }
    }

    inner class InsulinHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.item_insulin_name)
        private val icon: ImageView = view.findViewById(R.id.item_insulin_icon)

        fun onBind(insulin: Insulin) {
            title.text = insulin.name
            icon.setImageResource(insulin.timeFrame.icon)

            itemView.setOnClickListener { _editInsulin.value = Event(insulin.uid) }
        }

        fun onBind() {
            val res = itemView.resources
            title.text = res.getString(R.string.insulin_add_item)
            icon.setImageResource(R.drawable.ic_add)

            itemView.setOnClickListener { _editInsulin.value = Event(-1) }
        }

        fun clear() {
            itemView.visibility = View.GONE
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
