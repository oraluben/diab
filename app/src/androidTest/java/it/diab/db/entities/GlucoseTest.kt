package it.diab.db.entities

import android.os.Parcel
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import it.diab.util.extensions.get
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
            date = Date()[4]
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
        assert(restored.uid == original.uid)
        assert(restored == original)
    }
}
