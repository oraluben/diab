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
import it.diab.data.entities.GlucoseWithInsulin
import it.diab.glucose.util.extensions.getColorAttr
import it.diab.ui.util.extensions.setPreText
import it.diab.util.extensions.inSpans

class GlucoseHolder(
    view: View,
    private val callbacks: GlucoseHolderCallbacks
) : MainHolder(view) {
    private val titleView = view.findViewById<AppCompatTextView>(R.id.item_glucose_info)
    private val indicatorView = view.findViewById<ImageView>(R.id.item_glucose_status)

    fun onBind(item: GlucoseWithInsulin) {
        itemView.visibility = View.VISIBLE

        titleView.setPreText(buildText(item))

        setupValueIndicator(item.glucose)
        itemView.setOnClickListener { callbacks.onClick(item.glucose.uid) }
    }

    fun onLoading() {
        itemView.visibility = View.INVISIBLE
        itemView.setOnClickListener { }
    }

    private fun buildText(item: GlucoseWithInsulin) = SpannableStringBuilder().apply {

        val glucoseInfo = buildGlucoseInfo(item.glucose)
        inSpans(RelativeSizeSpan(1.2f)) {
            append(glucoseInfo)
        }

        val insulinInfo = buildInsulinInfo(item) ?: return@apply
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

    private fun buildInsulinInfo(item: GlucoseWithInsulin): String? {
        val builder = StringBuilder()
        val insulin = item.insulin
        val basal = item.basal

        if (item.insulin == null && basal == null) {
            return null
        }

        builder.apply {
            append('\n')
            if (insulin != null) {
                append(insulin.getDisplayedString(item.glucose.insulinValue0))
            }

            if (basal != null) {
                if (insulin != null) {
                    append(", ")
                }

                append(basal.getDisplayedString(item.glucose.insulinValue1))
            }
        }

        return builder.toString()
    }
}