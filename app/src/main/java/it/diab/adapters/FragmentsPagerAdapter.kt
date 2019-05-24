/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.adapters

import android.content.res.Resources
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import it.diab.fragments.BaseFragment

class FragmentsPagerAdapter(
    manager: FragmentManager,
    private val resources: Resources,
    vararg fragment: BaseFragment
) : FragmentPagerAdapter(manager) {

    private val fragments: Array<out BaseFragment> = fragment

    override fun getCount() = fragments.size

    override fun getItem(position: Int) = fragments[position]

    override fun getPageTitle(position: Int): String = fragments[position].getTitle(resources)
}