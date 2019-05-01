/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fragments

import android.content.res.Configuration
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
import it.diab.data.entities.Glucose
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.core.util.event.Event
import it.diab.core.util.event.EventObserver
import it.diab.ui.TimeHeaderDecoration
import it.diab.util.extensions.doOnNextLayout
import it.diab.util.extensions.removeAllItemDecorators
import it.diab.viewmodels.glucose.GlucoseListViewModel
import it.diab.viewmodels.glucose.GlucoseListViewModelFactory

class GlucoseListFragment : BaseFragment() {
    override val titleRes = R.string.fragment_glucose

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity ?: return

        viewModel.prepare {
            setViewModelStrings()

            adapter = GlucoseListAdapter(activity, viewModel)

            recyclerView.adapter = adapter

            viewModel.pagedList.observe(activity, Observer(this::update))
            viewModel.liveList.observe(activity, Observer(this::updateHeaders))
            adapter.openGlucose.observe(activity, EventObserver(this::onItemClick))
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Update strings in case Locale changed
        setViewModelStrings()
    }

    private fun update(data: PagedList<Glucose>?) {
        adapter.submitList(data)
    }

    private fun onItemClick(uid: Long) {
        _openGlucose.value = Event(uid)
    }

    private fun setViewModelStrings() {
        viewModel.setDateStrings(
            resources.getString(R.string.time_today),
            resources.getString(R.string.time_yesterday),
            resources.getString(R.string.glucose_header_last)
        )
    }

    private fun updateHeaders(list: List<Glucose>) {
        if (list.isEmpty()) {
            return
        }

        recyclerView.doOnNextLayout {
            recyclerView.removeAllItemDecorators()
            recyclerView.addItemDecoration(TimeHeaderDecoration(requireContext(), list))
        }
    }
}