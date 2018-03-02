package it.diab

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import it.diab.glucose.GlucoseViewModel
import it.diab.glucose.editor.EditorActivity
import it.diab.insulin.InsulinActivity
import it.diab.main.GlucoseFragment
import it.diab.main.OverviewFragment

class MainActivity : AppCompatActivity() {
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
        mViewModel.list.observe(this, Observer(mGlucoseFragment::update))

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mTabLayout = findViewById(R.id.tabs)
        mViewPager = findViewById(R.id.viewpager)
        mFab = findViewById(R.id.fab)

        mViewPager.adapter = ViewPagerAdapter(supportFragmentManager)
        mTabLayout.setupWithViewPager(mViewPager)
        mFab.setOnClickListener(this::onFabClick)
    }

    override fun onResume() {
        super.onResume()

        // Post content
        Handler().postDelayed(this::setupContent, 100)
    }

    override fun onCreateOptionsMenu(menu: Menu) =
            true.also { menuInflater.inflate(R.menu.activity_main, menu) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_insulin) {
            startActivity(Intent(this, InsulinActivity::class.java))
        }

        return true
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

    private fun setupContent() {
        val data = mViewModel.list.value
        mGlucoseFragment.update(data)
        mOverviewFragment.update(data, mViewModel.getAverageLastWeek())
    }

    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragments = arrayOf(mOverviewFragment, mGlucoseFragment)

        override fun getCount() = mFragments.size
        override fun getItem(position: Int) = mFragments[position]
        override fun getPageTitle(position: Int): String =
                getString(mFragments[position].getTitle())
    }
}
