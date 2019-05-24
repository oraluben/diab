/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.util.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.getDetailedString() =
    SimpleDateFormat("EEE dd-MM HH:mm", Locale.getDefault()).format(this)
        .upperCaseFirstChar()
