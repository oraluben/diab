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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import it.diab.fit.FitActivity
import it.diab.glucose.editor.EditorActivity
import it.diab.glucose.list.GlucoseListFragment
import it.diab.glucose.overview.OverviewFragment
import it.diab.insulin.InsulinActivity
import it.diab.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var mCoordinator: CoordinatorLayout
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mFab: FloatingActionButton

    private lateinit var mGlucoseFragment: GlucoseListFragment
    private lateinit var mOverviewFragment: OverviewFragment

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_main)

        mOverviewFragment = OverviewFragment()
        mGlucoseFragment = GlucoseListFragment()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mCoordinator = findViewById(R.id.coordinator)
        mTabLayout = findViewById(R.id.tabs)
        mViewPager = findViewById(R.id.viewpager)
        mFab = findViewById(R.id.fab)

        mViewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        mTabLayout.setupWithViewPager(mViewPager)
        mFab.setOnClickListener(this::onFabClick)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addShortcuts()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)

        // Hide fit if disabled
        val fit = menu.findItem(R.id.menu_fit)
        fit.isVisible = BuildConfig.HAS_FIT

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_insulin -> onMenuInsulin()
        R.id.menu_fit -> onMenuFit()
        R.id.menu_settings -> onMenuSettings()
        else -> false
    }

    fun onItemClick(uid: Long) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra(EditorActivity.EXTRA_GLUCOSE_ID, uid)
        }
        openEditor(intent, mFab)
    }

    private fun onFabClick(view: View) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra(EditorActivity.EXTRA_INSERT_MODE, true)
        }
        openEditor(intent, view)
    }

    private fun openEditor(intent: Intent, view: View) {
        val optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, view, view.transitionName)
        startActivity(intent, optionsCompat.toBundle())
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

        val bm = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bm
    }

    private fun onMenuInsulin(): Boolean {
        val intent = Intent(this, InsulinActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun onMenuFit(): Boolean {
        if (!BuildConfig.HAS_FIT) {
            return false
        }

        val intent = Intent(this, FitActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun onMenuSettings(): Boolean {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        return true
    }

    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragments = arrayOf(mOverviewFragment, mGlucoseFragment)

        override fun getCount() = mFragments.size
        override fun getItem(position: Int) = mFragments[position]
        override fun getPageTitle(position: Int): String =
                getString(mFragments[position].getTitle())
    }

    companion object {
        private const val SHORTCUTS_VERSION = 0
        private const val KEY_SHORTCUTS = "pref_home_shortcuts"
    }
}
