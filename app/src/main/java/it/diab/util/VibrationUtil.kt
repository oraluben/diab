package it.diab.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View

object VibrationUtil {

    fun vibrateForError(context: Context) {
        context.getSystemService(Vibrator::class.java).apply {
            val pattern = longArrayOf(0, 150, 125, 175)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION") // Needed for api25-
                vibrate(pattern, -1)
            }
        }
    }

    fun vibrateForImportantClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
    }
}