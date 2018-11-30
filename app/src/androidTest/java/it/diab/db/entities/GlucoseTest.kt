/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.db.entities

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import it.diab.util.extensions.glucose
import it.diab.util.timeFrame.TimeFrame
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
@SmallTest
class GlucoseTest {

    @Test
    fun writeReadParcel() {
        val original = glucose {
            uid = 32
            value = 103
            date = Date().apply { time -= (4 * 24 * 60 * 60 * 1000) }
            insulinId0 = 3
            insulinValue0 = 12.4f
            eatLevel = 2
            timeFrame = TimeFrame.LATE_MORNING
        }

        // Write
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, original.describeContents())

        // Move to the top
        parcel.setDataPosition(0)

        // Read
        val restored = Glucose.createFromParcel(parcel)
        assertThat(restored.uid).isEqualTo(original.uid)
        assertThat(restored).isEqualTo(original)
    }
}
