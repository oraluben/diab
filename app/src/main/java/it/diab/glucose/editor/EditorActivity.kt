package it.diab.glucose.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.annotation.IdRes
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.transition.TransitionManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.robinhood.spark.SparkView
import it.diab.R
import it.diab.db.entities.Glucose
import it.diab.db.entities.Insulin
import it.diab.ui.NumericKeyboardView
import it.diab.ui.graph.SimpleSparkAdapter
import it.diab.util.DateUtils
import it.diab.util.extensions.asTimeFrame
import java.util.*

class EditorActivity : AppCompatActivity() {

    private lateinit var mViewModel: EditorViewModel

    private lateinit var mConstraintRoot: ConstraintLayout
    private lateinit var mValueView: TextView
    private lateinit var mDateView: TextView
    private lateinit var mInsulin0: InsulinView
    private lateinit var mInsulin1: InsulinView
    private lateinit var mGraphView: SparkView
    private lateinit var mInfoView: TextView
    private lateinit var mKeyboardView: NumericKeyboardView
    private lateinit var mFab: FloatingActionButton

    private var mEditMode = false

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        setContentView(R.layout.activity_glucose_view)

        // Common Views
        mConstraintRoot = findViewById(R.id.glucose_editor_root)
        mValueView = findViewById(R.id.glucose_editor_edit_value)
        mDateView = findViewById(R.id.glucose_editor_edit_date)
        mInsulin0 = InsulinView(
                R.id.glucose_editor_insulin_0_layout,
                R.id.glucose_editor_insulin_0_title,
                R.id.glucose_editor_insulin_0_value)
        mInsulin1 = InsulinView(
                R.id.glucose_editor_insulin_1_layout,
                R.id.glucose_editor_insulin_1_title,
                R.id.glucose_editor_insulin_1_value)
        mFab = findViewById(R.id.fab)

        // Viewer Views
        mGraphView = findViewById(R.id.glucose_editor_graph)
        mInfoView = findViewById(R.id.glucose_editor_info)

        // Editor Views
        mKeyboardView = findViewById(R.id.glucose_editor_keyboard)

        mEditMode = intent.getBooleanExtra(EXTRA_ADD_MODE, false)
        val id = intent.getLongExtra(EXTRA_GLUCOSE_ID, -1)

        mViewModel = ViewModelProviders.of(this).get(EditorViewModel::class.java)
        mViewModel.setGlucose(id)

