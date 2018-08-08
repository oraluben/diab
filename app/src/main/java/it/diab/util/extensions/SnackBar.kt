package it.diab.util.extensions

import android.content.Context
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import it.diab.R

fun Snackbar.setDiabUi(context: Context): Snackbar {
    val margin = context.getDimen(R.dimen.snack_bar_margin)
    val elevation = context.getDimen(R.dimen.snack_bar_elevation)

    val params = view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(margin, margin, margin, margin)

    view.background = context.getDrawable(R.drawable.bg_snackbar)
    view.elevation = elevation.toFloat()

    return this
}