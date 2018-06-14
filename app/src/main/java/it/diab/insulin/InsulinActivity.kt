package it.diab.insulin

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import it.diab.R
import it.diab.db.entities.Insulin
import it.diab.ui.recyclerview.RecyclerViewExt

class InsulinActivity : AppCompatActivity() {
    private lateinit var mAdapter: InsulinAdapter

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_insulin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener { _ -> finish() }

        val recyclerView = findViewById<RecyclerViewExt>(R.id.insulin_list)
        mAdapter = InsulinAdapter(baseContext)

        val viewModel = ViewModelProviders.of(this).get(InsulinViewModel::class.java)
        viewModel.list.observe(this,
                Observer<PagedList<Insulin>> { t -> mAdapter.submitList(t) })

        recyclerView.adapter = mAdapter
    }
} 
