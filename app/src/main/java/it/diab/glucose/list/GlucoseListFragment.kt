/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.ui.MainFragment
import it.diab.ui.recyclerview.RecyclerViewExt
import it.diab.viewmodels.glucose.GlucoseListViewModel
import it.diab.viewmodels.glucose.GlucoseListViewModelFactory

class GlucoseListFragment : MainFragment() {
    private lateinit var mRecyclerView: RecyclerViewExt

    private lateinit var viewModel: GlucoseListViewModel
    private lateinit var mAdapter: GlucoseListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        val factory = GlucoseListViewModelFactory(
                GlucoseRepository.getInstance(context),
                InsulinRepository.getInstance(context)
        )
        viewModel = ViewModelProviders.of(this, factory)[GlucoseListViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                       savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_glucose, container, false)
        mRecyclerView = view.findViewById(R.id.glucose_recyclerview)

        mAdapter = GlucoseListAdapter(context!!, this::onItemClick, viewModel)

        viewModel.prepare { mRecyclerView.adapter = mAdapter }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pagedList.observe(this, Observer(this::update))
    }

    override fun getTitle() = R.string.fragment_glucose

    private fun update(data: PagedList<Glucose>?) {
        mAdapter.submitList(data)
    }

    private fun onItemClick(id: Long) {
        if (activity is MainActivity) {
            (activity as MainActivity).onItemClick(id)
        }
    }
}