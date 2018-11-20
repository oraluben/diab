package it.diab.util.extensions

import android.widget.TextView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun TextView.setPrecomputedText(text: CharSequence, coroutineScope: CoroutineScope) {
    val params = TextViewCompat.getTextMetricsParams(this)

    coroutineScope.launch(Dispatchers.IO) {
        val textDef = async { PrecomputedTextCompat.getTextFuture(text, params, null).get() }
        GlobalScope.launch(Dispatchers.Main) { this@setPrecomputedText.text = textDef.await() }
    }
}