package it.diab.ui.graph

import com.robinhood.spark.SparkAdapter
import it.diab.db.entities.Glucose

class SimpleSparkAdapter(list: List<Glucose>) : SparkAdapter() {
    private var mData = FloatArray(0, { 0F })

    init {
        // If there's only one item, duplicate it so a line will be drawn instead of a point
        var size = list.size
        if (size == 1) {
            size = 2
        }

        val data = FloatArray(size)
        for (i in data.indices) {
            data[i] = list[if (size > list.size) 0 else i].value.toFloat()
        }
        mData = data
    }

    override fun getCount() = mData.size

    override fun getItem(index: Int) = mData[index]

    override fun getY(index: Int) = mData[index]
}
