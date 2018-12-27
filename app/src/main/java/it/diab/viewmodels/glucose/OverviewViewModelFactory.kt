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