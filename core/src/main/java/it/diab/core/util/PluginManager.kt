/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.TimeFrame
import it.diab.core.util.extensions.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val emptyStream by lazy {
        ByteArrayInputStream("{\n}".toByteArray(Charsets.UTF_8))
    }

    fun isInstalled() = pluginDir.exists() && pluginDir.list().isNotEmpty()

    fun install(iStream: InputStream) {
        scope.launch {
            val pattern = Pattern.compile("^estimator_[0-6].json")
            var wasValid = false

            ZipInputStream(iStream).use { zipStream ->
                var entry: ZipEntry? = zipStream.nextEntry

                while (entry != null) {
                    // Filter out unneeded files
                    val matcher = pattern.matcher(entry.name)
                    if (!matcher.find()) {
                        entry = zipStream.nextEntry
                        continue
                    }

                    extractZipEntry(zipStream, entry)
                    wasValid = true
                    entry = zipStream.nextEntry
                }

                zipStream.closeEntry()
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

    suspend fun fetchSuggestion(glucose: Glucose, onExecuted: (Float) -> Unit) {
        delay(1000)

        val value = glucose.value / 10 * 10
        if (value <= LOWEST_SUGGESTION) {
            GlobalScope.launch(Dispatchers.Main) { onExecuted(TOO_LOW) }
            return
        }
        if (value >= HIGHEST_SUGGESTION) {
            GlobalScope.launch(Dispatchers.Main) { onExecuted(TOO_HIGH) }
            return
        }

        val iStream = getStreamFor(glucose.timeFrame)
        val map = parseInputStream(iStream)

        val result = map[value] ?: PARSE_ERROR

        GlobalScope.launch(Dispatchers.Main) {
            onExecuted(if (result == PARSE_ERROR) PARSE_ERROR else result + glucose.eatLevel - 1)
        }
    }

    @WorkerThread
    private fun parseInputStream(iStream: InputStream): HashMap<Int, Float> {
        val map = HashMap<Int, Float>()
        iStream.use {
            val content = BufferedReader(InputStreamReader(iStream)).readLines()

            val json = JSONObject(content)
            val iterator = json.keys()

            while (iterator.hasNext()) {
                val key = iterator.next()
                val value = json[key]

                if (value is Double) {
                    map[key.toInt()] = value.toFloat()
                }
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

    @WorkerThread
    private fun extractZipEntry(zipStream: ZipInputStream, entry: ZipEntry) {
        if (!pluginDir.exists()) {
            pluginDir.mkdir()
        }

        val buffer = ByteArray(1024)
        val extractedFile = File(pluginDir, entry.name)
        extractedFile.createNewFile()
        FileOutputStream(extractedFile).use { oStream ->
            var len = zipStream.read(buffer)
            while (len > 0) {
                oStream.write(buffer, 0, len)
                len = zipStream.read(buffer)
            }
        }
    }

    private fun BufferedReader.readLines(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return lines().collect(Collectors.joining("\n"))
        }

        val builder = StringBuilder()
        var line: String? = readLine()
        while (line != null) {
            builder.append(line).append('\n')
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
