package it.diab.insulin.editor

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatSpinner
import android.view.Window
import android.widget.ArrayAdapter

import it.diab.R
import it.diab.util.timeFrame.TimeFrame

class EditorActivity : AppCompatActivity() {

    private lateinit var mEditText: AppCompatEditText
    private lateinit var mSpinner: AppCompatSpinner

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
            mViewModel.save()
            finish()
        }

        setupUI()
    }

    private fun setupUI() {
        val uid = intent.getLongExtra(EXTRA_UID, -1)

        if (uid >= 0) {
            mViewModel.setInsulin(uid)
            mEditMode = true
        }

        mSpinner.adapter = ArrayAdapter(this,
                android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item,
                mTimeFrames)
        mSpinner.setSelection(if (mEditMode) mViewModel.insulin.timeFrame.toInt() + 1 else 0)

        if (mEditMode) {
            mEditText.setText(mViewModel.insulin.name)
        }
    }

    companion object {
        const val EXTRA_UID = "InsulinEditorExtraUid"
    }
} 
