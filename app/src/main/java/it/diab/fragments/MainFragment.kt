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
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.diab.R
import it.diab.adapters.MainAdapter
import it.diab.core.util.Activities
import it.diab.core.util.event.EventObserver
import it.diab.core.util.intentTo
import it.diab.data.entities.Glucose
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.ui.TimeHeaderDecoration
import it.diab.ui.models.DataSetsModel
import it.diab.ui.models.LastGlucoseModel
import it.diab.ui.widgets.RecyclerViewExt
import it.diab.util.extensions.doOnNextLayout
import it.diab.util.extensions.removeAllItemDecorators
import it.diab.viewmodels.MainViewModel
import it.diab.viewmodels.MainViewModelFactory

class MainFragment : Fragment(), MainAdapter.Callbacks {

    private lateinit var viewModel: MainViewModel
    private lateinit var listAdapter: MainAdapter

    private lateinit var fab: FloatingActionButton
    private lateinit var glucoseList: RecyclerViewExt

    private var last: LastGlucoseModel = LastGlucoseModel.Loading
    private var dataSetsModel: DataSetsModel = DataSetsModel.Loading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return
        val factory = MainViewModelFactory(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        )

        viewModel = ViewModelProviders.of(this, factory)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        fab = view.findViewById(R.id.fab)
        glucoseList = view.findViewById(R.id.main_list)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return
        listAdapter = MainAdapter(context, this)

        fab.setOnClickListener { onFabClick() }

        viewModel.prepare {
            val activity = activity ?: return@prepare
            viewModel.pagedList.observe(activity, Observer(this::onPagedListChanged))
            viewModel.liveList.observe(activity, Observer(this::onLiveListChanged))
            listAdapter.openGlucose.observe(activity, EventObserver(this::onItemClick))
        }
    }

    override fun getInsulin(uid: Long) = viewModel.getInsulin(uid)

    private fun onItemClick(uid: Long) {
        val activity = activity ?: return
        val intent = intentTo(Activities.Glucose.Editor).apply {
            putExtra(Activities.Glucose.Editor.EXTRA_UID, uid)
        }

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            fab,
            fab.transitionName
        )
        startActivity(intent, options.toBundle())
    }

    private fun onFabClick() {
        onItemClick(-1)
    }

    private fun onPagedListChanged(data: PagedList<Glucose>?) {
        listAdapter.submitList(data)

        if (glucoseList.adapter == null) {
            glucoseList.adapter = listAdapter
        }
    }

    private fun onLiveListChanged(data: List<Glucose>?) {
        if (data == null) {
            return
        }

        if (data.isEmpty()) {
            last = LastGlucoseModel.Empty
            dataSetsModel = DataSetsModel.Empty
        } else {
            last = LastGlucoseModel.Available(data[0])
        }

        updateHeaders(data)
        viewModel.getDataSets(this::onGraphDataSetChanged)
    }

    override fun getLastGlucose() = last

    override fun getDataSets() = dataSetsModel

    private fun onGraphDataSetChanged(model: DataSetsModel) {
        dataSetsModel = model
        listAdapter.notifyItemChanged(0)
    }

    private fun updateHeaders(list: List<Glucose>) {
        glucoseList.doOnNextLayout {
            glucoseList.removeAllItemDecorators()
            glucoseList.addItemDecoration(TimeHeaderDecoration(requireContext(), list, 1))
        }
    }
}