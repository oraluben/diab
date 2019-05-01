/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.data.entities

import it.diab.data.extensions.insulin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class InsulinTest {

    @Test
    fun defaults() {
        insulin { uid = 12 }.run {
            assertEquals("", name)
            assertEquals(TimeFrame.EXTRA, timeFrame)
            assertFalse(isBasal)
            assertFalse(hasHalfUnits)
        }
    }

    @Test
    fun setTimeFrame() {
        val insulin = insulin { timeFrame = TimeFrame.MORNING }
        insulin.setTimeFrame(5)

        assertEquals(TimeFrame.NIGHT, insulin.timeFrame)
    }

    @Test
    fun getDisplayedString() {
        val insulin = insulin { name = "test" }

        assertEquals("test 10.5", insulin.getDisplayedString(10.54f))
    }

    @Test
    fun equals() {
        val a = insulin {
            uid = 1
            name = "test"
        }

        val b = insulin {
            uid = 2
            name = "test"
        }

        assertTrue(a == b)
        assertTrue(a.hashCode() == b.hashCode())
    }
}