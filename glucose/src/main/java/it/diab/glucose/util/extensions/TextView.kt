/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.util.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import it.diab.glucose.R

fun TextView.animateThreeDots(): Animator {
    val textLength = text.length
    val spannable = SpannableString("$text...")

    val animator = ValueAnimator.ofInt(0, 4).apply {
        repeatCount = ValueAnimator.INFINITE
        duration = 1000
        addUpdateListener {
            val count = it.animatedValue as Int
            if (count < 4) {
                spannable.run {
                    getSpans(
                        textLength,
                        textLength + 3,
                        ForegroundColorSpan::class.java
                    ).forEach(this::removeSpan)
                    setSpan(
                        ForegroundColorSpan(Color.TRANSPARENT),
                        textLength + count,
                        textLength + 3,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                setText(spannable, TextView.BufferType.SPANNABLE)
            }
        }
    }
    animator.start()
    return animator
}

fun TextView.setErrorStatus(context: Context, toError: Boolean) {
    val originalColor = ContextCompat.getColor(context, R.color.colorAccent)
    val errorColor = ContextCompat.getColor(context, R.color.action_dangerous)

    val animator = ValueAnimator.ofArgb(
        if (toError) originalColor else errorColor,
        if (toError) errorColor else originalColor
    )

    val drawable = compoundDrawables[0]

    animator.addUpdateListener { animation ->
        drawable.setColorFilter(animation.animatedValue as Int, PorterDuff.Mode.SRC_ATOP)
    }

    animator.start()
}

fun TextView.setTextErrorStatus(context: Context, toError: Boolean) {
    val originalColor = context.getColorAttr(R.style.DiabTheme, android.R.attr.textColorPrimary)
    val errorColor = ContextCompat.getColor(context, R.color.action_dangerous)

    val animator = ValueAnimator.ofArgb(
        if (toError) originalColor else errorColor,
        if (toError) errorColor else originalColor
    )

    animator.addUpdateListener { animation -> setTextColor(animation.animatedValue as Int) }
    animator.start()
}
