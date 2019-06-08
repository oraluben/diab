/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.button.MaterialButton
import it.diab.core.util.Activities
import it.diab.core.util.intentTo
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.glucose.R
import it.diab.glucose.ui.models.InsulinDialogUiModel
import it.diab.glucose.util.InsulinSelector
import it.diab.glucose.viewmodels.InsulinDialogViewModel
import it.diab.glucose.viewmodels.InsulinDialogViewModelFactory
import it.diab.ui.widgets.BottomSheetDialogFragmentExt

class InsulinDialogFragment : BottomSheetDialogFragmentExt() {

    private lateinit var nameSpinner: AppCompatSpinner
    private lateinit var valueEditText: EditText
    private lateinit var addButton: MaterialButton
    private lateinit var removeButton: MaterialButton

    private lateinit var editorIcon: ImageView

    private lateinit var viewModel: InsulinDialogViewModel

    private var wantsBasal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return
        val factory = InsulinDialogViewModelFactory(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        )

        viewModel = ViewModelProviders.of(this, factory)[InsulinDialogViewModel::class.java]
    }

    override fun onCreateDialogView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_insulin, container, false)

        nameSpinner = view.findViewById(R.id.glucose_editor_insulin_spinner)
        valueEditText = view.findViewById(R.id.glucose_editor_insulin_value)
        addButton = view.findViewById(R.id.glucose_editor_insulin_add)
        removeButton = view.findViewById(R.id.glucose_editor_insulin_remove)
        editorIcon = view.findViewById(R.id.glucose_editor_insulin_editor)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getLong(Activities.Glucose.Editor.EXTRA_UID, -1L) ?: -1L
        wantsBasal = arguments?.getBoolean(Activities.Glucose.Editor.EXTRA_INSULIN_BASAL) ?: false

        viewModel.prepare(uid, wantsBasal, this::setup)
    }

    private fun setup(model: InsulinDialogUiModel) {
        val context = context ?: return

        if (model.insulinValue > 0f) {
            val formattedValue = "%.1f".format(model.insulinValue)
            valueEditText.setText(formattedValue)
        }

        nameSpinner.adapter = ArrayAdapter(
            context,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            model.insulins.map { "${it.name} (${getString(it.timeFrame.string)})" }
        )

        val spinnerIndex = InsulinSelector(model.targetTimeFrame).run {
            if (wantsBasal) suggestBasal(model.insulins.toTypedArray(), model.currentInsulinId)
            else suggestInsulin(model.insulins.toTypedArray(), model.currentInsulinId)
        }
        nameSpinner.setSelection(spinnerIndex)

        editorIcon.setOnClickListener { startActivity(intentTo(Activities.Insulin)) }
        addButton.setOnClickListener { onSave() }
        removeButton.setOnClickListener { onRemove() }

        if (model.currentInsulinId < 1L) {
            removeButton.visibility = View.GONE
        }
    }

    private fun onSave() {
        val currentValue = valueEditText.text.toString().toFloatOrNull() ?: 0f
        viewModel.apply {
            if (wantsBasal) {
                setBasal(nameSpinner.selectedItemPosition, currentValue)
            } else {
                setInsulin(nameSpinner.selectedItemPosition, currentValue)
            }
        }

        Handler().postDelayed(this::dismiss, 350)
    }

    private fun onRemove() {
        viewModel.run {
            if (wantsBasal) {
                removeBasal()
            } else {
                removeInsulin()
            }
        }

        Handler().postDelayed(this::dismiss, 350)
    }
}