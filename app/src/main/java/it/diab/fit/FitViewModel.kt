package it.diab.fit

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.HealthDataTypes
import com.google.android.gms.fitness.request.DataDeleteRequest
import java.util.concurrent.TimeUnit

class FitViewModel(owner: Application) : AndroidViewModel(owner) {
    private val mOptions = FitnessOptions.builder()
            .addDataType(HealthDataTypes.TYPE_BLOOD_GLUCOSE, FitnessOptions.ACCESS_WRITE)
            .build()
    private val mAccount = GoogleSignIn.getLastSignedInAccount(owner)

    fun isConnected() = GoogleSignIn.hasPermissions(mAccount, mOptions)

    fun connect(activity: Activity, requestCode: Int) {
        GoogleSignIn.requestPermissions(activity, requestCode, mAccount, mOptions)
    }

    fun disconnect(context: Context) {
        // Disable fitness client
        Fitness.getConfigClient(context, mAccount!!)
                .disableFit()

        // Log out
        val signInOptions = GoogleSignInOptions.Builder()
                .addExtension(mOptions)
                .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        client.revokeAccess()
    }

    fun deleteAllData(context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val request = DataDeleteRequest.Builder()
                .deleteAllData()
                .setTimeInterval(1L, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build()

        Fitness.getHistoryClient(context, mAccount!!)
                .deleteData(request)
                .addOnCompleteListener { onSuccess() }
                .addOnFailureListener { onFailure() }
    }
}