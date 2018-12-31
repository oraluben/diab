/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.editor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProviders
import it.diab.R
import it.diab.db.repositories.InsulinRepository
import it.diab.util.timeFrame.TimeFrame
import it.diab.viewmodels.insulin.EditorViewModel
import it.diab.viewmodels.insulin.EditorViewModelFactory

class EditorActivity : AppCompatActivity() {

    private lateinit var mEditText: AppCompatEditText
    private lateinit var mSpinner: AppCompatSpinner
    private lateinit var mBasalSwitch: SwitchCompat
    private lateinit var mHalfUnitsSwitch: SwitchCompat

    private lateinit var viewModel: EditorViewModel
    private lateinit var mTimeFrames: Array<String>
    private var mEditMode = false

    @SuppressLint("InflateParams")
    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setFinishOnTouchOutside(true)

        val factory = EditorViewModelFactory(InsulinRepository.getInstance(this))
        viewModel = ViewModelProviders.of(this, factory)[EditorViewModel::class.java]

        val layoutInflater = getSystemService(LayoutInflater::class.java)
        val view = layoutInflater.inflate(R.layout.dialog_insulin_edit, null)

        mEditText = view.findViewById(R.id.insulin_edit_name)
        mSpinner = view.findViewById(R.id.insulin_edit_time)
        mBasalSwitch = view.findViewById(R.id.insulin_edit_basal)
        mHalfUnitsSwitch = view.findViewById(R.id.insulin_edit_half_units)

        mTimeFrames = Array(TimeFrame.values().size) { getString(TimeFrame.values()[it].string) }

        setupUI()

        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.insulin_editor_edit)
            .setView(view)
            .setOnDismissListener { finish() }
            .setPositiveButton(R.string.save) { _, _ -> onSaveInsulin() }
            .setNegativeButton(R.string.cancel) { _, _ -> finish() }

        if (mEditMode) {
            builder.setNeutralButton(R.string.remove) { _, _ -> onDeleteInsulin() }
        }

        builder.show()
    }

    private fun setupUI() {
        val uid = intent.getLongExtra(EXTRA_UID, -1)
        mEditMode = uid >= 0

        mSpinner.adapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            mTimeFrames
        )

        if (!mEditMode) {
            title = getString(R.string.insulin_editor_add)
            return
        }

        viewModel.setInsulin(uid) {
            mEditText.setText(it.name)
            mSpinner.setSelection(if (mEditMode) it.timeFrame.toInt() + 1 else 0)
            mBasalSwitch.isChecked = it.isBasal
            mHalfUnitsSwitch.isChecked = it.hasHalfUnits
        }
    }

    private fun onSaveInsulin() {
        viewModel.insulin.run {
            name = mEditText.text.toString()
            isBasal = mBasalSwitch.isChecked
            hasHalfUnits = mHalfUnitsSwitch.isChecked
            setTimeFrame(mSpinner.selectedItemPosition - 1)
        }

        viewModel.save()
        finish()
    }

    private fun onDeleteInsulin() {
        viewModel.delete()
        finish()
    }

    companion object {
        const val EXTRA_UID = "InsulinEditorExtraUid"
    }
}
