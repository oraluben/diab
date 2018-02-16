package it.diab.insulin

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
        val recyclerView = findViewById<RecyclerViewExt>(R.id.insulin_list)

        val viewModel = ViewModelProviders.of(this).get(InsulinViewModel::class.java)
        viewModel.list.observe(this,
                Observer<List<Insulin>> { list -> mAdapter.updateList(list ?: arrayListOf()) })

        toolbar.setNavigationOnClickListener { _ -> finish() }
        setSupportActionBar(toolbar)
        mAdapter = InsulinAdapter(baseContext, viewModel.list.value)
        recyclerView.adapter = mAdapter
    }
} 
