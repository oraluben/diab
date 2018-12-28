/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import it.diab.R
import it.diab.db.entities.Glucose

class EatBar : AppCompatSeekBar {
    private var currentColor: Int
    private lateinit var coloredProgressDrawable: Drawable

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        max = BAR_COLORS.size - 1
        progress = 1

        currentColor = ContextCompat.getColor(context, R.color.eat_bar_medium)

        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                recolor(BAR_COLORS[progress])
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    @Glucose.CREATOR.EatLevel
    override fun getProgress(): Int {
        return super.getProgress()
    }

    private fun recolor(@ColorRes color: Int) {
        if (!::coloredProgressDrawable.isInitialized && progressDrawable is LayerDrawable) {
            coloredProgressDrawable = (progressDrawable as LayerDrawable)
                    .findDrawableByLayerId(android.R.id.progress)
        }

        val tintColor = ContextCompat.getColor(context, color)
        val animator = ValueAnimator.ofArgb(currentColor, tintColor)
        animator.addUpdateListener { value ->
            val animColor = value.animatedValue as Int

            coloredProgressDrawable.setColorFilter(animColor, PorterDuff.Mode.SRC_IN)
            thumb?.setColorFilter(animColor, PorterDuff.Mode.SRC_IN)
        }
        animator.start()

        currentColor = tintColor
    }

    companion object {
        @ColorRes
        private val BAR_COLORS = arrayOf(
                R.color.eat_bar_low,
                R.color.eat_bar_medium,
                R.color.eat_bar_high,
                R.color.eat_bar_max
        )
    }
}