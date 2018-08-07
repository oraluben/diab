package it.diab.insulin.editor

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter

import it.diab.R
import it.diab.util.timeFrame.TimeFrame

class EditorActivity : AppCompatActivity() {

    private lateinit var mEditText: AppCompatEditText
    private lateinit var mSpinner: AppCompatSpinner
    private lateinit var mBasalSwitch: SwitchCompat
    private lateinit var mHalfUnitsSwitch: SwitchCompat
    private lateinit var mDeleteButton: AppCompatButton

    private lateinit var mViewModel: EditorViewModel
    private lateinit var mTimeFrames: Array<String>
    private var mEditMode = false

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_insulin_edit)

        mViewModel = ViewModelProviders.of(this).get(EditorViewModel::class.java)

        mEditText = findViewById(R.id.insulin_edit_name)
        mSpinner = findViewById(R.id.insulin_edit_time)
        mBasalSwitch = findViewById(R.id.insulin_edit_basal)
        mHalfUnitsSwitch = findViewById(R.id.insulin_edit_half_units)
        mDeleteButton = findViewById(R.id.insulin_edit_btn_neutral)
        val cancelButton = findViewById<AppCompatButton>(R.id.insulin_edit_btn_negative)
        val saveButton = findViewById<AppCompatButton>(R.id.insulin_edit_btn_positive)

        mTimeFrames = arrayOf(getString(TimeFrame.EXTRA.string),
                getString(TimeFrame.MORNING.string), getString(TimeFrame.LATE_MORNING.string),
                getString(TimeFrame.LUNCH.string), getString(TimeFrame.AFTERNOON.string),
                getString(TimeFrame.DINNER.string), getString(TimeFrame.NIGHT.string))

        cancelButton.setOnClickListener { _ -> finish() }
        saveButton.setOnClickListener { _ ->
            mViewModel.insulin.name = mEditText.text.toString()
            mViewModel.insulin.setTimeFrame(mSpinner.selectedItemPosition - 1)
            mViewModel.insulin.isBasal = mBasalSwitch.isChecked
            mViewModel.insulin.hasHalfUnits = mHalfUnitsSwitch.isChecked
            mViewModel.save()
            finish()
        }

        setupUI()
    }

    private fun setupUI() {
        val uid = intent.getLongExtra(EXTRA_UID, -1)
        mEditMode = uid >= 0

        mSpinner.adapter = ArrayAdapter(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                mTimeFrames)
        mSpinner.setSelection(if (mEditMode) mViewModel.insulin.timeFrame.toInt() + 1 else 0)

        if (!mEditMode) {
            title = getString(R.string.insulin_editor_add)
            return
        }

        mViewModel.setInsulin(uid)

        title = getString(R.string.insulin_editor_edit)
        mEditText.setText(mViewModel.insulin.name)
        mBasalSwitch.isChecked = mViewModel.insulin.isBasal
        mHalfUnitsSwitch.isChecked = mViewModel.insulin.hasHalfUnits
        mDeleteButton.setOnClickListener {
            mViewModel.delete(mViewModel.insulin)
            finish()
        }
        mDeleteButton.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_UID = "InsulinEditorExtraUid"
    }
} 
