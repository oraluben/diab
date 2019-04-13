/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.diab.R
import it.diab.core.data.entities.Insulin

class InsulinHolder(
    view: View,
    private val callbacks: InsulinHolderCallbacks
) : RecyclerView.ViewHolder(view) {
    private val titleView = view.findViewById<TextView>(R.id.item_insulin_name)
    private val iconView = view.findViewById<ImageView>(R.id.item_insulin_icon)

    fun onBind(insulin: Insulin) {
        titleView.text = insulin.name
        iconView.setImageResource(insulin.timeFrame.icon)

        itemView.setOnClickListener { callbacks.onClick(insulin.uid) }
    }

    fun onBind() {
        val res = itemView.resources
        titleView.text = res.getString(R.string.insulin_add_item)
        iconView.setImageResource(R.drawable.ic_add)

        itemView.setOnClickListener { callbacks.onClick(-1) }
    }

    fun onLoading() {
        itemView.visibility = View.GONE
    }
}