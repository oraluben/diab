package it.diab

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import it.diab.db.DbThread

@Suppress("unused")
class DiabApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
        }
    }

    override fun onTerminate() {
        // Stop the jobs in the db thread
        DbThread.shutDown()

        super.onTerminate()
    }
}