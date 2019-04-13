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
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import it.diab.R
import it.diab.core.data.entities.Insulin
import it.diab.core.util.event.Event
import it.diab.holders.InsulinHolderCallbacks
import it.diab.holders.InsulinHolder

class InsulinAdapter : PagedListAdapter<Insulin, InsulinHolder>(CALLBACK), InsulinHolderCallbacks {

    private val _editInsulin = MutableLiveData<Event<Long>>()
    internal val editInsulin: LiveData<Event<Long>> = _editInsulin

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinHolder {
        return InsulinHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_insulin, parent, false),
            this
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
            holder.onBind(item)
        }
    }

    override fun onClick(uid: Long) {
        _editInsulin.value = Event(uid)
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
