/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import it.diab.adapters.FragmentsPagerAdapter
import it.diab.core.util.Activities
import it.diab.core.util.event.EventObserver
import it.diab.core.util.intentTo
import it.diab.fragments.GlucoseListFragment
import it.diab.fragments.InsulinFragment
import it.diab.fragments.OverviewFragment
import it.diab.util.ShortcutUtils

class MainActivity : AppCompatActivity() {
    private lateinit var fab: FloatingActionButton

    private val fragmentsLifeCycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)

            // Re-bind click listener when glucose fragment is re-created
            if (f is GlucoseListFragment) {
                f.openGlucose.observe(
                    this@MainActivity,
                    EventObserver(this@MainActivity::onGlucoseClick)
                )
            }
        }
    }

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_main)

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        fab = findViewById(R.id.fab)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentsLifeCycleCallback, false)
        viewPager.adapter = FragmentsPagerAdapter(
            supportFragmentManager,
            resources,
            OverviewFragment(),
            GlucoseListFragment(),
            InsulinFragment()
        )

        tabLayout.setupWithViewPager(viewPager)
        fab.setOnClickListener { onGlucoseClick(-1) }

        createShortcuts()
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentsLifeCycleCallback)

        super.onDestroy()
    }

    private fun onGlucoseClick(uid: Long) {
        val intent = intentTo(Activities.Glucose.Editor).apply {
            putExtra(Activities.Glucose.Editor.EXTRA_UID, uid)
        }
        val optionsCompat = ActivityOptionsCompat
            .makeSceneTransitionAnimation(this, fab, fab.transitionName)
        startActivity(intent, optionsCompat.toBundle())
    }

    private fun createShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutUtils.setupShortcuts(this)
        }
    }
}
