package it.diab

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

@Suppress("unused")
class DiabApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (!LeakCanary.isInAnalyzerProcess(this) &&
            LeakCanary.installedRefWatcher() == RefWatcher.DISABLED) {
            LeakCanary.install(this)
        }
    }
}