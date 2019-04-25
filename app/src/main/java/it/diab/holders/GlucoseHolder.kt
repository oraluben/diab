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
import it.diab.core.data.entities.Glucose
import it.diab.core.util.extensions.setPrecomputedText

class GlucoseHolder(
    view: View,
    private val callbacks: GlucoseHolderCallbacks
) : RecyclerView.ViewHolder(view) {
    private val iconView = view.findViewById<ImageView>(R.id.item_glucose_timezone)
    private val titleView = view.findViewById<TextView>(R.id.item_glucose_value)
    private val summaryView = view.findViewById<TextView>(R.id.item_glucose_insulin)
    private val indicatorView = view.findViewById<ImageView>(R.id.item_glucose_status)

    fun onBind(glucose: Glucose) {
        itemView.visibility = View.VISIBLE

        bindValue(glucose)
        bindInsulin(glucose)

        iconView.setImageResource(glucose.timeFrame.icon)
        itemView.setOnClickListener { callbacks.onClick(glucose.uid) }
    }

    fun onLoading() {
        itemView.visibility = View.INVISIBLE
    }

    private fun bindValue(glucose: Glucose) {
        val title = "${glucose.value} (%1\$s)"

        callbacks.fetchHourText(glucose.date) { text, scope ->
            titleView.setPrecomputedText(title.format(text), scope)
        }

        val indicatorDrawable = callbacks.getIndicator(glucose.value)
        if (indicatorDrawable == null) {
            indicatorView.visibility = View.GONE
        } else {
            indicatorView.visibility = View.VISIBLE
            indicatorView.setImageDrawable(indicatorDrawable)
        }
    }

    private fun bindInsulin(glucose: Glucose) {
        val builder = StringBuilder()
        val insulinId = glucose.insulinId0
        val basalId = glucose.insulinId1

        builder.apply {
            if (insulinId >= 0) {
                append(glucose.insulinValue0)
                append(" ")
                append(callbacks.getInsulinName(insulinId))
            }

            if (basalId >= 0) {
                if (insulinId >= 0) {
                    append(", ")
                }

                append(glucose.insulinValue1)
                append(" ")
                append(callbacks.getInsulinName(basalId))
            }
        }

        if (builder.isEmpty()) {
            summaryView.visibility = View.GONE
        } else {
            summaryView.visibility = View.VISIBLE
            summaryView.text = builder.toString()
        }
    }
}