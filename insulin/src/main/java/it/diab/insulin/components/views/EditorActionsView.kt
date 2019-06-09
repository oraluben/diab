/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.components.views

import android.view.View
import com.google.android.material.button.MaterialButton
import it.diab.core.arch.EventBusFactory
import it.diab.core.arch.UiView
import it.diab.core.arch.ViewStatus
import it.diab.insulin.R
import it.diab.insulin.events.EditEvent

class EditorActionsView(
    container: View,
    bus: EventBusFactory
) : UiView<ViewStatus, ViewStatus>(container) {

    private val save: MaterialButton =
        container.findViewById(R.id.insulin_edit_save)

    private val delete: MaterialButton =
        container.findViewById(R.id.insulin_edit_delete)

    init {
        save.setOnClickListener {
            bus.emit(EditEvent::class, EditEvent.IntentRequestSave)
        }

        delete.setOnClickListener {
            bus.emit(EditEvent::class, EditEvent.IntentRequestDelete)
        }
    }
}