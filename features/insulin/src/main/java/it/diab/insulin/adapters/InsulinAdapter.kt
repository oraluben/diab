/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import it.diab.core.arch.EventBusFactory
import it.diab.data.entities.Insulin
import it.diab.insulin.R
import it.diab.insulin.components.status.ListItemStatus
import it.diab.insulin.holders.InsulinHolder

class InsulinAdapter(private val bus: EventBusFactory) : PagedListAdapter<Insulin, InsulinHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinHolder {
        return InsulinHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_insulin, parent, false),
            bus
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
            holder.onLoading()
        } else {
            holder.onBind(
                ListItemStatus(
                    item.uid,
                    item.name,
                    item.timeFrame.icon
                )
            )
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
