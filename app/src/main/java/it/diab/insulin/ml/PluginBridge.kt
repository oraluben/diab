package it.diab.insulin.ml

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import it.diab.util.timeFrame.TimeFrame
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class PluginBridge(context: Context) {
    private val mAssetManager: AssetManager
    private var mIsAvailable = false

    private val emptyStream: InputStream
        get() = ByteArrayInputStream("{\n}".toByteArray(Charsets.UTF_8))

    init {
        mAssetManager = if (hasPlugin(context)) {
            mIsAvailable = true
            val plugin = context.createPackageContext(PLUGIN_PACKAGE_NAME, 0)
            plugin.assets
        } else {
            context.assets
        }
    }

    fun getStreamFor(timeFrame: TimeFrame): InputStream? {
        if (!mIsAvailable) {
            return emptyStream
        }

        val assetName = MODEL_NAME.format(timeFrame.ordinal)
        return try {
            mAssetManager.open(assetName)
        } catch (e: IOException) {
            emptyStream
        }
    }

    private fun hasPlugin(context: Context) = try {
        context.packageManager.getApplicationInfo(PLUGIN_PACKAGE_NAME, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }


    companion object {
        private const val PLUGIN_PACKAGE_NAME = "it.diab.plugin"
        private const val MODEL_NAME = "estimator_%1\$d.json"
    }
}