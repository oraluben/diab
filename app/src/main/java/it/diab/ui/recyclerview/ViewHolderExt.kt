/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.ui.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolderExt(view: View) : RecyclerView.ViewHolder(view) {
    var id: Long = -1
        protected set
}
