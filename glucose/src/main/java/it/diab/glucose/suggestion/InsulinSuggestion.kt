/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.suggestion

import android.content.res.Resources
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.Insulin
import it.diab.core.data.entities.TimeFrame
import it.diab.core.util.PluginManager
import it.diab.glucose.R
import kotlin.math.roundToInt

class InsulinSuggestion(
    private val glucose: Glucose,
    private val proposedInsulin: Insulin,
    private val onSuggestionApplied: (value: Float, insulin: Insulin) -> Unit
) : SuggestionCallback<Float>, SuggestionConfig {

    override val shouldAnimate = true

    override val isValid by lazy {
        val allowedTimeFrames = arrayOf(TimeFrame.MORNING, TimeFrame.LUNCH, TimeFrame.DINNER)
        allowedTimeFrames.contains(glucose.timeFrame) && glucose.insulinValue0 == 0f
    }

    override fun validate(value: Float) = value >= 0

    override fun getFailMessage(value: Float, res: Resources?): String? {
        if (res == null) {
            return null
        }

        return when (value) {
            PluginManager.TOO_HIGH -> res.getString(R.string.insulin_suggestion_warning_high)
            PluginManager.TOO_LOW -> res.getString(R.string.insulin_suggestion_warning_low)
            PluginManager.NO_MODEL -> null
            else -> res.getString(R.string.insulin_suggestion_error)
        }
    }

    override fun getSuccessMessage(value: Float, res: Resources?): String {
        return res?.getString(R.string.insulin_suggestion_value, value.roundToInsulin()) ?: "???"
    }

    override fun onSuggestionApply(value: Float) {
        onSuggestionApplied(value.roundToInsulin(), proposedInsulin)
    }

    private fun Float.roundToInsulin() = if (proposedInsulin.hasHalfUnits)
        (this * 2).roundToInt() / 2f
    else
        roundToInt().toFloat()
}