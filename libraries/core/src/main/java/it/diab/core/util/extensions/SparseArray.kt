/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.util.extensions

import android.util.SparseArray

fun <T> Map<Int, T>.toSparseArray(): SparseArray<T> {
    val array = SparseArray<T>()
    keys.forEach { array.put(it, get(it)) }
    return array
}

fun <T> List<Pair<Int, T>>.toSparseArray(): SparseArray<T> {
    val array = SparseArray<T>()
    forEach {
        array.put(it.first, it.second)
    }
    return array
}

fun <T> SparseArray<T>.containsKey(key: Int) = get(key) != null