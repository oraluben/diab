package it.diab.ui.recyclerview

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class RecyclerViewExt : RecyclerView {
    private var mItemTouchListener: RecyclerView.OnItemTouchListener? = null

    constructor(context: Context) : super(context) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context)
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style) {
        setup(context)
    }

    override fun addOnItemTouchListener(listener: RecyclerView.OnItemTouchListener) {
        if (mItemTouchListener != null) {
            removeOnItemTouchListener(mItemTouchListener)
        }
        mItemTouchListener = listener

        super.addOnItemTouchListener(listener)
    }

    private fun setup(context: Context) {
        itemAnimator = DefaultItemAnimator()
        layoutManager = LinearLayoutManager(context)
    }
}
