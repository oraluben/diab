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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import it.diab.R
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.repositories.GlucoseRepository
import it.diab.util.DateUtils
import it.diab.util.timeFrame.TimeFrame
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ExportGlucoseService : Service() {
    private lateinit var repository: GlucoseRepository
    private lateinit var mNotificationManager: NotificationManager

    private val job = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()

        repository = GlucoseRepository.getInstance(this)
        mNotificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIfNeeded()
        }

        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            val defResult = async { exportFiles(this, repository) }

            val result = defResult.await()

            delay(1500)
            GlobalScope.launch(Dispatchers.Main) { onTaskCompleted(result) }
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

    private fun onTaskCompleted(result: Boolean) {
        mNotificationManager.cancel(NOTIFICATION_ID)

        Toast.makeText(this,
                if (result) R.string.export_completed_success
                else R.string.export_completed_failure, Toast.LENGTH_LONG).show()

        stopSelf()
    }

    @RequiresApi(26)
    private fun createChannelIfNeeded() {
        val channel = mNotificationManager.getNotificationChannel(CHANNEL)
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

        mNotificationManager.createNotificationChannel(newChannel)
    }

    private suspend fun exportFiles(scope: CoroutineScope, repository: GlucoseRepository): Boolean {
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
        private const val CHANNEL = "exportChannel"
        private const val FULL_HEADER = "value,eatLevel,insulin\n"
    }
}
