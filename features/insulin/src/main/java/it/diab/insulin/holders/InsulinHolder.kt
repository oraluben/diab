/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.holders

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import it.diab.core.arch.EventBusFactory
import it.diab.insulin.R
import it.diab.insulin.components.status.ListItemStatus
import it.diab.insulin.events.ListEvent
import it.diab.ui.util.extensions.setPreText

internal class InsulinHolder(
    view: View,
    private val bus: EventBusFactory
) : RecyclerView.ViewHolder(view) {
    private val titleView = view.findViewById<AppCompatTextView>(R.id.item_insulin_name)
    private val iconView = view.findViewById<ImageView>(R.id.item_insulin_icon)

    fun onBind(status: ListItemStatus) {
        titleView.setPreText(status.name)
        iconView.setImageResource(status.icon)

        itemView.setOnClickListener {
            bus.emit(ListEvent::class, ListEvent.ClickEvent(status.uid))
        }
    }

    fun onBind() {
        val res = itemView.resources
        titleView.setPreText(res.getString(R.string.insulin_add_item))
        iconView.setImageResource(R.drawable.ic_add)

        itemView.setOnClickListener {
            bus.emit(ListEvent::class, ListEvent.ClickEvent(-1))
        }
    }

    fun onLoading() {
        itemView.visibility = View.GONE
    }
}