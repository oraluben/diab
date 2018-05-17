package it.diab.db

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import it.diab.db.dao.GlucoseDao
import it.diab.db.dao.InsulinDao
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.util.timeFrame.TimeFrame
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

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

    @After
    fun tearDown() = Unit

    @Test
    fun addGlucose() {
        val item = Glucose(1, 100, Date(), -1, 0f, -1, 0f, 1)
        glucoseDao?.insert(item)
        val test = glucoseDao?.getById(1)!![0]
        Assert.assertEquals(item, test)
    }

    @Test
    fun addInsulin() {
        val item = Insulin(1, "TEST 0",TimeFrame.LUNCH, false, true)
        insulinDao?.insert(item)
        val test = insulinDao?.getById(1)!![0]
        Assert.assertEquals(item, test)
    }

    @Test
    fun addGlucoseWithLinkedInsulins() {
        val insulin = Insulin(2, "TEST 1", TimeFrame.DINNER, false, false)
        val basal = Insulin(3, "TEST 2", TimeFrame.DINNER, true, true)
        insulinDao?.insert(insulin)
        insulinDao?.insert(basal)

        val glucose = Glucose(2, 100, Date(), insulin.uid, 3f, basal.uid, 4f, 2)
        glucoseDao?.insert(glucose)

        val test = glucoseDao?.getById(2)!![0]
        val test1 = insulinDao?.getById(test.insulinId0)!![0]
        val test2 = insulinDao?.getById(test.insulinId1)!![0]

        Assert.assertEquals(insulin.uid, test1.uid)
        Assert.assertEquals(basal.uid, test2.uid)
    }
}