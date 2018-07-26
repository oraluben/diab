package it.diab.db

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import it.diab.db.dao.GlucoseDao
import it.diab.db.dao.InsulinDao
import it.diab.db.entities.Glucose
import it.diab.db.entities.glucose
import it.diab.db.entities.insulin
import it.diab.util.timeFrame.TimeFrame
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private var glucoseDao: GlucoseDao? = null
    private var insulinDao: InsulinDao? = null

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true
        val instance = AppDatabase.getInstance(InstrumentationRegistry.getContext())
        glucoseDao = instance.glucose()
        insulinDao = instance.insulin()
    }

    @Test
    fun addGlucose() {
        val item = glucose {
            uid = 1
            value = 100
            eatLevel = Glucose.MEDIUM
            timeFrame = TimeFrame.MORNING
        }

        glucoseDao?.insert(item)
        val test = glucoseDao?.getById(1)!![0]
        Assert.assertEquals(item, test)
    }

    @Test
    fun addInsulin() {
        val item = insulin {
            uid = 1
            name = "TEST 0"
            timeFrame = TimeFrame.LUNCH
            hasHalfUnits = true
        }

        insulinDao?.insert(item)
        val test = insulinDao?.getById(1)!![0]
        Assert.assertEquals(item, test)
    }

    @Test
    fun addGlucoseWithLinkedInsulins() {
        val insulin = insulin {
            uid = 2
            name = "TEST 1"
            timeFrame = TimeFrame.DINNER
        }

        val basal = insulin {
            uid = 3
            name = "TEST 2"
            timeFrame = TimeFrame.DINNER
            isBasal = true
            hasHalfUnits = true
        }

        insulinDao?.insert(insulin)
        insulinDao?.insert(basal)

        val glucose = glucose {
            uid = 2
            value = 100
            insulinId0 = insulin.uid
            insulinValue0 = 3f
            insulinId1 = basal.uid
            insulinValue1 = 4f
            eatLevel = Glucose.HIGH
            timeFrame = TimeFrame.EXTRA
        }

        glucoseDao?.insert(glucose)

        val test = glucoseDao?.getById(2)!![0]
        val test1 = insulinDao?.getById(test.insulinId0)!![0]
        val test2 = insulinDao?.getById(test.insulinId1)!![0]

        Assert.assertEquals(insulin.uid, test1.uid)
        Assert.assertEquals(basal.uid, test2.uid)
    }
}
