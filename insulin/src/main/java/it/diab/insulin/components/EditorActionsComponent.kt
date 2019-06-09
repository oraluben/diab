/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.components

import android.view.View
import it.diab.core.arch.EventBusFactory
import it.diab.core.arch.UiComponent
import it.diab.insulin.components.views.EditorActionsView

class EditorActionsComponent(
    container: View,
    bus: EventBusFactory
) : UiComponent {

    init {
        EditorActionsView(container, bus)
    }
}