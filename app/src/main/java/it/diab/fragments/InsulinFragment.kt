/*
 * Copyright (c) 2019 Bevilacqua Joey
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import it.diab.R
import it.diab.adapters.InsulinAdapter
import it.diab.data.entities.Insulin
import it.diab.data.repositories.InsulinRepository
import it.diab.core.util.Activities
import it.diab.core.util.event.EventObserver
import it.diab.core.util.intentTo
import it.diab.viewmodels.insulin.InsulinViewModel
import it.diab.viewmodels.insulin.InsulinViewModelFactory

class InsulinFragment : BaseFragment() {
    override val titleRes = R.string.fragment_insulin

    private lateinit var recyclerView: RecyclerView

    private lateinit var viewModel: InsulinViewModel

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        val context = context ?: return

        val factory = InsulinViewModelFactory(InsulinRepository.getInstance(context))
        viewModel = ViewModelProviders.of(this, factory)[InsulinViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_insulin, container, false)
        recyclerView = view.findViewById(R.id.insulin_list)

        val adapter = InsulinAdapter()
        recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer<PagedList<Insulin>>(adapter::submitList))
        adapter.editInsulin.observe(viewLifecycleOwner, EventObserver(this::onItemClick))

        return view
    }

    private fun onItemClick(uid: Long) {
        val intent = intentTo(Activities.Insulin.Editor).apply {
            putExtra(Activities.Insulin.Editor.EXTRA_UID, uid)
        }
        startActivity(intent)
    }
}