        setup()
    }

    public override fun onResume() {
        super.onResume()

        change()
    }

    private fun setup() {
        mValueView.text = mViewModel.glucose.value.toString()
        mDateView.text = DateUtils.dateToString(mViewModel.glucose.date)

        mKeyboardView.bindTextView(mValueView)

        mFab.setOnClickListener { _ ->
            if (mEditMode) {
                save()
            } else {
                edit()
            }
        }

        mDateView.setOnClickListener { _ ->
            if (mEditMode) {
                pickDate()
            }
        }

        mInsulin0.layout.setOnClickListener { _ -> showInsulinDialog(true) }
        mInsulin1.layout.setOnClickListener { _ ->
            if (mViewModel.glucose.insulinId0 != -1L) {
                showInsulinDialog(false)
            }
        }
    }

    private fun save() {
        if (hasErrors()) {
            return
        }

        Snackbar.make(mConstraintRoot, getString(R.string.saved), 800).show()
        Handler().postDelayed({
            saveData()
            finish()
        }, 1000)
    }

    private fun saveData() {
        mViewModel.glucose.value = mKeyboardView.input
        mViewModel.save()
    }

    private fun edit() {
        Handler().postDelayed({
            mEditMode = true
            val editSet = ConstraintSet()
            editSet.clone(this, R.layout.constraint_glucose_edit)
            TransitionManager.beginDelayedTransition(mConstraintRoot)
            editSet.applyTo(mConstraintRoot)

            change()
        }, 350)
    }

    private fun change() {
        if (mEditMode) {
            changeToEdit()
        } else {
            changeToShow()
        }

        mFab.setImageResource(if (mEditMode) R.drawable.ic_done else R.drawable.ic_edit)
    }

    private fun changeToEdit() {
        val editMode = ConstraintSet()
        editMode.clone(this, R.layout.constraint_glucose_edit)
        editMode.applyTo(mConstraintRoot)

        mInsulin0.layout.visibility = View.GONE
        mInsulin1.layout.visibility = View.GONE
    }

    private fun changeToShow() {
        mInsulin0.layout.visibility = View.VISIBLE

        val id = Pair(mViewModel.glucose.insulinId0, mViewModel.glucose.insulinId1)



        if (id.first == -1L) {
            mInsulin0.title.text = getString(R.string.glucose_editor_insulin_add)
            mInsulin0.value.text = ""

            mInsulin1.title.text = ""
        } else {
            val i0 = mViewModel.getInsulin(id.first)
            mInsulin0.title.text = getString(R.string.glucose_editor_insulin)
            mInsulin0.value.text = i0.getDisplayedString(mViewModel.glucose.insulinValue0)

            mInsulin1.layout.visibility = View.VISIBLE
            mInsulin1.title.text = getString(R.string.glucose_editor_insulin_add)
        }

        if (id.second == -1L) {
            mInsulin1.value.text = ""
        } else {
            val i1 = mViewModel.getInsulin(id.second)
            mInsulin1.title.text = ""
            mInsulin1.value.text = i1.getDisplayedString(mViewModel.glucose.insulinValue1)
        }

        val data = mViewModel.previousWeek
        mGraphView.adapter = SimpleSparkAdapter(data)
        mInfoView.text = getInformation(data)

        mDateView.text = DateUtils.dateToString(mViewModel.glucose.date)
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance()
        calendar.time = mViewModel.glucose.date

        val newTime = Calendar.getInstance()

        val onTimeSet = { _: View, hour: Int, minute: Int ->
            newTime.set(Calendar.HOUR_OF_DAY, hour)
            newTime.set(Calendar.MINUTE, minute)
            mViewModel.glucose.date = newTime.time
            mDateView.text = DateUtils.dateToString(newTime.time)
        }
        val onDateSet = { _: View, year: Int, month: Int, day: Int ->
            newTime.set(year, month, day)
            TimePickerDialog(this, onTimeSet, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true).show()
        }

        DatePickerDialog(this, onDateSet, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showInsulinDialog(isFirst: Boolean) {
        val dialog = AddInsulinDialog(this, mViewModel.glucose, isFirst)
        dialog.setInsulins(mViewModel.insulins)

        dialog.show(
                { insulin, value -> onInsulinPositive(insulin, value, isFirst) },
                { onInsulinNeutral(isFirst) },
                { changeToShow() })
    }

    private fun onInsulinPositive(insulin: Insulin, value: Float, isFirst: Boolean) {
        if (isFirst) {
            mViewModel.glucose.insulinId0 = insulin.uid
            mViewModel.glucose.insulinValue0 = value
            mInsulin0.value.text = insulin.getDisplayedString(value)
        } else {
            mViewModel.glucose.insulinId1 = insulin.uid
            mViewModel.glucose.insulinValue1 = value
            mInsulin1.value.text = insulin.getDisplayedString(value)
        }

        saveData()
    }

    private fun onInsulinNeutral(isFirst: Boolean) {
        if (isFirst) {
            mViewModel.glucose.insulinId0 = -1
            mViewModel.glucose.insulinValue0 = 0F
            mInsulin0.title.text = getString(R.string.glucose_editor_insulin_add)
        } else {
            mViewModel.glucose.insulinId1 = -1
            mViewModel.glucose.insulinValue1 = 0F
            mInsulin1.title.text =
                    if (mViewModel.glucose.insulinId0 == -1L) ""
                    else getString(R.string.glucose_editor_insulin_add)
        }

        saveData()
    }

    private fun hasErrors(): Boolean {
        if ("0" == mValueView.text) {
            mValueView.animate()
                    .translationY(-5f)
                    .setDuration(50)
                    .withEndAction {
                        mValueView.animate()
                                .translationY(10f)
                                .setDuration(100)
                                .withEndAction {
                                    mValueView.animate()
                                            .translationY(-5f)
                                            .setDuration(50)
                                            .start()
                                }
                                .start()
                    }
                    .start()

            Snackbar.make(mConstraintRoot, getString(R.string.glucose_editor_save_error),
                    Snackbar.LENGTH_LONG).show()
            return true
        }

        if (Date().time < mViewModel.glucose.date.time) {
            mDateView.animate()
                    .translationY(-5f)
                    .setDuration(50)
                    .withEndAction {
                        mDateView.animate()
                                .translationY(10f)
                                .setDuration(100)
                                .withEndAction {
                                    mDateView.animate()
                                            .translationX(-5f)
                                            .setDuration(50)
                                            .start()
                                }
                                .start()
                    }
                    .start()

            Snackbar.make(mConstraintRoot, getString(R.string.glucose_editor_save_error),
                    Snackbar.LENGTH_LONG).show()
            return true
        }

        return false
    }

    // TODO expose those magic numbers to userland
    private fun getInformation(list: List<Glucose>): String {
        var average = 0f
        val values = IntArray(list.size)
        for (i in list.indices) {
            values[i] = list[i].value
            average += values[i].toFloat()
        }
        average /= list.size.toFloat()

        val builder = StringBuilder()
        val timeFrame = mViewModel.glucose.date.asTimeFrame()
        val status = when {
            average > 180 -> R.string.glucose_type_high
            average > 70 -> R.string.glucose_type_medium
            else -> R.string.glucose_type_low
        }

        builder.append(getString(R.string.glucose_report_base,
                getString(timeFrame.string).toLowerCase(), getString(status), average.toInt()))
                .append('\n')

        if (status != R.string.glucose_type_medium) {
            var correction = 0
            val isLow = average > 70
            while (average >= 180 || average <= 70) {
                if (average >= 180) {
                    correction++
                    average -= 40f
                } else {
                    correction--
                    average += 40f
                }
            }

            correction = Math.abs(correction)

            builder.append(getString(R.string.glucose_report_advice,
                    getString(if (isLow)
                        R.string.glucose_report_advice_increase
                    else
                        R.string.glucose_report_advice_decrease), correction))
                    .append('\n')
        }

        for (i in values) {
            builder.append('\n')
                    .append("\u2022 ")
                    .append(i)
        }

        return builder.toString()
    }

    private inner class InsulinView(@IdRes layoutInt: Int,
                                    @IdRes titleInt: Int,
                                    @IdRes valueInt: Int) {
        val layout: LinearLayout = findViewById(layoutInt)
        val title: TextView = findViewById(titleInt)
        val value: TextView = findViewById(valueInt)
    }

    companion object {
        const val EXTRA_ADD_MODE = "GlucoseViewer_Extra_Add"
        const val EXTRA_GLUCOSE_ID = "GlucoseViewer_Extra_Id"
    }
}