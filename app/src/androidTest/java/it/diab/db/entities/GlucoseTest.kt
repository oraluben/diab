package it.diab.db.entities

import android.os.Parcel
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import it.diab.util.extensions.get
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class GlucoseTest {

    @Test
    fun writeReadParcel() {
        val original = Glucose(32, 103, Date()[4], 3, 12.4f, -1, 0f, 2)

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