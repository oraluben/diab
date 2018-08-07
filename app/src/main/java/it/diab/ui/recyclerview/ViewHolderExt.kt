package it.diab.ui.recyclerview

import androidx.recyclerview.widget.RecyclerView
import android.view.View

abstract class ViewHolderExt(view: View) : RecyclerView.ViewHolder(view) {
    var id: Long = -1
        protected set
}
