package it.diab.ui.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolderExt(view: View) : RecyclerView.ViewHolder(view) {
    var id: Long = -1
        protected set
}
