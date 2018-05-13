package it.diab.main

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

        val list = arguments?.getParcelableArrayList<Glucose>(ARG_DATA)
        mAdapter = GlucoseAdapter(context!!, list, this::onItemClick)

        mRecyclerView.adapter = mAdapter
        return view
    }

    override fun getTitle() = R.string.fragment_glucose

    override fun update(data: List<Glucose>?) {
        mAdapter?.updateList(data)
    }

    private fun onItemClick(id: Long) {
        if (activity is MainActivity) {
            (activity as MainActivity).onItemClick(id)
        }
    }

    companion object {
        const val ARG_DATA = "extra_data"
    }
}