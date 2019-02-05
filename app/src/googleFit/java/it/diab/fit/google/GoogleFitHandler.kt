/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fit.google

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.Device
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.data.HealthFields
import com.google.android.gms.fitness.request.DataUpdateRequest
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.TimeFrame
import it.diab.core.override.BaseFitHandler
import java.util.concurrent.TimeUnit

@Suppress("unused")
class GoogleFitHandler : BaseFitHandler() {

    override val isEnabled = true

    override fun hasFit(context: Context): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val status = availability.isGooglePlayServicesAvailable(context)

        if (status != ConnectionResult.SUCCESS) {
            return false
        }

        val options = FitnessOptions.builder()
            .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_WRITE)
            .build()

        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), options)
    }

    override fun openFitActivity(context: Context) {
        val intent = Intent(context, FitActivity::class.java)
        context.startActivity(intent)
    }

    override fun upload(
        context: Context,
        glucose: Glucose,
        isNew: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val timeStamp = glucose.date.time
        val source = DataSource.Builder()
            .setType(DataSource.TYPE_RAW)
            .setDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE)
            .setDevice(Device.getLocalDevice(context))
            .build()

        val data = DataPoint.create(source).apply {
            setTimestamp(timeStamp, TimeUnit.MILLISECONDS)

            getValue(HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL).setFloat(glucose.value / 18f)

            getValue(HealthFields.FIELD_BLOOD_GLUCOSE_SPECIMEN_SOURCE).setInt(
                HealthFields.BLOOD_GLUCOSE_SPECIMEN_SOURCE_CAPILLARY_BLOOD
            )

            getValue(HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL).setInt(glucose.timeFrame.toFitMealRelation())

            getValue(HealthFields.FIELD_TEMPORAL_RELATION_TO_SLEEP).setInt(glucose.timeFrame.toFitSleepRelation())
        }

        val set = DataSet.create(source).apply { add(data) }

        val request = DataUpdateRequest.Builder()
            .setDataSet(set)
            .setTimeInterval(timeStamp, timeStamp, TimeUnit.MILLISECONDS)
            .build() ?: return

        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return
        Fitness.getHistoryClient(context, account)
            .run { if (isNew) insertData(set) else updateData(request) }
            .addOnFailureListener { e -> onFailure(e) }
            .addOnSuccessListener { onSuccess() }
    }

    private fun TimeFrame.toFitMealRelation() = when (this) {
        TimeFrame.LATE_MORNING,
        TimeFrame.AFTERNOON,
        TimeFrame.NIGHT -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_AFTER_MEAL
        else -> HealthFields.FIELD_TEMPORAL_RELATION_TO_MEAL_BEFORE_MEAL
    }

    private fun TimeFrame.toFitSleepRelation() =
        if (TimeFrame.MORNING == this)
            HealthFields.TEMPORAL_RELATION_TO_SLEEP_ON_WAKING
        else
            HealthFields.TEMPORAL_RELATION_TO_SLEEP_FULLY_AWAKE
}