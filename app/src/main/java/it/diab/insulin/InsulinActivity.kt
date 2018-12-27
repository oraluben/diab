/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import it.diab.R
import it.diab.db.entities.Insulin
import it.diab.db.repositories.InsulinRepository
import it.diab.ui.recyclerview.RecyclerViewExt
import it.diab.viewmodels.insulin.InsulinViewModel
import it.diab.viewmodels.insulin.InsulinViewModelFactory

class InsulinActivity : AppCompatActivity() {
    private lateinit var adapter: InsulinAdapter

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_insulin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back)
        toolbar.setNavigationOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerViewExt>(R.id.insulin_list)
        adapter = InsulinAdapter(this)

        val factory = InsulinViewModelFactory(InsulinRepository.getInstance(this))
        val viewModel = ViewModelProviders.of(this, factory)[InsulinViewModel::class.java]
        viewModel.list.observe(this, Observer<PagedList<Insulin>>(adapter::submitList))

        recyclerView.adapter = adapter
    }
} 
