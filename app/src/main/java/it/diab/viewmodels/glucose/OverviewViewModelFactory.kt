/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.viewmodels.glucose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.diab.db.repositories.GlucoseRepository

class OverviewViewModelFactory(
    private val glucoseRepository: GlucoseRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        OverviewViewModel(glucoseRepository) as T
}