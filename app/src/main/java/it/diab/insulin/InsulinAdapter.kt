/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import it.diab.R
import it.diab.db.entities.Insulin
import it.diab.insulin.editor.EditorActivity
import it.diab.ui.recyclerview.ViewHolderExt

class InsulinAdapter(private val context: Context) :
        PagedListAdapter<Insulin, InsulinAdapter.InsulinHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsulinHolder {
        return InsulinHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_insulin, parent, false))
    }

    override fun onBindViewHolder(holder: InsulinHolder, position: Int) {
        val item = getItem(position)
        if (item == null) {
            holder.clear()
        } else {
            holder.onBind(context, item)
        }
    }

    class InsulinHolder(view: View) : ViewHolderExt(view) {
        private val title: TextView = view.findViewById(R.id.item_insulin_name)
        private val icon: ImageView = view.findViewById(R.id.item_insulin_icon)

        fun onBind(context: Context, insulin: Insulin) {
            id = insulin.uid

            title.text = insulin.name
            icon.setImageResource(insulin.timeFrame.icon)

            itemView.setOnClickListener {
                val intent = Intent(context, EditorActivity::class.java)
                intent.putExtra(EditorActivity.EXTRA_UID, insulin.uid)
                context.startActivity(intent)
            }
        }

        fun clear() {
            itemView.visibility = View.GONE
        }
    }

    companion object {
        private val CALLBACK = object : DiffUtil.ItemCallback<Insulin>() {
            override fun areContentsTheSame(oldItem: Insulin, newItem: Insulin) =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: Insulin, newItem: Insulin) =
                    oldItem.uid == newItem.uid
        }
    }
}
