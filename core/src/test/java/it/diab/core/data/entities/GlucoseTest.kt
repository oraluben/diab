/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.core.data.entities

import it.diab.core.util.extensions.glucose
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GlucoseTest {

    @Test
    fun defaults() {
        glucose { uid = 12 }.run {
            assertEquals(value, 0)
            assertEquals(insulinId0, -1)
            assertEquals(insulinValue0, 0f)
            assertEquals(insulinId1, -1)
            assertEquals(insulinValue1, 0f)
            assertEquals(eatLevel, Glucose.MEDIUM)
            assertEquals(timeFrame, TimeFrame.EXTRA)
        }
    }
}