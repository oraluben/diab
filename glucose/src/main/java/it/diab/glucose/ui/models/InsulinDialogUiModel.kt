/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.ui.models

import it.diab.data.entities.Insulin
import it.diab.data.entities.TimeFrame

data class InsulinDialogUiModel(
    val targetTimeFrame: TimeFrame,
    val insulinValue: Float,
    val currentInsulinId: Long,
    val insulins: List<Insulin>
)