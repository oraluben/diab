/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.export.writer

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.WorkerThread
import it.diab.core.data.repositories.GlucoseRepository
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.util.extensions.forEachWithIndex
import it.diab.core.util.extensions.format
import it.diab.export.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.dhatim.fastexcel.Color
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object XlsxWriter {
    private const val TAG = "ExportService"

    suspend fun exportSheet(
        scope: CoroutineScope,
        glucoseRepository: GlucoseRepository,
        insulinRepository: InsulinRepository,
        glucoseHeaders: List<String>,
        insulinHeaders: List<String>
    ): File? {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val outDir = File(documentsDir, "diab").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val file = File(outDir, "diab-$date.xlsx")

        FileOutputStream(file).use {
            val workBook = Workbook(it, BuildConfig.APPLICATION_ID, null)
            val glucoseSheet = workBook.newWorksheet("Glucose")
            val insulinSheet = workBook.newWorksheet("Insulin")

            val glucoseDeferred = scope.async {
                buildGlucoseSheet(glucoseSheet, glucoseRepository, insulinRepository, glucoseHeaders)
            }

            val insulinDeferred = scope.async {
                buildInsulinSheet(insulinSheet, insulinRepository, insulinHeaders)
            }

            return try {
                glucoseDeferred.await()
                insulinDeferred.await()
                workBook.finish()
                file
            } catch (e: IOException) {
                Log.e(TAG, e.message)
                null
            }
        }
    }

    @WorkerThread
    private fun buildGlucoseSheet(
        sheet: Worksheet,
        glucoseRepository: GlucoseRepository,
        insulinRepository: InsulinRepository,
        headers: List<String>
    ) {
        headers.forEachWithIndex { i, str -> sheet.value(0, i, str) }
        sheet.style(0, headers.size - 1).bold()

        val list = glucoseRepository.getAllItems()
        list.forEachWithIndex { i, glucose ->
            sheet.run {
                val insulin = insulinRepository.getById(glucose.insulinId0)
                val basal = insulinRepository.getById(glucose.insulinId1)

                value(i + 1, 0, glucose.value)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Java 8's java.time.LocalDateTime is required for this
                    value(i + 1, 1, glucose.date)
                } else {
                    value(i + 1, 1, glucose.date.format("yyyy-MM-dd HH:mm"))
                }
                value(i + 1, 2, glucose.eatLevel)
                if (insulin.name.isNotEmpty()) {
                    value(i + 1, 3, insulin.name)
                    value(i + 1, 4, glucose.insulinValue0)
                }
                if (basal.name.isNotEmpty()) {
                    value(i + 1, 5, basal.name)
                    value(i + 1, 6, glucose.insulinValue1)
                }
            }
        }

        sheet.range(0, 0, list.size, headers.size - 1)
            .style()
            .shadeAlternateRows(Color.GRAY2)
            .set()

        // Java 8's java.time.LocalDateTime is required for this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sheet.range(1, 1, list.size, 1)
                .style()
                .format("yyyy-MM-dd HH:mm")
                .set()
        }
    }

    @WorkerThread
    private fun buildInsulinSheet(
        sheet: Worksheet,
        insulinRepository: InsulinRepository,
        headers: List<String>
    ) {
        headers.forEachWithIndex { i, str -> sheet.value(0, i, str) }
        sheet.style(0, headers.size - 1).bold()

        val list = insulinRepository.getInsulins()
        list.forEachWithIndex { i, insulin ->
            sheet.run {
                value(i + 1, 0, insulin.name)
                value(i + 1, 1, insulin.timeFrame.name)
                value(i + 1, 2, if (insulin.isBasal) "TRUE" else "FALSE")
                value(i + 1, 3, if (insulin.hasHalfUnits) "TRUE" else "FALSE")
            }
        }

        sheet.range(0, 0, list.size, headers.size - 1)
            .style()
            .shadeAlternateRows(Color.GRAY2)
            .set()
    }
}