/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.ml

import android.content.Context
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import it.diab.db.entities.Glucose
import it.diab.util.extensions.set
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class PluginManager(context: Context) {
    private val job = Job()
    private val scope = CoroutineScope(IO + job)

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val pluginDir = File(ContextCompat.getDataDir(context), "plugin")
    private val emptyStream: InputStream
        get() = ByteArrayInputStream("{\n}".toByteArray(Charsets.UTF_8))

    fun isInstalled() = pluginDir.exists() && pluginDir.list().isNotEmpty()

    fun install(iStream: InputStream) {
        scope.launch {
            val buffer = ByteArray(1024)
            val zipStream = ZipInputStream(iStream)
            var entry: ZipEntry? = zipStream.nextEntry

            val pattern = Pattern.compile("estimator_[0-6].json")

            var wasValid = false

            while (entry != null) {
                // Filter out unneeded files
                val matcher = pattern.matcher(entry.name)
                if (!matcher.find()) {
                    entry = zipStream.nextEntry
                    continue
                }

                if (!pluginDir.exists()) {
                    pluginDir.mkdir()
                }

                val extractedFile = File(pluginDir, entry.name)
                extractedFile.createNewFile()
                val oStream = FileOutputStream(extractedFile)
                var len = zipStream.read(buffer)
                while (len > 0) {
                    oStream.write(buffer, 0, len)
                    len = zipStream.read(buffer)
                }

                oStream.close()

                if (!wasValid) {
                    wasValid = true
                }

                entry = zipStream.nextEntry
            }

            zipStream.run {
                closeEntry()
                close()
            }

            if (wasValid) {
                preferences[LAST_UPDATE] = System.currentTimeMillis()
            }
        }
    }

    fun uninstall() {
        scope.launch {
            pluginDir.deleteRecursively()
            preferences[LAST_UPDATE] = 0L
        }
    }

    fun fetchSuggestion(glucose: Glucose, onExecuted: (Float) -> Unit) {
        scope.launch {
            val value = glucose.value / 10 * 10
            if (value < LOWEST_SUGGESTION) {
                GlobalScope.launch(Dispatchers.Main) { onExecuted(TOO_LOW) }
                return@launch
            }
            if (value > HIGHEST_SUGGESTION) {
                GlobalScope.launch(Dispatchers.Main) { onExecuted(TOO_HIGH) }
                return@launch
            }

            val iStream = getStreamFor(glucose.timeFrame)
            val map = parseInputStream(iStream)

            val result = map[value] ?: PARSE_ERROR
            GlobalScope.launch(Dispatchers.Main) {
                onExecuted(if (result == PARSE_ERROR) PARSE_ERROR else result + glucose.eatLevel - 1)
            }
        }
    }

    @WorkerThread
    private fun parseInputStream(iStream: InputStream): HashMap<Int, Float> {
        val content = BufferedReader(InputStreamReader(iStream)).readLines()

        val json = JSONObject(content)
        val iterator = json.keys()
        val map = HashMap<Int, Float>()

        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = json[key]

            if (value is Double) {
                map[key.toInt()] = value.toFloat()
            }
        }

        return map
    }

    @WorkerThread
    private fun getStreamFor(timeFrame: TimeFrame): InputStream {
        val file = File(pluginDir, MODEL_NAME.format(timeFrame.ordinal))

        if (!file.exists() || !file.canRead()) {
            return emptyStream
        }

        return FileInputStream(file)
    }

    private fun BufferedReader.readLines(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return lines().parallel().collect(Collectors.joining("\n"))
        }

        val builder = StringBuilder()
        var line: String? = readLine()
        while (line != null) {
            builder.append(line).append("\n")
            line = readLine()
        }
        return builder.toString()
    }

    companion object {
        private const val MODEL_NAME = "estimator_%1\$d.json"

        private const val LOWEST_SUGGESTION = 40
        private const val HIGHEST_SUGGESTION = 420

        const val LAST_UPDATE = "pref_plugin_last_update"

        const val TOO_LOW = -1f
        const val TOO_HIGH = -2f
        const val PARSE_ERROR = -3f
        const val NO_MODEL = -4f
    }
}
