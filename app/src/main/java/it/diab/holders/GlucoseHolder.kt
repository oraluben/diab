/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.holders

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import it.diab.R
import it.diab.data.entities.Glucose
import it.diab.glucose.util.extensions.getColorAttr
import it.diab.ui.util.extensions.setPreText
import it.diab.util.extensions.inSpans

class GlucoseHolder(
    view: View,
    private val callbacks: GlucoseHolderCallbacks
) : MainHolder(view) {
    private val titleView = view.findViewById<AppCompatTextView>(R.id.item_glucose_info)
    private val indicatorView = view.findViewById<ImageView>(R.id.item_glucose_status)

    fun onBind(glucose: Glucose) {
        itemView.visibility = View.VISIBLE

        titleView.setPreText(buildText(glucose))

        setupValueIndicator(glucose)
        itemView.setOnClickListener { callbacks.onClick(glucose.uid) }
    }

    fun onLoading() {
        itemView.visibility = View.INVISIBLE
        itemView.setOnClickListener { }
    }

    private fun buildText(glucose: Glucose) = SpannableStringBuilder().apply {

        val glucoseInfo = buildGlucoseInfo(glucose)
        inSpans(RelativeSizeSpan(1.2f)) {
            append(glucoseInfo)
        }

        val insulinInfo = buildInsulinInfo(glucose) ?: return@apply
        val insulinColor = itemView.context.getColorAttr(R.style.AppTheme, android.R.attr.textColorSecondary)
        inSpans(ForegroundColorSpan(insulinColor)) {
            append(insulinInfo)
        }
    }

    private fun setupValueIndicator(glucose: Glucose) {
        val indicatorDrawable = callbacks.getIndicator(glucose.value)
        if (indicatorDrawable == null) {
            indicatorView.visibility = View.GONE
        } else {
            indicatorView.visibility = View.VISIBLE
            indicatorView.setImageDrawable(indicatorDrawable)
        }
    }

    private fun buildGlucoseInfo(glucose: Glucose): String {
        return "${glucose.value} (${callbacks.fetchHourText(glucose.date)})"
    }

    private fun buildInsulinInfo(glucose: Glucose): String? {
        val builder = StringBuilder()
        val insulinId = glucose.insulinId0
        val basalId = glucose.insulinId1

        if (insulinId < 0 && basalId < 0) {
            return null
        }

        builder.apply {
            append('\n')
            if (insulinId >= 0) {
                append(glucose.insulinValue0)
                append(' ')
                append(callbacks.getInsulinName(insulinId))
            }

            if (basalId >= 0) {
                if (insulinId >= 0) {
                    append(", ")
                }

                append(glucose.insulinValue1)
                append(' ')
                append(callbacks.getInsulinName(basalId))
            }
        }

        return builder.toString()
    }
}