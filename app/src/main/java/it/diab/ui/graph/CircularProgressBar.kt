package it.diab.ui.graph

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import it.diab.R
import it.diab.util.extensions.getColorAttr
import it.diab.util.extensions.getFloatAttr
import it.diab.util.extensions.getStringAttr

class CircularProgressBar : View {

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mViewSize = 0
    private var mSweepAngle = 0f

    private var mProgressColorNormal = Color.BLACK
    private var mProgressColorHigh = Color.BLACK
    private var mProgressColorLow = Color.BLACK
    private var mBackgroundColor = Color.TRANSPARENT
    private var mTextColor = Color.BLACK

    private var mValue = 0.toDouble()
    private var mTresholdHigh = Double.MAX_VALUE
    private var mTresholdLow = Double.MIN_VALUE
    private var mTresholdMax = Double.MAX_VALUE

    private var mTextFormat = ""

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style) {
        initAttrs(context, attrs)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mViewSize = width

        drawOutlineArc(canvas)
        drawText(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    fun setProgress(progress: Int) {
        mValue = progress.toDouble()

        // Animate only the first time
        if (mSweepAngle != 0f) {
            mSweepAngle =
                    calcSweepAngleFromProgress(if (progress < 1) 100 else (progress * 100))
            return
        }

        mSweepAngle = 0f

        val animator = ValueAnimator.ofFloat(mSweepAngle,
                calcSweepAngleFromProgress(if (progress < 1) 100 else (progress * 100)))
        animator.interpolator = FastOutSlowInInterpolator()
        animator.duration = 1600
        animator.startDelay = 300
        animator.addUpdateListener { valueAnimator ->
            mSweepAngle = valueAnimator.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    fun setTreshold(high: Int, low: Int) {
        mTresholdHigh = high.toDouble()
        mTresholdLow = low.toDouble()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }

        val styleable = R.styleable.CircularProgressBar
        mTresholdMax = context.getFloatAttr(attrs,
                R.styleable.CircularProgressBar_cpb_maxValue, styleable, 100f).toDouble()
        mTextFormat = context.getStringAttr(attrs,
                R.styleable.CircularProgressBar_cpb_textFormat, styleable, "%.2f")
        mTextColor = context.getColorAttr(attrs,
                R.styleable.CircularProgressBar_cpb_textColor, styleable)
        mProgressColorHigh = context.getColorAttr(attrs,
                R.styleable.CircularProgressBar_cpb_progressColorHigh, styleable)
        mProgressColorNormal = context.getColorAttr(attrs,
                R.styleable.CircularProgressBar_cpb_progressColorNormal, styleable)
        mProgressColorLow = context.getColorAttr(attrs,
                R.styleable.CircularProgressBar_cpb_progressColorLow, styleable)
        mBackgroundColor = context.getColorAttr(attrs,
                R.styleable.CircularProgressBar_cpb_backgroundColor, styleable, Color.TRANSPARENT)
    }

    private fun calcSweepAngleFromProgress(progress: Int) =
            (360 * progress / mTresholdMax).toFloat()

    private fun drawOutlineArc(canvas: Canvas) {
        val diameter = mViewSize - 48
        val outerOval = RectF(24f, 24f, diameter.toFloat(), diameter.toFloat())

        mPaint.strokeWidth = 32f
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.style = Paint.Style.STROKE
        mPaint.color = mBackgroundColor
        canvas.drawArc(outerOval, 0f, 360f, false, mPaint)

        mPaint.color = when {
            mValue < mTresholdLow -> mProgressColorLow
            mValue > mTresholdHigh -> mProgressColorHigh
            else -> mProgressColorNormal
        }
        canvas.drawArc(outerOval, 0f, mSweepAngle, false, mPaint)
    }

    private fun drawText(canvas: Canvas) {
        mPaint.textSize = mViewSize / 5f
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.strokeWidth = 0f
        mPaint.color = mTextColor

        // Center text
        val posX = canvas.width / 2
        val posY = (canvas.height / 2 - (mPaint.descent() + mPaint.ascent()) / 2).toInt()
        canvas.drawText(mTextFormat.format(mValue), posX.toFloat(), posY.toFloat(), mPaint)
    }
}