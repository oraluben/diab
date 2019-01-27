/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.fit

import android.content.Context
import it.diab.core.data.entities.Glucose
import java.lang.Exception

/**
 * Base class for Fitness services integration.
 * Create a class that extends this one and
 * update the R.string.config_class_fit_handler
 */
open class BaseFitHandler {

    /**
     * Override this to true to enable the
     * fitness integration
     */
    open val isEnabled = false

    /**
     * Whether fitness integration is allowed
     */
    open fun hasFit(context: Context) = false

    /**
     * Open the fitness activity
     *
     * @param context Context of the caller
     */
    open fun openFitActivity(context: Context) = Unit

    /**
     * Upload the glucose data to the fitness service
     */
    open fun upload(
        context: Context,
        glucose: Glucose,
        isNew: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) = Unit
}