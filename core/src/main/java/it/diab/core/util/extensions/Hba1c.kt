/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util.extensions

import it.diab.core.data.entities.Hba1c

fun hba1c(block: Hba1c.() -> Unit) = Hba1c().apply(block)