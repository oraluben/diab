/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util.extensions

import android.widget.TextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun TextView.setPrecomputedText(text: CharSequence) {
    val params = TextViewCompat.getTextMetricsParams(this)

    GlobalScope.launch(Dispatchers.Default) {
        val textDef = async { PrecomputedTextCompat.getTextFuture(text, params, null).get() }
        launch(Dispatchers.Main) { this@setPrecomputedText.text = textDef.await() }
    }
}
