/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtTest {

    @Test
    fun upperCaseFirstChar() {
        assertEquals("test".upperCaseFirstChar(), "Test")
        assertEquals("TEST".upperCaseFirstChar(), "TEST")
    }
}