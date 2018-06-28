package it.diab.ui

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.insulin.ml.InsulinSuggestionTask
import it.diab.util.VibrationUtil
import it.diab.util.timeFrame.TimeFrame
import kotlin.math.roundToInt

class InsulinSuggestionView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val mCardView: CardView
    private val mProgressView: ProgressBar
    private val mTextView: TextView

    private lateinit var mTask: InsulinSuggestionTask

    private var mOnSuggestionApply: (Float, Insulin) -> Unit = { _,_ -> }
    private var mIsEnabled = false

    private lateinit var mGlucose: Glucose
    private lateinit var mInsulin: Insulin

    init {
        View.inflate(context, R.layout.component_insulin_suggestion, this)
        mCardView = findViewById(R.id.insulin_suggestion_card)
        mProgressView = findViewById(R.id.insulin_suggestion_progress)
        mTextView = findViewById(R.id.insulin_suggestion_text)
    }

    fun bind(glucose: Glucose, insulin: Insulin, onSuggestionApply: (Float, Insulin) -> Unit) {
        if (::mGlucose.isInitialized) {
            return
        }

        mGlucose = glucose
        mInsulin = insulin
        mOnSuggestionApply = onSuggestionApply
        mTask = InsulinSuggestionTask(resources, this::onSuggestionLoaded)

        setup()
        runTask()
    }

    private fun setup() {
        val allowedTimeFrames = arrayOf(TimeFrame.MORNING, TimeFrame.LUNCH, TimeFrame.DINNER)
        val timeFrame = mGlucose.timeFrame

        mIsEnabled = allowedTimeFrames.indexOf(timeFrame) != -1 && mGlucose.insulinValue0 == 0f
        visibility = if (mIsEnabled) View.VISIBLE else View.GONE
    }

    private fun runTask(): Boolean {
        if (!mIsEnabled || mTask.status == AsyncTask.Status.RUNNING) {
            return false
        }

        mProgressView.visibility = View.VISIBLE
        mTextView.text = resources.getString(R.string.insulin_suggestion_loading)

        mTask.execute(mGlucose)
        return true
    }

    private fun onSuggestionLoaded(result: Float) {
        mProgressView.visibility = View.GONE

        if (result < 0) {
            if (result == InsulinSuggestionTask.NO_MODEL) {
                visibility = View.GONE
                return
            }


            mTextView.text = resources.getString(when (result) {
                InsulinSuggestionTask.TOO_HIGH -> R.string.insulin_suggestion_warning_high
                InsulinSuggestionTask.TOO_LOW -> R.string.insulin_suggestion_warning_low
                else -> R.string.insulin_suggestion_error
            })

            val errorColor = ContextCompat.getColor(context, R.color.action_dangerous)
            mCardView.setCardBackgroundColor(errorColor)
            return
        }

        // Round to 0.5
        val formattedResult = if (mInsulin.hasHalfUnits)
            (result * 2).roundToInt() / 2f
        else
            result.roundToInt().toFloat()
        mTextView.text = resources.getString(R.string.insulin_suggestion_value, formattedResult)

        mCardView.setOnClickListener {
            VibrationUtil.vibrateForImportantClick(it)
            mOnSuggestionApply(formattedResult, mInsulin)
            Handler().postDelayed(this::onSuggestionApplied, 350)
        }
    }

    private fun onSuggestionApplied() {
        mIsEnabled = false
        animate().alpha(0f)
                .withEndAction { mCardView.visibility = View.GONE }
                .start()
    }
}
