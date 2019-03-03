/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.data.entities

import it.diab.core.util.extensions.insulin
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class InsulinTest {

    @Test
    fun defaults() {
        insulin { uid = 12 }.run {
            assertEquals(name, "")
            assertEquals(timeFrame, TimeFrame.EXTRA)
            assertEquals(isBasal, false)
            assertEquals(hasHalfUnits, false)
        }
    }

    @Test
    fun setTimeFrame() {
        val insulin = insulin { timeFrame = TimeFrame.MORNING }
        insulin.setTimeFrame(5)

        assertEquals(insulin.timeFrame, TimeFrame.NIGHT)
    }

    @Test
    fun getDisplayedString() {
        val insulin = insulin { name = "test" }

        assertEquals(insulin.getDisplayedString(10.54f), "test 10.5")
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

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}