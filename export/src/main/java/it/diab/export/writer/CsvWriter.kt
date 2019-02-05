/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.export.writer

import android.os.Environment
import androidx.annotation.WorkerThread
import it.diab.core.data.entities.Glucose
import it.diab.core.data.entities.TimeFrame
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.io.File
import java.io.FileWriter
import java.io.IOException

object CsvWriter {
    private const val FULL_HEADER = "value,eatLevel,insulin\n"

    suspend fun exportCsv(scope: CoroutineScope, repository: GlucoseRepository): Boolean {
        val baseName = "train_%1\$d.csv"
        val end = System.currentTimeMillis()
        val start = end - DateUtils.DAY * 60
        val list = repository.getInDateRange(start, end)

        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val outDir = File(documentsDir, "diab").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        // Build the files asynchronously to speed things up

        val morningDeferred = scope.async {
            writeFile(list.filter { it.timeFrame == TimeFrame.MORNING }, baseName.format(1), outDir)
        }

        val lunchDeferred = scope.async {
            writeFile(list.filter { it.timeFrame == TimeFrame.LUNCH }, baseName.format(3), outDir)
        }

        val dinnerDeferred = scope.async {
            writeFile(list.filter { it.timeFrame == TimeFrame.DINNER }, baseName.format(5), outDir)
        }

        return try {
            morningDeferred.await()
            lunchDeferred.await()
            dinnerDeferred.await()
            true
        } catch (e: IOException) {
            false
        }
    }

    @WorkerThread
    private fun writeFile(list: List<Glucose>, name: String, parent: File) {
        val file = File(parent, name)
        val writer = FileWriter(file)
        val builder = StringBuilder()

        list.forEach {
            builder.append("${it.value},${it.eatLevel},${it.insulinValue0}\n")
        }

        writer.run {
            write(FULL_HEADER)
            write(builder.toString())
            close()
        }
    }
}