/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import it.diab.BuildConfig
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.util.DateUtils
import it.diab.util.extensions.forEachWithIndex
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhatim.fastexcel.Color
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportService : Service() {
    private lateinit var glucoseRepository: GlucoseRepository
    private lateinit var insulinRepository: InsulinRepository
    private lateinit var notificationManager: NotificationManager

    private val job = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()

        glucoseRepository = GlucoseRepository.getInstance(this)
        insulinRepository = InsulinRepository.getInstance(this)
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!job.isCompleted) {
            job.cancel()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIfNeeded()
        }

        startForeground(NOTIFICATION_ID, notification)

        val targetAction = intent?.getIntExtra(EXPORT_TARGET, -1) ?: -1
        when (targetAction) {
            TARGET_CSV -> exportCsv(this::onTaskCompleted)
            TARGET_XLSX -> exportXlxs(this::onTaskCompleted)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL)
        .setContentTitle(getString(R.string.export_notification_title))
        .setSmallIcon(R.drawable.ic_export)
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setProgress(100, 10, true)
        .build()

    private fun exportCsv(onTaskCompleted: (Boolean) -> Unit) {
        serviceScope.launch {
            val result = exportCsv(this, glucoseRepository)
            GlobalScope.launch(Dispatchers.Main) { onTaskCompleted(result) }
        }
    }

    private fun exportXlxs(onTaskCompleted: (Boolean) -> Unit) {
        val glucoseHeaders = listOf(
            getString(R.string.add)
        )
        val insulinHeaders = listOf(
            getString(R.string.add)
        )

        serviceScope.launch {
            val result = exportSheet(this, glucoseRepository, insulinRepository, glucoseHeaders, insulinHeaders)
            GlobalScope.launch(Dispatchers.Main) { onTaskCompleted(result) }
        }
    }

    private fun onTaskCompleted(result: Boolean) {
        notificationManager.cancel(NOTIFICATION_ID)

        Toast.makeText(
            this,
            if (result) R.string.export_completed_success
            else R.string.export_completed_failure, Toast.LENGTH_LONG
        ).show()

        stopSelf()
    }

    @RequiresApi(26)
    private fun createChannelIfNeeded() {
        val channel = notificationManager.getNotificationChannel(CHANNEL)
        if (channel != null) {
            return
        }

        val name = getString(R.string.export_notification_channel)
        val newChannel = NotificationChannel(CHANNEL, name, NotificationManager.IMPORTANCE_LOW).apply {
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
        }

        notificationManager.createNotificationChannel(newChannel)
    }

    private suspend fun exportSheet(
        scope: CoroutineScope,
        glucoseRepository: GlucoseRepository,
        insulinRepository: InsulinRepository,
        glucoseHeaders: List<String>,
        insulinHeaders: List<String>
    ): Boolean {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val outDir = File(documentsDir, "diab").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val file = File(outDir, "diab-${SimpleDateFormat("yyyy-MM-dd").format(Date())}.xlsx")

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

            try {
                glucoseDeferred.await()
                insulinDeferred.await()
                workBook.finish()
                return true
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }
        }

        return false
    }

    @WorkerThread
    private fun buildGlucoseSheet(
        sheet: Worksheet,
        glucoseRepository: GlucoseRepository,
        insulinRepository: InsulinRepository,
        headers: List<String>
    ) {
        headers.forEachWithIndex { i, str -> sheet.value(0, i, str) }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val list = glucoseRepository.getAllItems()
        list.forEachWithIndex { i, glucose ->
            sheet.run {
                val insulin = insulinRepository.getById(glucose.insulinId0)
                val basal = insulinRepository.getById(glucose.insulinId1)

                value(i + 1, 0, glucose.value)
                value(i + 1, 1, dateFormat.format(glucose.date))
                value(i + 1, 2, glucose.eatLevel)
                if (insulin.name.isNotEmpty()) {
                    value(i + 1, 3, "${insulin.name}: ${glucose.insulinValue0}")
                }
                if (basal.name.isNotEmpty()) {
                    value(i + 1, 4, "${basal.name}: ${glucose.insulinValue1}")
                }
            }
        }

        sheet.range(0, 0, list.size, headers.size)
            .style()
            .shadeAlternateRows(Color.GRAY2)
            .set()
    }

    @WorkerThread
    private fun buildInsulinSheet(
        sheet: Worksheet,
        insulinRepository: InsulinRepository,
        headers: List<String>
    ) {
        headers.forEachWithIndex { i, str -> sheet.value(0, i, str) }

        val list = insulinRepository.getInsulins()
        list.forEachWithIndex { i, insulin ->
            sheet.run {
                value(i + 1, 0, insulin.name)
                value(i + 1, 1, insulin.timeFrame.name)
                value(i + 1, 2, if (insulin.isBasal) "TRUE" else "FALSE")
                value(i + 1, 3, if (insulin.hasHalfUnits) "TRUE" else "FALSE")
            }
        }

        sheet.range(0, 0, list.size, headers.size)
            .style()
            .shadeAlternateRows(Color.GRAY2)
            .set()
    }

    private suspend fun exportCsv(scope: CoroutineScope, repository: GlucoseRepository): Boolean {
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

        try {
            morningDeferred.await()
            lunchDeferred.await()
            dinnerDeferred.await()
        } catch (e: IOException) {
            return false
        }

        return true
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

    companion object {
        const val NOTIFICATION_ID = 1927

        const val EXPORT_TARGET = "export_target"
        const val TARGET_CSV = 0
        const val TARGET_XLSX = 1

        private const val TAG = "ExportService"
        private const val CHANNEL = "exportChannel"
        private const val FULL_HEADER = "value,eatLevel,insulin\n"
    }
}
