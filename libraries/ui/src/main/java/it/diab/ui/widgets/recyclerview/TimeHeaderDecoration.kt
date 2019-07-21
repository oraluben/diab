/*
 * Copyright 2018 Google LLC
 * Copyright 2019 Bevilacqua Joey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.diab.ui.widgets.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import androidx.recyclerview.widget.RecyclerView
import it.diab.core.time.DateTime
import it.diab.core.time.DateTimeFormatter
import it.diab.ui.R
import it.diab.ui.util.extensions.inSpans
import it.diab.ui.util.extensions.withTranslation

/**
 * A [RecyclerView.ItemDecoration] which draws sticky headers for a given list of sessions.
 */
class TimeHeaderDecoration(
    context: Context,
    data: List<DateTime>,
    private val shiftList: Int = 0
) : RecyclerView.ItemDecoration() {

    private val paint: TextPaint
    private val width: Int
    private val paddingTop: Int
    private val monthTextSize: Int
    private val dayFormatter = DateTimeFormatter("dd")
    private val monthFormatter = DateTimeFormatter("MMM yyyy")

    init {
        val attrs = context.obtainStyledAttributes(
            R.style.AppTheme_TimeHeaders,
            R.styleable.TimeHeader
        )

        paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = attrs.getColor(R.styleable.TimeHeader_android_textColor, Color.BLACK)
            textSize = attrs.getDimension(R.styleable.TimeHeader_dayTextSize, 0f)
        }

        width = attrs.getDimensionPixelSize(R.styleable.TimeHeader_android_width, 0)
        paddingTop = attrs.getDimensionPixelSize(R.styleable.TimeHeader_android_paddingTop, 0)
        monthTextSize = attrs.getDimensionPixelSize(R.styleable.TimeHeader_monthTextSize, 0)

        attrs.recycle()
    }

    private val daySlots: Map<Int, StaticLayout> =
        data.mapIndexed { index, date ->
            index + shiftList to date
        }.distinctBy {
            val dateTime = it.second
            (dateTime[DateTime.YEAR] * 1000) + dateTime[DateTime.DAY_OF_YEAR]
        }.map {
            it.first to createHeader(it.second)
        }.toMap()

    /**
     * Loop over each child and draw any corresponding headers.
     * We also look back to see if there are any headers _before_ the first header we
     * found i.e. which needs to be sticky.
     */
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (daySlots.isEmpty() || parent.childCount == 0) {
            return
        }

        var earliestFoundHeaderPos = -1
        var prevHeaderTop = Int.MAX_VALUE

        for (i in parent.childCount - 1 downTo 0) {
            val view = parent.getChildAt(i) ?: continue
            val viewTop = view.top + view.translationY.toInt()
            if (view.bottom > 0 && viewTop < parent.height) {
                val position = parent.getChildAdapterPosition(view)
                daySlots[position]?.let { layout ->
                    paint.alpha = (view.alpha * 255).toInt()
                    val top = (viewTop + paddingTop)
                        .coerceAtLeast(paddingTop)
                        .coerceAtMost(prevHeaderTop - layout.height)
                    c.withTranslation(y = top.toFloat()) {
                        layout.draw(c)
                    }
                    earliestFoundHeaderPos = position
                    prevHeaderTop = viewTop
                }
            }
        }

        // If no headers found, ensure header of the first shown item is drawn.
        if (earliestFoundHeaderPos < 0) {
            earliestFoundHeaderPos = parent.getChildAdapterPosition(parent.getChildAt(0)) + 1
        }

        // Look back over headers to see if a prior item should be drawn sticky.
        for (headerPos in daySlots.keys.reversed()) {
            if (headerPos < earliestFoundHeaderPos) {
                daySlots[headerPos]?.let {
                    val top = (prevHeaderTop - it.height).coerceAtMost(paddingTop)
                    c.withTranslation(y = top.toFloat()) {
                        it.draw(c)
                    }
                }
                break
            }
        }
    }

    /**
     * Create a header layout for the given [date].
     */
    private fun createHeader(date: DateTime): StaticLayout {
        val text = SpannableStringBuilder().apply {
            inSpans(StyleSpan(Typeface.BOLD)) {
                append(dayFormatter.format(date))
            }
            append("\n")
            inSpans(AbsoluteSizeSpan(monthTextSize)) {
                append(monthFormatter.format(date))
            }
        }

        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
    }
}