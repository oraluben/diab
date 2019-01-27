/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.widget

import android.animation.Animator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.Insulin
import it.diab.core.data.timeframe.TimeFrame
import it.diab.core.util.PluginManager
import it.diab.glucose.R
import it.diab.glucose.util.VibrationUtil
import it.diab.glucose.util.extensions.animateThreeDots
import kotlin.math.roundToInt

class InsulinSuggestionView : LinearLayout {
    private val textView: TextView

    private lateinit var animator: Animator

    private var onSuggestionApply: (Float, Insulin) -> Unit = { _, _ -> }
    private var hasSuggestions = false

    private lateinit var glucose: Glucose
    private lateinit var insulin: Insulin

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.component_insulin_suggestion, this)
        textView = findViewById(R.id.insulin_suggestion_text)
    }

    fun bind(target: Glucose, proposedInsulin: Insulin, onApplied: (Float, Insulin) -> Unit) {
        if (::glucose.isInitialized) {
            return
        }

        glucose = target
        insulin = proposedInsulin
        onSuggestionApply = onApplied

        setup()
        showLoad()
    }

    fun onSuggestionLoaded(result: Float) {
        if (::animator.isInitialized) {
            animator.cancel()
        }

        if (result < 0) {
            if (result == PluginManager.NO_MODEL) {
                visibility = View.GONE
                return
            }

            textView.text = resources.getString(
                when (result) {
                    PluginManager.TOO_HIGH -> R.string.insulin_suggestion_warning_high
                    PluginManager.TOO_LOW -> R.string.insulin_suggestion_warning_low
                    else -> R.string.insulin_suggestion_error
                }
            )

            textView.isEnabled = false
            return
        }

        // Round to 0.5
        val formattedResult = if (insulin.hasHalfUnits)
            (result * 2).roundToInt() / 2f
        else
            result.roundToInt().toFloat()
        textView.text = resources.getString(R.string.insulin_suggestion_value, formattedResult)

        textView.setOnClickListener {
            VibrationUtil.vibrateForImportantClick(it)
            onSuggestionApply(formattedResult, insulin)
            Handler().postDelayed(this::onSuggestionApplied, 350)
        }
    }

    private fun setup() {
        val allowedTimeFrames = arrayOf(TimeFrame.MORNING, TimeFrame.LUNCH, TimeFrame.DINNER)
        val timeFrame = glucose.timeFrame

        hasSuggestions = allowedTimeFrames.indexOf(timeFrame) != -1 && glucose.insulinValue0 == 0f
        visibility = if (hasSuggestions) View.VISIBLE else View.GONE
    }

    private fun showLoad() {
        if (!hasSuggestions) {
            return
        }

        textView.text = resources.getString(R.string.insulin_suggestion_loading)
        animator = textView.animateThreeDots()
    }

    private fun onSuggestionApplied() {
        hasSuggestions = false
        animate().alpha(0f)
            .withEndAction { textView.visibility = View.GONE }
            .start()
    }
}
