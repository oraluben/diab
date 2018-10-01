package it.diab.insulin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
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
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerViewExt>(R.id.insulin_list)
        mAdapter = InsulinAdapter(this)

        val viewModel = ViewModelProviders.of(this).get(InsulinViewModel::class.java)
        viewModel.list.observe(this,
                Observer<PagedList<Insulin>> { t -> mAdapter.submitList(t) })

        recyclerView.adapter = mAdapter
    }
} 
