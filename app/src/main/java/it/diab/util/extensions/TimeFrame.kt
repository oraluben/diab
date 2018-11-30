/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.extensions

import com.google.android.gms.fitness.data.HealthFields
import it.diab.util.timeFrame.TimeFrame

fun TimeFrame.toFitMealRelation() = when (this) {
    TimeFrame.LATE_MORNING,
    TimeFrame.AFTERNOON,
    TimeFrame.NIGHT -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_AFTER_MEAL
    else -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL
}

fun TimeFrame.toFitSleepRelation() =
    if (TimeFrame.MORNING == this)
        HealthFields.TEMPORAL_RELATION_TO_SLEEP_ON_WAKING
    else
        HealthFields.TEMPORAL_RELATION_TO_SLEEP_FULLY_AWAKE

