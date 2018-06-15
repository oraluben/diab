package it.diab.util.extensions

import com.google.android.gms.fitness.data.HealthFields
import it.diab.util.timeFrame.TimeFrame

fun TimeFrame.toFitMealRelation() = when (toInt()) {
    1,
    5 -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_AFTER_MEAL
    else -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL
}

fun TimeFrame.toFitSleepRelation() =
        if (this == TimeFrame.MORNING) HealthFields.TEMPORAL_RELATION_TO_SLEEP_ON_WAKING
        else HealthFields.TEMPORAL_RELATION_TO_SLEEP_FULLY_AWAKE

