/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import it.diab.R
import it.diab.adapters.GlucoseListAdapter
import it.diab.core.data.entities.Glucose
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.util.event.Event
import it.diab.core.util.event.EventObserver
import it.diab.viewmodels.glucose.GlucoseListViewModel
import it.diab.viewmodels.glucose.GlucoseListViewModelFactory

class GlucoseListFragment : MainFragment() {
    private lateinit var recyclerView: RecyclerView

    private lateinit var viewModel: GlucoseListViewModel
    private lateinit var adapter: GlucoseListAdapter

    private val _openGlucose = MutableLiveData<Event<Long>>()
    val openGlucose: LiveData<Event<Long>> = _openGlucose

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        val factory = GlucoseListViewModelFactory(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        )
        viewModel = ViewModelProviders.of(this, factory)[GlucoseListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_glucose, container, false)
        recyclerView = view.findViewById(R.id.glucose_recyclerview)

        val context = context ?: return view

        viewModel.prepare {
            adapter = GlucoseListAdapter(context, viewModel)

            recyclerView.adapter = adapter

            viewModel.pagedList.observe(viewLifecycleOwner, Observer(this::update))
            adapter.openGlucose.observe(viewLifecycleOwner, EventObserver(this::onItemClick))
        }

        return view
    }

    override fun getTitle() = R.string.fragment_glucose

    private fun update(data: PagedList<Glucose>?) {
        adapter.submitList(data)
    }

    private fun onItemClick(uid: Long) {
        _openGlucose.value = Event(uid)
    }
}