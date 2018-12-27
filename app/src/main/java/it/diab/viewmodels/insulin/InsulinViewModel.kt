package it.diab.viewmodels.insulin

import androidx.paging.LivePagedListBuilder
import it.diab.db.repositories.InsulinRepository
import it.diab.viewmodels.ScopedViewModel

class InsulinViewModel internal constructor(
        insulinRepository: InsulinRepository
) : ScopedViewModel() {

    val list = LivePagedListBuilder(insulinRepository.all, 5).build()
}