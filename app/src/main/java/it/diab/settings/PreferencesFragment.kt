package it.diab.settings

import android.content.Intent
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import it.diab.R
import it.diab.insulin.InsulinActivity

class PreferencesFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstance: Bundle?, key: String?) {
        addPreferencesFromResource(R.xml.settings)

        val insulin = findPreference(KEY_INSULIN)
        insulin?.setOnPreferenceClickListener { _ ->
            startActivity(Intent(context, InsulinActivity::class.java))
            true
        }
    }

    companion object {
        private const val KEY_INSULIN = "pref_insulins"
    }
}
