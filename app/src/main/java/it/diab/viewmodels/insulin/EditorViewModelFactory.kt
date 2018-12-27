package it.diab.viewmodels.insulin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.diab.db.repositories.InsulinRepository

class EditorViewModelFactory(
        private val insulinRepository: InsulinRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
            EditorViewModel(insulinRepository) as T
}