/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import it.diab.core.data.repositories.InsulinRepository
import it.diab.core.data.timeframe.TimeFrame
import it.diab.core.util.Activities
import it.diab.core.util.UIUtils
import it.diab.insulin.R

class EditorActivity : AppCompatActivity() {

    private lateinit var dialog: BottomSheetDialog
    private lateinit var titleView: TextView
    private lateinit var editText: AppCompatEditText
    private lateinit var spinner: AppCompatSpinner
    private lateinit var basalSwitch: SwitchCompat
    private lateinit var halfUnitsSwitch: SwitchCompat
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton

    private lateinit var viewModel: it.diab.insulin.viewmodels.EditorViewModel
    private lateinit var timeFrames: Array<String>
    private var editMode = false

    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setFinishOnTouchOutside(true)

        val factory = it.diab.insulin.viewmodels.EditorViewModelFactory(InsulinRepository.getInstance(this))
        viewModel = ViewModelProviders.of(this, factory)[it.diab.insulin.viewmodels.EditorViewModel::class.java]

        val layoutInflater = getSystemService(LayoutInflater::class.java)
        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.dialog_insulin_edit, null)

        titleView = view.findViewById(R.id.insulin_edit_dialog_title)
        editText = view.findViewById(R.id.insulin_edit_name)
        spinner = view.findViewById(R.id.insulin_edit_time)
        basalSwitch = view.findViewById(R.id.insulin_edit_basal)
        halfUnitsSwitch = view.findViewById(R.id.insulin_edit_half_units)
        saveButton = view.findViewById(R.id.insulin_edit_save)
        deleteButton = view.findViewById(R.id.insulin_edit_delete)

        timeFrames = Array(TimeFrame.values().size) { getString(TimeFrame.values()[it].string) }

        dialog = BottomSheetDialog(this).apply {
            setContentView(view)
            setOnDismissListener { finish() }
        }

        setupUI()

        UIUtils.setWhiteNavBarIfNeeded(this, dialog)
        dialog.show()
    }

    private fun setupUI() {
        val uid = intent.getLongExtra(Activities.Insulin.Editor.EXTRA_UID, -1)
        editMode = uid >= 0

        spinner.adapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            timeFrames
        )

        saveButton.setOnClickListener { onSaveInsulin() }

        if (!editMode) {
            titleView.text = getString(R.string.insulin_editor_add)
            deleteButton.visibility = View.GONE
            return
        }

        titleView.text = getString(R.string.insulin_editor_edit)
        deleteButton.setOnClickListener { onDeleteInsulin() }

        viewModel.setInsulin(uid) {
            editText.setText(it.name)
            spinner.setSelection(if (editMode) it.timeFrame.toInt() + 1 else 0)
            basalSwitch.isChecked = it.isBasal
            halfUnitsSwitch.isChecked = it.hasHalfUnits
        }
    }

    private fun onSaveInsulin() {
        viewModel.insulin.run {
            name = editText.text.toString()
            isBasal = basalSwitch.isChecked
            hasHalfUnits = halfUnitsSwitch.isChecked
            setTimeFrame(spinner.selectedItemPosition - 1)
        }

        viewModel.save()
        dialog.dismiss()
    }

    private fun onDeleteInsulin() {
        viewModel.delete()
        dialog.dismiss()
    }
}
