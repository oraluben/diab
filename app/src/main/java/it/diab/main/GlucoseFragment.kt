package it.diab.main

import android.arch.paging.PagedList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.diab.MainActivity
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.glucose.GlucoseAdapter
import it.diab.ui.MainFragment
import it.diab.ui.recyclerview.RecyclerViewExt

class GlucoseFragment : MainFragment() {
    private lateinit var mRecyclerView: RecyclerViewExt
    private var mAdapter: GlucoseAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                       savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_glucose, container, false)
        mRecyclerView = view.findViewById(R.id.glucose_recyclerview)

        mAdapter = GlucoseAdapter(context!!, this::onItemClick)

        mRecyclerView.adapter = mAdapter
        return view
    }

    override fun getTitle() = R.string.fragment_glucose

    fun update(data: PagedList<Glucose>?) {
        mAdapter?.submitList(data)
    }

    private fun onItemClick(id: Long) {
        if (activity is MainActivity) {
            (activity as MainActivity).onItemClick(id)
        }
    }
}