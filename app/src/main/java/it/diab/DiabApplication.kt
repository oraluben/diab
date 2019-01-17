/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab

import android.app.Application
import androidx.preference.PreferenceManager
import it.diab.settings.SettingsFragment
import it.diab.util.UIUtils
import it.diab.util.extensions.get

@Suppress("unused")
class DiabApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setStyle()
    }

    private fun setStyle() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        UIUtils.setStyleMode(prefs[SettingsFragment.PREF_UI_STYLE, "1"])
    }
}