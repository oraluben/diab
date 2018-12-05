/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object SystemUtil {

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
}