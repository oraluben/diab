/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import it.diab.R
import it.diab.adapters.InsulinAdapter
import it.diab.db.entities.Insulin
import it.diab.db.repositories.InsulinRepository
import it.diab.insulin.editor.EditorActivity
import it.diab.ui.MainFragment
import it.diab.ui.recyclerview.RecyclerViewExt
import it.diab.util.event.EventObserver
import it.diab.viewmodels.insulin.InsulinViewModel
import it.diab.viewmodels.insulin.InsulinViewModelFactory

class InsulinFragment : MainFragment() {

    private lateinit var recyclerView: RecyclerViewExt

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val adapter = InsulinAdapter()
        recyclerView.adapter = adapter
        viewModel.list.observe(this, Observer<PagedList<Insulin>>(adapter::submitList))

        adapter.editInsulin.observe(viewLifecycleOwner, EventObserver { uid ->
            val intent = Intent(context, EditorActivity::class.java).apply {
                putExtra(EditorActivity.EXTRA_UID, uid)
            }
            startActivity(intent)
        })
    }

    override fun getTitle() = R.string.fragment_insulin

    override fun onEditor(view: View) {
        val context = context ?: return
        startActivity(Intent(context, EditorActivity::class.java))
    }
}
