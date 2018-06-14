package it.diab

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import it.diab.db.entities.Glucose
import it.diab.glucose.GlucoseViewModel
import it.diab.glucose.editor.EditorActivity
import it.diab.glucose.export.ExportGlucoseService
import it.diab.insulin.InsulinActivity
import it.diab.main.GlucoseFragment
import it.diab.main.OverviewFragment
import it.diab.util.extensions.setDiabUi

class MainActivity : AppCompatActivity() {
    private lateinit var mCoordinator: CoordinatorLayout
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mFab: FloatingActionButton

    private lateinit var mViewModel: GlucoseViewModel
    private lateinit var mGlucoseFragment: GlucoseFragment
    private lateinit var mOverviewFragment: OverviewFragment

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_main)

        mOverviewFragment = OverviewFragment()
        mGlucoseFragment = GlucoseFragment()

        mViewModel = ViewModelProviders.of(this).get(GlucoseViewModel::class.java)
        mViewModel.list.observe(this, Observer(this::updateAverage))
        mViewModel.pagedList.observe(this, Observer(mGlucoseFragment::update))

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

    override fun onCreateOptionsMenu(menu: Menu) =
            true.also { menuInflater.inflate(R.menu.activity_main, menu) }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_insulin -> onMenuInsulin()
        R.id.menu_export -> onMenuExport()
        else -> false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_STORAGE_ACCESS -> handleStorageResult()
            REQUEST_USER_AUTH -> handleUserAuthResult(resultCode)
        }
    }

    fun onItemClick(uid: Long) {
        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra(EditorActivity.EXTRA_GLUCOSE_ID, uid)
        openEditor(intent, mFab)
    }

    private fun onFabClick(view: View) {
        val intent = Intent(this, EditorActivity::class.java)
        intent.putExtra(EditorActivity.EXTRA_INSERT_MODE, true)
        openEditor(intent, view)
    }

    private fun openEditor(intent: Intent, view: View) {
        val optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, view, view.transitionName)
        startActivity(intent, optionsCompat.toBundle())
    }

    private fun updateAverage(data: List<Glucose>?) {
        mOverviewFragment.update(data, mViewModel.getAverageLastWeek())
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

    private fun onMenuExport(): Boolean {
        AlertDialog.Builder(this)
                .setTitle(R.string.export_ask_title)
                .setMessage(R.string.export_ask_message)
                .setPositiveButton(R.string.export_ask_positive, { _, _ -> requestExport() })
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        return true
    }

    private fun handleStorageResult() {
        if (hasStorageAccess()) {
            requestExport()
            return
        }

        AlertDialog.Builder(this)
                .setTitle(R.string.export_ask_title)
                .setMessage(R.string.export_ask_permission_message)
                .setPositiveButton(R.string.export_ask_permission_positive,
                        { _, _ -> startExport() })
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    private fun handleUserAuthResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            startExport()
            return
        }

        Snackbar.make(mCoordinator, R.string.export_failed_auth, Snackbar.LENGTH_LONG)
                .setDiabUi(this)
                .show()
    }

    private fun startExport() {
        val intent = Intent(this, ExportGlucoseService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun hasStorageAccess() = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestExport() {
        if (!hasStorageAccess()) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_ACCESS)
            return
        }

        val keyguardManager = getSystemService(KeyguardManager::class.java)
        val title = getString(R.string.export_ask_auth_title)
        val message = getString(R.string.export_ask_auth_message)
        val requestIntent = keyguardManager.createConfirmDeviceCredentialIntent(title, message)

        if (requestIntent != null) {
            startActivityForResult(requestIntent, REQUEST_USER_AUTH)
            return
        }

        // No secure lock screen is set
        startExport()
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
        private const val REQUEST_STORAGE_ACCESS = 391
        private const val REQUEST_USER_AUTH = 392
    }
}
