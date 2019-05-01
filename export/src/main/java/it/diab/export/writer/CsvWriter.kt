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
import it.diab.data.entities.Glucose
import it.diab.data.entities.TimeFrame
import it.diab.data.repositories.GlucoseRepository
import it.diab.core.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import java.io.File
import java.io.FileWriter
import java.io.IOException

object CsvWriter {
    private const val FULL_HEADER = "value,eatLevel,insulin\n"
    private const val DAYS_TO_EXPORT = DateUtils.DAY * 60
    private const val OUT_DIR_NAME = "diab"

    suspend fun exportTrain(scope: CoroutineScope, repository: GlucoseRepository): Boolean {
        val baseName = "train_%1\$d.csv"
        val end = System.currentTimeMillis()
        val start = end - DAYS_TO_EXPORT
        val list = repository.getInDateRange(start, end).filter { it.insulinValue0 > 0 }

        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val outDir = File(documentsDir, OUT_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }

        // Build the files asynchronously to speed things up

        val morningDeferred = scope.async {
            writeFile(
                list.filter { it.timeFrame == TimeFrame.MORNING },
                baseName.format(TimeFrame.MORNING.ordinal),
                outDir
            )
        }

        val lunchDeferred = scope.async {
            writeFile(
                list.filter { it.timeFrame == TimeFrame.LUNCH },
                baseName.format(TimeFrame.LUNCH.ordinal),
                outDir
            )
        }

        val dinnerDeferred = scope.async {
            writeFile(
                list.filter { it.timeFrame == TimeFrame.DINNER },
                baseName.format(TimeFrame.DINNER.ordinal),
                outDir
            )
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

    suspend fun exportTest(
        scope: CoroutineScope,
        repository: GlucoseRepository,
        range: IntRange
    ): Boolean {
        val baseName = "test_%1\$d.csv"
        val end = System.currentTimeMillis()
        val start = end - DAYS_TO_EXPORT
        val list = repository.getInDateRange(start, end)

        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val outDir = File(documentsDir, OUT_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val morningDeferred = scope.async {
            writeFile(
                extractGoodOutcomes(list, range, TimeFrame.MORNING, TimeFrame.LUNCH),
                baseName.format(TimeFrame.MORNING.ordinal),
                outDir
            )
        }

        val lunchDeferred = scope.async {
            writeFile(
                extractGoodOutcomes(list, range, TimeFrame.LUNCH, TimeFrame.DINNER),
                baseName.format(TimeFrame.LUNCH.ordinal),
                outDir
            )
        }

        val dinnerDeferred = scope.async {
            writeFile(
                extractGoodOutcomes(list, range, TimeFrame.DINNER, TimeFrame.NIGHT),
                baseName.format(TimeFrame.DINNER.ordinal),
                outDir
            )
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

        writer.use {
            it.write(FULL_HEADER)
            it.write(builder.toString())
        }
    }

    @WorkerThread
    private fun extractGoodOutcomes(
        list: List<Glucose>,
        optimalRange: IntRange,
        targetTimeFrame: TimeFrame,
        maxNextTimeFrame: TimeFrame
    ): List<Glucose> {
        val result = mutableListOf<Glucose>()
        var i = list.size - 1
        while (i > 0) {
            val item = list[i]
            i--

            if (item.timeFrame != targetTimeFrame) {
                // Not a timeFrame we want
                continue
            }

            // We already moved the index to the next element
            val next = list[i]
            if (next.timeFrame.toInt() > maxNextTimeFrame.toInt()) {
                // This is too far from a desirable target to see whether the previous one was good
                continue
            }

            if (next.value in optimalRange) {
                result.add(item)
            }
        }

        return result
    }
}