package it.diab.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.widget.SeekBar
import it.diab.R
import it.diab.db.entities.Glucose

class EatBar(context: Context, attrs: AttributeSet) : AppCompatSeekBar(context, attrs) {
    private var mCurrentColor = ContextCompat.getColor(context, R.color.eat_bar_medium)
    private var mProgressDrawable: Drawable? = null

    init {
        max = BAR_COLORS.size - 1
        progress = 1

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
        if (mProgressDrawable == null && progressDrawable is LayerDrawable) {
            mProgressDrawable = (progressDrawable as LayerDrawable)
                    .findDrawableByLayerId(android.R.id.progress)
        }

        val tintColor = ContextCompat.getColor(context, color)
        val animator = ValueAnimator.ofArgb(mCurrentColor, tintColor)
        animator.addUpdateListener { value ->
            val animColor = value.animatedValue as Int

            mProgressDrawable?.setColorFilter(animColor, PorterDuff.Mode.SRC_IN)
            thumb?.setColorFilter(animColor, PorterDuff.Mode.SRC_IN)
        }
        animator.start()

        mCurrentColor = tintColor
    }

    companion object {
        @ColorRes
        private val BAR_COLORS : Array<Int> = arrayOf(
                R.color.eat_bar_low,
                R.color.eat_bar_medium,
                R.color.eat_bar_high,
                R.color.eat_bar_max
        )
    }
}