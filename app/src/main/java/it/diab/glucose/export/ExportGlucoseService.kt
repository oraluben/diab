package it.diab.glucose.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import it.diab.R
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.util.DateUtils
import it.diab.util.extensions.asTimeFrame
import it.diab.util.timeFrame.TimeFrame
import java.io.File
import java.io.FileWriter
import java.io.IOException

class ExportGlucoseService : Service() {
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mNotificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        mAppDatabase = AppDatabase.getInstance(this)
        mNotificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelIfNeeded()
        }

        val task = WriteTrainTask(mAppDatabase, this::onTaskCompleted)

        startForeground(NOTIFICATION_ID, notification)

        task.execute()
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
        val newChannel = NotificationChannel(CHANNEL, name, NotificationManager.IMPORTANCE_LOW)
        newChannel.enableLights(false)
        newChannel.enableVibration(false)
        newChannel.setShowBadge(false)
        newChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET

        mNotificationManager.createNotificationChannel(newChannel)
    }

    private class WriteTrainTask(private val db: AppDatabase,
                                 private val onCompleted: (Boolean) -> Unit) :
            AsyncTask<Unit, Unit, Boolean>() {

        override fun doInBackground(vararg params: Unit?): Boolean {
            val end = System.currentTimeMillis()
            val start = end - DateUtils.DAY * 40
            val list = db.glucose().getInDateRange(start, end)

            try {
                val docsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS)
                val outDir = File(docsDir, "diab")

                if (!outDir.exists()) {
                    outDir.mkdirs()
                }

                val baseName = "train_"

                build(list.filter { it.date.asTimeFrame() == TimeFrame.MORNING },
                        "${baseName}1.csv", outDir)
                build(list.filter { it.date.asTimeFrame() == TimeFrame.LUNCH },
                        "${baseName}3.csv", outDir)
                build(list.filter { it.date.asTimeFrame() == TimeFrame.DINNER },
                        "${baseName}5.csv", outDir)
            } catch (e: IOException) {
                return false
            }

            // Take a nap so the user can see the progress notification
            Thread.sleep(2000)
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            if (result != null) {
                onCompleted(result)
            }

            super.onPostExecute(result)
        }

        private fun build(list: List<Glucose>, fileName: String, parentDir: File) {
            val file = File(parentDir, fileName)
            val writer = FileWriter(file)
            val builder = StringBuilder()

            list.forEach { builder.append("${it.value},${it.eatLevel},${it.insulinValue0}\n") }

            file.mkdirs()
            writer.write(FULL_HEADER)
            writer.write(builder.toString())
            writer.close()
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1927
        private const val CHANNEL = "exportChannel"
        private const val FULL_HEADER = "value,eatLevel,insulin\n"
    }
}
