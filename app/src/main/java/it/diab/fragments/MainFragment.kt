/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fragments

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

abstract class MainFragment : Fragment() {
    @StringRes
    abstract fun getTitle(): Int
}