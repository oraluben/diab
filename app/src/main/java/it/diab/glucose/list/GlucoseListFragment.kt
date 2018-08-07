package it.diab.glucose.list

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.ui.MainFragment
import it.diab.ui.recyclerview.RecyclerViewExt

class GlucoseListFragment : MainFragment() {
    private lateinit var mRecyclerView: RecyclerViewExt

    private lateinit var mViewModel: GlucoseListViewModel
    private lateinit var mAdapter: GlucoseListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = ViewModelProviders.of(this)[GlucoseListViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                       savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_glucose, container, false)
        mRecyclerView = view.findViewById(R.id.glucose_recyclerview)

        mAdapter = GlucoseListAdapter(context!!, this::onItemClick)

        mRecyclerView.adapter = mAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel.pagedList.observe(this, Observer(this::update))
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