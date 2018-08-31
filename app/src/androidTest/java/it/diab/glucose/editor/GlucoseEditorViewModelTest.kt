package it.diab.glucose.editor

import androidx.lifecycle.ViewModelProviders
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import it.diab.db.AppDatabase
import it.diab.db.entities.Glucose
import it.diab.db.entities.glucose
import it.diab.db.entities.insulin
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

        val new = glucose {
            uid = 1
            value = 50
            insulinValue1 = 6f
            eatLevel = Glucose.LOW
            timeFrame = TimeFrame.DINNER
        }
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
        mViewModel!!.glucose.eatLevel = Glucose.MAX

        mViewModel!!.save()

        val finalSize = mDatabase!!.glucose().allStatic.size
        assert(finalSize == initialSize + 1)
    }

    @Test
    fun getInsulin() {
        val new = insulin {
            uid = (50..60).random().toLong()
            name = "FooBar"
            timeFrame = TimeFrame.DINNER
            hasHalfUnits = true
        }
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
        val insulin = insulin {
            uid = (100..133).random().toLong()
            timeFrame = targetTimeFrame
            isBasal = true
        }
        mDatabase!!.insulin().insert(insulin)

        val glucose = glucose {
            date = a.time
            timeFrame = TimeFrame.LUNCH
        }

        assert(mViewModel!!.hasPotentialBasal(glucose))

        glucose.date = b.time
        assert(!mViewModel!!.hasPotentialBasal(glucose))
    }

    @Test
    fun getInsulinByTimeFrame() {
        val targetTimeFrame = TimeFrame.NIGHT

        val test = insulin {
            uid = (60..90).random().toLong()
            name = "FooBar"
            timeFrame = targetTimeFrame
            isBasal = true
        }

        mDatabase!!.insulin().insert(test)

        val result = mViewModel!!.getInsulinByTimeFrame(targetTimeFrame)
        assert(result.uid == test.uid)
        assert(result == test)
    }

    @Test
    fun applyInsulinSuggestion() {
        val insulin = insulin {
            uid = (0..20).random().toLong()
            name = "FooBar"
            timeFrame = TimeFrame.MORNING
        }
        val test = (0..10).random().toFloat()

        mViewModel!!.setGlucose(-1)
        mViewModel!!.applyInsulinSuggestion(test, insulin) {}

        assert(mViewModel!!.glucose.insulinValue0 == test)
        assert(mViewModel!!.glucose.insulinId0 == insulin.uid)
    }
}

private fun ClosedRange<Int>.random() = Random().nextInt(endInclusive - start) + start
