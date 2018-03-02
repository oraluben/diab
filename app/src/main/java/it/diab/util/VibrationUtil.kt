package it.diab.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationUtil {

    fun vibrateForError(context: Context) {
        val vib = getVibrator(context)
        vib.exec(longArrayOf(0, 150, 125, 175), -1)
    }

    private fun Vibrator.exec(pattern: LongArray, repeat: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate(VibrationEffect.createWaveform(pattern, repeat))
        } else {
            vibrate(pattern, repeat)
        }
    }

    private fun getVibrator(context: Context) = context.getSystemService(Vibrator::class.java)
}