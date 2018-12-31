/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.annotation.StringRes
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.InvocationTargetException

object SystemUtil {
    private const val TAG = "SystemUtil"

    fun getProp(prop: String, default: String): String = try {
        val process = Runtime.getRuntime().exec("getprop $prop")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        val output = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            output.append(line)
            line = reader.readLine()
        }

        output.toString()
    } catch (e: IOException) {
        default
    }

    /*
     * Method: getOverrideObject(Class<T>, Context, Int): T
     *
     * Copyright (C) 2008 The Android Open Source Project
     * Licensed under the Apache License Version 2.0
     *
     * From aosp: android_packages_apps_Launcher3/src/com/android/launcher3/Utilities.java
     */
    fun <T> getOverrideObject(clazz: Class<T>, context: Context, @StringRes resId: Int): T {
        val className = context.getString(resId)

        if (!TextUtils.isEmpty(className)) {
            try {
                val cls = Class.forName(className)
                @Suppress("UNCHECKED_CAST")
                return cls.getDeclaredConstructor().newInstance() as T
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "Bad overriden class", e)
            } catch (e: InstantiationException) {
                Log.e(TAG, "Bad overriden class", e)
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "Bad overriden class", e)
            } catch (e: ClassCastException) {
                Log.e(TAG, "Bad overriden class", e)
            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "Bad overriden class", e)
            } catch (e: InvocationTargetException) {
                Log.e(TAG, "Bad overriden class", e)
            }
        }

        try {
            return clazz.newInstance()
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }
}