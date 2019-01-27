/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.insulin

import androidx.paging.LivePagedListBuilder
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.viewmodels.ScopedViewModel

class InsulinViewModel internal constructor(
    insulinRepository: InsulinRepository
) : ScopedViewModel() {

    val list = LivePagedListBuilder(insulinRepository.all, 5).build()
}