/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.insulin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import it.diab.core.util.Activities
import it.diab.data.entities.Insulin
import it.diab.data.entities.TimeFrame
import it.diab.data.repositories.InsulinRepository
import it.diab.insulin.R
import it.diab.insulin.viewmodels.EditorViewModel
import it.diab.insulin.viewmodels.EditorViewModelFactory
import it.diab.ui.util.UIUtils
import it.diab.ui.widgets.BottomSheetDialogFragmentExt

class EditorFragment : BottomSheetDialogFragmentExt() {

    private lateinit var titleView: TextView
    private lateinit var editText: AppCompatEditText
    private lateinit var spinner: AppCompatSpinner
    private lateinit var basalSwitch: SwitchCompat
    private lateinit var halfUnitsSwitch: SwitchCompat
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton

    private lateinit var viewModel: EditorViewModel

    private var editMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return
        val factory = EditorViewModelFactory(InsulinRepository.getInstance(context))
        viewModel = ViewModelProviders.of(this, factory)[EditorViewModel::class.java]
    }

    override fun onCreateDialogView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_insulin_edit, container, false)

        titleView = view.findViewById(R.id.insulin_edit_dialog_title)
        editText = view.findViewById(R.id.insulin_edit_name)
        spinner = view.findViewById(R.id.insulin_edit_time)
        basalSwitch = view.findViewById(R.id.insulin_edit_basal)
        halfUnitsSwitch = view.findViewById(R.id.insulin_edit_half_units)
        saveButton = view.findViewById(R.id.insulin_edit_save)
        deleteButton = view.findViewById(R.id.insulin_edit_delete)

        UIUtils.setWhiteNavBarIfNeeded(requireContext(), dialog)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getLong(Activities.Insulin.EXTRA_EDITOR_UID, -1L) ?: -1L
        editMode = uid >= 0
        viewModel.setInsulin(uid, this::setup)
    }

    private fun setup(insulin: Insulin) {

        titleView.text = getString(if (editMode) R.string.insulin_editor_edit else R.string.insulin_editor_add)

        editText.setText(insulin.name)
        basalSwitch.isChecked = insulin.isBasal
        halfUnitsSwitch.isChecked = insulin.hasHalfUnits

        setupSpinner(insulin)

        saveButton.setOnClickListener { saveInsulin() }
        deleteButton.setOnClickListener { deleteInsulin() }

        if (!editMode) {
            deleteButton.visibility = View.GONE
        }
    }

    private fun setupSpinner(insulin: Insulin) {
        val context = context ?: return

        val timeFrames = TimeFrame.values()
        spinner.apply {
            adapter = ArrayAdapter(
                context,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                timeFrames.map { timeFrame -> getString(timeFrame.string) }
            )

            setSelection(insulin.timeFrame.toInt() + 1)
        }
    }

    private fun saveInsulin() {
        viewModel.insulin.apply {
            name = editText.text.toString()
            isBasal = basalSwitch.isChecked
            hasHalfUnits = halfUnitsSwitch.isChecked
            setTimeFrame(spinner.selectedItemPosition - 1)
        }

        viewModel.save()
        dismiss()
    }

    private fun deleteInsulin() {
        viewModel.delete()
        dismiss()
    }
}