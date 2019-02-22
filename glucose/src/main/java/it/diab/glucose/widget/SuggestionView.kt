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
import it.diab.glucose.R
import it.diab.glucose.suggestion.SuggestionCallback
import it.diab.glucose.suggestion.SuggestionConfig
import it.diab.glucose.util.VibrationUtil
import it.diab.glucose.util.extensions.animateThreeDots

class SuggestionView : LinearLayout {
    private val textView: TextView

    private lateinit var animator: Animator

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.component_suggestion, this)
        textView = findViewById(R.id.insulin_suggestion_text)
    }

    fun setup(config: SuggestionConfig) {
        if (!config.isValid) {
            visibility = View.GONE
            return
        }

        if (config.shouldAnimate) {
            showLoad()
        }
    }

    fun <T> onSuggestionLoaded(value: T, callback: SuggestionCallback<T>) {
        if (::animator.isInitialized) {
            animator.cancel()
        }

        if (!callback.validate(value)) {
            val message = callback.getFailMessage(value, resources)
            if (message == null) {
                visibility = View.GONE
                return
            }

            textView.apply {
                text = message
                isEnabled = false
            }
            return
        }

        textView.text = callback.getSuccessMessage(value, resources)

        textView.setOnClickListener {
            VibrationUtil.vibrateForImportantClick(it)
            callback.onSuggestionApply(value)
            Handler().postDelayed(this::onSuggestionApplied, 350)
        }
    }

    private fun showLoad() {
        textView.text = resources.getString(R.string.insulin_suggestion_loading)
        animator = textView.animateThreeDots()
    }

    private fun onSuggestionApplied() {
        animate().alpha(0f)
            .withEndAction { visibility = View.GONE }
            .start()
    }
}
