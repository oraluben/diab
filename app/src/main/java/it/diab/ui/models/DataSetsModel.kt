/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.ui.models

import com.github.mikephil.charting.data.Entry

sealed class DataSetsModel {

    object Empty : DataSetsModel()
    object Loading : DataSetsModel()
    data class Available(
        val today: List<Entry>,
        val average: List<Entry>
    ) : DataSetsModel()
}
