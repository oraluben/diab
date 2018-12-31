/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab

import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import it.diab.glucose.editor.EditorActivity
import it.diab.glucose.list.GlucoseListFragment
import it.diab.glucose.overview.OverviewFragment
import it.diab.insulin.InsulinFragment
import it.diab.util.event.EventObserver

class MainActivity : AppCompatActivity() {
    private lateinit var coordinator: CoordinatorLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var fab: FloatingActionButton

    private lateinit var overviewFragment: OverviewFragment
    private lateinit var glucoseFragment: GlucoseListFragment
    private lateinit var insulinFragment: InsulinFragment

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_main)

        overviewFragment = OverviewFragment()
        glucoseFragment = GlucoseListFragment()
        insulinFragment = InsulinFragment()

        coordinator = findViewById(R.id.coordinator)
        tabLayout = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.viewpager)
        fab = findViewById(R.id.fab)

        adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)
        fab.setOnClickListener(this::onFabClick)

        glucoseFragment.openGlucose.observe(this, EventObserver(this::onGlucoseClick))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addShortcuts()
        }
    }

    private fun onGlucoseClick(uid: Long) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra(EditorActivity.EXTRA_GLUCOSE_ID, uid)
        }
        val optionsCompat = ActivityOptionsCompat
            .makeSceneTransitionAnimation(this, fab, fab.transitionName)
        startActivity(intent, optionsCompat.toBundle())
    }

    private fun onFabClick(view: View) {
        adapter.onEditor(view, viewPager.currentItem)
    }

    @TargetApi(26)
    private fun addShortcuts() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getInt(KEY_SHORTCUTS, SHORTCUTS_VERSION - 1) >= SHORTCUTS_VERSION) {
            return
        }

        val manager = getSystemService(ShortcutManager::class.java)

        val title = getString(R.string.app_shortcut_add_glucose)
        val editIntent = Intent(this, EditorActivity::class.java)
        editIntent.putExtra(EditorActivity.EXTRA_INSERT_MODE, true)
        editIntent.action = Intent.ACTION_VIEW
        val bm = getShortcutIcon(R.drawable.ic_shortcut_add_glucose)
        val addShortcut = ShortcutInfo.Builder(this, title)
            .setShortLabel(title)
            .setLongLabel(title)
            .setIntent(editIntent)
            .setIcon(Icon.createWithAdaptiveBitmap(bm))
            .build()

        manager.removeAllDynamicShortcuts()
        manager.addDynamicShortcuts(listOf(addShortcut))

        prefs.edit().putInt(KEY_SHORTCUTS, SHORTCUTS_VERSION).apply()
    }

    private fun getShortcutIcon(@DrawableRes icon: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, icon)
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        if (drawable == null) {
            throw IllegalArgumentException("Could not get a valid drawable from argument")
        }

        val bm = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bm)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bm
    }

    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val fragments = arrayOf(overviewFragment, glucoseFragment, insulinFragment)

        override fun getCount() = fragments.size
        override fun getItem(position: Int) = fragments[position]
        override fun getPageTitle(position: Int): String = getString(fragments[position].getTitle())

        fun onEditor(view: View, position: Int) {
            fragments[position].onEditor(view)
        }
    }

    companion object {
        private const val SHORTCUTS_VERSION = 0
        private const val KEY_SHORTCUTS = "pref_home_shortcuts"
    }
}
