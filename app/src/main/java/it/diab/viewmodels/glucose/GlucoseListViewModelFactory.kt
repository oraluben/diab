package it.diab.viewmodels.glucose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository

class GlucoseListViewModelFactory(
        private val glucoseRepository: GlucoseRepository,
        private val insulinRepository: InsulinRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
            GlucoseListViewModel(glucoseRepository, insulinRepository) as T
}