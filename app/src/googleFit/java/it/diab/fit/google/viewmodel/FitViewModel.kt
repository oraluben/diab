/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fit.google.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.request.DataDeleteRequest
import java.util.concurrent.TimeUnit

class FitViewModel : ViewModel() {
    private val options = FitnessOptions.builder()
        .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_WRITE)
        .build()
    private var account: GoogleSignInAccount? = null

    fun isConnected() = GoogleSignIn.hasPermissions(account, options)

    fun connect(activity: Activity, requestCode: Int) {
        if (account == null) {
            account = GoogleSignIn.getLastSignedInAccount(activity)
        }
        GoogleSignIn.requestPermissions(activity, requestCode, account, options)
    }

    fun disconnect(context: Context) {
        val account = account ?: return

        // Disable fitness client
        Fitness.getConfigClient(context, account)
            .disableFit()

        // Log out
        val signInOptions = GoogleSignInOptions.Builder()
            .addExtension(options)
            .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        client.revokeAccess()
    }

    fun deleteAllData(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val account = account ?: return

        val request = DataDeleteRequest.Builder()
            .deleteAllData()
            .setTimeInterval(1L, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account)
            .deleteData(request)
            .addOnCompleteListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }
}