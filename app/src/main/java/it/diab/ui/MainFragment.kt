package it.diab.ui

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

abstract class MainFragment : Fragment() {
    @StringRes
    abstract fun getTitle(): Int
}