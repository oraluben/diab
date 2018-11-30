/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util

import android.content.Context
import androidx.preference.PreferenceManager
import it.diab.util.extensions.get

object PreferencesUtil {

    private const val GLUCOSE_THRESHOLD_HIGH = "pref_glucose_threshold_high"
    private const val GLUCOSE_THRESHOLD_LOW = "pref_glucose_threshold_low"

    fun getGlucoseHighThreshold(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context)[GLUCOSE_THRESHOLD_HIGH, 180]

    fun getGlucoseLowThreshold(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context)[GLUCOSE_THRESHOLD_LOW, 60]

}