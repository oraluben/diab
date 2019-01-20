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
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import it.diab.BuildConfig
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.util.DateUtils
import it.diab.util.extensions.forEachWithIndex
import it.diab.util.extensions.format
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

        startForeground(RUNNING_NOTIFICATION_ID, notification)

        val targetAction = intent?.getIntExtra(EXPORT_TARGET, -1) ?: -1
        when (targetAction) {
            TARGET_CSV -> exportCsv(this::onTaskCompleted)
            TARGET_XLSX -> exportXlxs(this::onTaskCompleted)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL)
        .setSmallIcon(R.drawable.ic_export)
        .setContentTitle(getString(R.string.export_notification_title))
        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
        .setProgress(100, 10, true)
        .build()

    private fun buildCompletedNotification(file: File?, success: Boolean) {
        val message = getString(if (success) R.string.export_completed_success else R.string.export_completed_failure)

        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_export)
            .setContentTitle(getString(R.string.export_notification_title))
            .setContentText(message)
            .setColor(ContextCompat.getColor(this, R.color.colorAccent))

        if (success && file != null && file.exists()) {
            val uri = FileProvider.getUriForFile(this, "it.diab.files", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "application/octet-stream"
            }

            val sharePendingIntent = PendingIntent.getActivity(this, 0,
                Intent.createChooser(shareIntent, getString(R.string.share)), PendingIntent.FLAG_CANCEL_CURRENT)

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, contentResolver.getType(uri))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val openPendingIntent = PendingIntent.getActivity(this, 0,
                Intent.createChooser(openIntent, getString(R.string.share)), PendingIntent.FLAG_CANCEL_CURRENT)

            notification.addAction(R.drawable.ic_export, getString(R.string.share), sharePendingIntent)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
        }

        notificationManager.notify(COMPLETED_NOTIFICATION_ID, notification.build())
    }

    private fun exportCsv(onTaskCompleted: (File?, Boolean) -> Unit) {
        serviceScope.launch {
            val result = exportCsv(this, glucoseRepository)
            GlobalScope.launch(Dispatchers.Main) { onTaskCompleted(null, result) }
        }
    }

    private fun exportXlxs(onTaskCompleted: (File?, Boolean) -> Unit) {
        val glucoseHeaders = listOf(
            getString(R.string.export_sheet_glucose_value),
            getString(R.string.export_sheet_glucose_date),
            getString(R.string.export_sheet_glucose_eat),
            getString(R.string.export_sheet_glucose_insulin),
            getString(R.string.export_sheet_glucose_insulin_value),
            getString(R.string.export_sheet_glucose_basal),
            getString(R.string.export_sheet_glucose_insulin_value)
        )
        val insulinHeaders = listOf(
            getString(R.string.export_sheet_insulin_name),
            getString(R.string.export_sheet_insulin_time_frame),
            getString(R.string.export_sheet_insulin_basal),
            getString(R.string.export_sheet_insulin_half_units)
        )

        serviceScope.launch {
            val result = exportSheet(this, glucoseRepository, insulinRepository, glucoseHeaders, insulinHeaders)
            GlobalScope.launch(Dispatchers.Main) { onTaskCompleted(result, result != null) }
        }
    }

    private fun onTaskCompleted(output: File?, result: Boolean) {
        notificationManager.cancel(RUNNING_NOTIFICATION_ID)
        buildCompletedNotification(output, result)
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
    ): File? {
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
                return file
            } catch (e: IOException) {
                Log.e(TAG, e.message)
            }
        }

        return null
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
        const val RUNNING_NOTIFICATION_ID = 1927
        private const val COMPLETED_NOTIFICATION_ID = 1928

        const val EXPORT_TARGET = "export_target"
        const val TARGET_CSV = 0
        const val TARGET_XLSX = 1

        private const val TAG = "ExportService"
        private const val CHANNEL = "exportChannel"
        private const val FULL_HEADER = "value,eatLevel,insulin\n"
    }
}
