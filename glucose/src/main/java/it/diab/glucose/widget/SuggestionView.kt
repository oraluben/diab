/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import it.diab.glucose.R
import it.diab.glucose.suggestion.SuggestionModel
import it.diab.glucose.util.VibrationUtil

class SuggestionView : LinearLayout {
    private val textView: TextView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.component_suggestion, this)
        textView = findViewById(R.id.insulin_suggestion_text)

        textView.visibility = View.GONE
    }

    /**
     * Try to apply a [SuggestionModel] to the view.
     * A config may or may not apply depending on the [SuggestionModel.isValid]
     * value.
     *
     * @param model the applied model
     * @return whether the model has been enabled
     */
    fun <T> applyConfig(model: SuggestionModel<T>): Boolean {
        if (model.isValid()) {
            textView.setCompoundDrawablesWithIntrinsicBounds(model.icon, 0, 0, 0)
            return true
        }

        textView.visibility = View.GONE
        return false
    }

    fun <T> onSuggestionLoaded(value: T, model: SuggestionModel<T>) {
        val isValid = model.validate(value)
        textView.text = if (isValid) model.getSuccessMessage(value, resources)
        else model.getFailMessage(value, resources)

        if (textView.text.isEmpty()) {
            return
        }

        textView.isEnabled = isValid

        animateIn()

        if (!isValid) {
            return
        }

        textView.setOnClickListener {
            VibrationUtil.vibrateForImportantClick(it)

            animate()
                .alpha(0f)
                .translationYBy(10f)
                .withEndAction {
                    visibility = View.GONE
                    model.onSuggestionApply(value)
                }
                .start()
        }
    }

    private fun animateIn() {
        textView.apply {
            translationY = 10f
            alpha = 0f
            visibility = View.VISIBLE

            animate().alpha(1f)
                .translationY(0f)
                .start()
        }
    }
}
