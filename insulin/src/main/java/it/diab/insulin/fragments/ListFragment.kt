/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import it.diab.core.util.Activities
import it.diab.core.util.event.EventObserver
import it.diab.data.repositories.InsulinRepository
import it.diab.insulin.R
import it.diab.insulin.adapters.InsulinAdapter
import it.diab.insulin.viewmodels.ListViewModel
import it.diab.insulin.viewmodels.ListViewModelFactory

class ListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var viewModel: ListViewModel

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        val context = context ?: return

        val factory = ListViewModelFactory(InsulinRepository.getInstance(context))
        viewModel = ViewModelProviders.of(this, factory)[ListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_insulin_list, container, false)
        recyclerView = view.findViewById(R.id.insulin_list)

        val adapter = InsulinAdapter()
        recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer(adapter::submitList))
        adapter.editInsulin.observe(viewLifecycleOwner, EventObserver(this::onItemClick))

        return view
    }

    private fun onItemClick(uid: Long) {
        val editorFragment = EditorFragment().apply {
            arguments = Bundle().apply {
                putLong(Activities.Insulin.EXTRA_EDITOR_UID, uid)
            }
        }

        childFragmentManager.beginTransaction()
            .add(editorFragment, "editor")
            .commit()
    }
}
