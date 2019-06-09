/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import it.diab.core.util.Activities
import it.diab.core.util.extensions.bus
import it.diab.data.entities.Insulin
import it.diab.data.entities.TimeFrame
import it.diab.data.repositories.InsulinRepository
import it.diab.insulin.R
import it.diab.insulin.components.EditableComponent
import it.diab.insulin.components.EditorActionsComponent
import it.diab.insulin.components.status.EditableInStatus
import it.diab.insulin.events.EditEvent
import it.diab.insulin.viewmodels.EditorViewModel
import it.diab.insulin.viewmodels.EditorViewModelFactory
import it.diab.ui.widgets.BottomSheetDialogFragmentExt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class EditorFragment : BottomSheetDialogFragmentExt() {

    private lateinit var viewModel: EditorViewModel

    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return
        val factory = EditorViewModelFactory(InsulinRepository.getInstance(context))
        viewModel = ViewModelProviders.of(this, factory)[EditorViewModel::class.java]
    }

    override fun onCreateDialogView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_insulin_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EditableComponent(view, uiScope, bus)
        EditorActionsComponent(view, bus)

        val uid = arguments?.getLong(Activities.Insulin.EXTRA_EDITOR_UID, -1L) ?: -1L
        viewModel.setInsulin(uid, this::setup)
    }

    private fun setup(insulin: Insulin) {
        bus.subscribe(EditEvent::class, uiScope) {
            when (it) {
                is EditEvent.IntentSave -> {
                    viewModel.save(it.status)
                    dismiss()
                }
                is EditEvent.IntentRequestDelete -> {
                    viewModel.delete()
                    dismiss()
                }
            }
        }

        bus.emit(
            EditEvent::class, EditEvent.IntentEdit(
                EditableInStatus(
                    insulin.uid > 0L,
                    insulin.name,
                    insulin.timeFrame.toInt() + 1,
                    TimeFrame.values().map(TimeFrame::string),
                    insulin.hasHalfUnits,
                    insulin.isBasal
                )
            )
        )
    }
}