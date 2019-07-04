/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.holders

import android.graphics.drawable.Drawable
import java.util.Date

interface GlucoseHolderCallbacks {

    /**
     * Fetch a String that represents hours of a given [Date]
     *
     * @param date date to be put in the string
     */
    fun fetchHourText(date: Date): String

    /**
     * Get the indicator drawable for
     * a given glucose value
     */
    fun getIndicator(value: Int): Drawable?

    /**
     * Get the name of an insulin
     *
     * @param uid uid of the insulin
     */
    fun getInsulinName(uid: Long): String

    /**
     * OnClick event callback
     */
    fun onClick(uid: Long)
}
