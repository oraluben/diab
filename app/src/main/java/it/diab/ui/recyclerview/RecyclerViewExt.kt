package it.diab.ui.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.diab.R

class RecyclerViewExt : RecyclerView {
    private var mItemTouchListener: RecyclerView.OnItemTouchListener? = null

    constructor(context: Context) : super(context) {
        setup(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, style: Int) : super(context, attrs, style) {
        setup(context, attrs)
    }

    override fun addOnItemTouchListener(listener: RecyclerView.OnItemTouchListener) {
        mItemTouchListener?.let {
            removeOnItemTouchListener(it)
        }

        mItemTouchListener = listener

        super.addOnItemTouchListener(listener)
    }

    private fun setup(context: Context, attrs: AttributeSet?) {
        val gridLayout: Boolean
        val gridColumns: Int

        if (attrs == null) {
            gridLayout = false
            gridColumns = 1
        } else {
            val array = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewExt, 0, 0)
            gridLayout = array.getBoolean(R.styleable.RecyclerViewExt_gridLayout, false)
            gridColumns = array.getInt(R.styleable.RecyclerViewExt_gridColumns, 1)
            array.recycle()
        }

        itemAnimator = DefaultItemAnimator()
        layoutManager = if (gridLayout)
            GridLayoutManager(context, gridColumns)
        else
            LinearLayoutManager(context)
    }
}
