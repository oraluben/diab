package it.diab.glucose.editor

import android.arch.lifecycle.ViewModelProviders
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.get
import it.diab.util.timeFrame.TimeFrame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class GlucoseEditorViewModelTest {
    private var mViewModel: EditorViewModel? = null
    private var mDatabase: AppDatabase? = null

    @Suppress("MemberVisibilityCanBePrivate")
    @get:Rule
    val testRule = ActivityTestRule<EditorActivity>(EditorActivity::class.java)

    @Before
    fun setup() {
        mViewModel = ViewModelProviders.of(testRule.activity)[EditorViewModel::class.java]

        AppDatabase.TEST_MODE = true
        mDatabase = AppDatabase.getInstance(testRule.activity)
    }

    @Test
    fun setGlucose() {
        mViewModel!!.setGlucose(-1)

        val test = mViewModel!!.glucose
        assert(test.uid == 0L)

        val new = Glucose(1, 50, Date(), -1L, 0f, 1L, 6f, Glucose.LOW)
        mDatabase!!.glucose().insert(new)

        mViewModel!!.setGlucose(new.uid)

        assert(mViewModel!!.glucose.uid == new.uid)
        assert(mViewModel!!.glucose == new)

        mDatabase!!.glucose().delete(new)
    }

    @Test
    fun save() {
        val initialSize = mDatabase!!.glucose().allStatic.size

        mViewModel!!.setGlucose(-1)

        mViewModel!!.glucose.value = 173
        mViewModel!!.glucose.date = Date()[-6]
        mViewModel!!.glucose.insulinId0 = 0L
        mViewModel!!.glucose.insulinValue0 = 10.5f
        mViewModel!!.glucose.insulinId1 = -1L
        mViewModel!!.glucose.insulinValue1 = 1f
        mViewModel!!.glucose.eatLevel = Glucose.MAX

        mViewModel!!.save()

        val finalSize = mDatabase!!.glucose().allStatic.size
        assert(finalSize == initialSize + 1)
    }

    @Test
    fun getInsulin() {
        val new = Insulin((50..60).random().toLong(), "FooBar", TimeFrame.DINNER, false, true)
        mDatabase!!.insulin().insert(new)

        val test = mViewModel!!.getInsulin(new.uid)

        assert(test.uid == new.uid)
        assert(test == new)
    }

    @Test
    fun hasPotentialBasal() {
        val a = Calendar.getInstance()
        val b = Calendar.getInstance()

        a[Calendar.HOUR_OF_DAY] = 12
        b[Calendar.HOUR_OF_DAY] = 20

        val targetTimeFrame = a.time.asTimeFrame()
        val insulin = Insulin((100..133).random().toLong(), "FooBar", targetTimeFrame, true, false)
        mDatabase!!.insulin().insert(insulin)

        val glucose = Glucose(-1, 106, a.time, -1L, 0f, -1L, 0f, Glucose.MEDIUM)
        assert(mViewModel!!.hasPotentialBasal(glucose))

        glucose.date = b.time
        assert(!mViewModel!!.hasPotentialBasal(glucose))
    }

    @Test
    fun getInsulinByTimeFrame() {
        val targetTimeFrame = TimeFrame.NIGHT

        val test = Insulin((60..90).random().toLong(), "FooBar", targetTimeFrame, true, false)
        mDatabase!!.insulin().insert(test)

        val result = mViewModel!!.getInsulinByTimeFrame(targetTimeFrame)
        assert(result.uid == test.uid)
        assert(result == test)
    }

    @Test
    fun applyInsulinSuggestion() {
        val insulin = Insulin((0..20).random().toLong(), "FooBar", TimeFrame.MORNING, false, false)
        val test = (0..10).random().toFloat()

        mViewModel!!.setGlucose(-1)
        mViewModel!!.applyInsulinSuggestion(test, insulin, {})

        assert(mViewModel!!.glucose.insulinValue0 == test)
        assert(mViewModel!!.glucose.insulinId0 == insulin.uid)
    }

    private fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start
}
