package it.diab.ui

import android.support.annotation.StringRes
import android.support.v4.app.Fragment

abstract class MainFragment : Fragment() {
    @StringRes
    abstract fun getTitle(): Int
}