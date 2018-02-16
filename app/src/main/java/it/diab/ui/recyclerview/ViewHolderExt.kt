package it.diab.ui.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class ViewHolderExt(view: View) : RecyclerView.ViewHolder(view) {
    var id: Long = -1
        protected set
}
