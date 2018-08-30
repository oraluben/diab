package it.diab.insulin.editor

import androidx.lifecycle.ViewModelProviders
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import it.diab.db.AppDatabase
import it.diab.db.entities.Insulin
import it.diab.db.entities.insulin
import it.diab.util.timeFrame.TimeFrame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InsulinEditorViewModelTest {
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
    fun setInsulin() {
        mViewModel!!.setInsulin(-1)

        val test = mViewModel!!.insulin
        assert(test.uid == 0L)

        val new = insulin {
            uid = 1
            name = "FooBar"
            timeFrame = TimeFrame.LUNCH
            hasHalfUnits = true
        }
        mDatabase!!.insulin().insert(new)

        mViewModel!!.setInsulin(new.uid)

        assert(mViewModel!!.insulin.uid == new.uid)
        assert(mViewModel!!.insulin == new)

        mDatabase!!.insulin().delete(new)
    }

    @Test
    fun delete() {
        val insulins = mDatabase!!.insulin().allStatic
        assert(insulins.isNotEmpty())

        val test = insulins[0]

        mViewModel!!.delete(test)

        val result = mDatabase!!.insulin().getById(test.uid)
        assert(result.isEmpty())
    }


    @Test
    fun save() {
        val initialSize = mDatabase!!.insulin().allStatic.size

        mViewModel!!.setInsulin(-1)

        mViewModel!!.insulin.name = "barFoo"
        mViewModel!!.insulin.timeFrame = TimeFrame.LATE_MORNING
        mViewModel!!.insulin.isBasal = true
        mViewModel!!.insulin.hasHalfUnits = false

        mViewModel!!.save()

        val finalSize = mDatabase!!.insulin().allStatic.size
        assert(finalSize == initialSize + 1)
    }
}
