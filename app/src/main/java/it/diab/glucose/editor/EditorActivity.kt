/*
 * Copyright (c) 2019 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.glucose.editor

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import it.diab.R
import it.diab.db.entities.Insulin
import it.diab.db.repositories.GlucoseRepository
import it.diab.db.repositories.InsulinRepository
import it.diab.fit.BaseFitHandler
import it.diab.insulin.ml.PluginManager
import it.diab.ui.EatBar
import it.diab.ui.InsulinSuggestionView
import it.diab.ui.NumericKeyboardView
import it.diab.util.SystemUtil
import it.diab.util.VibrationUtil
import it.diab.util.extensions.asTimeFrame
import it.diab.util.extensions.getDetailedString
import it.diab.util.extensions.insulin
import it.diab.util.extensions.setErrorStatus
import it.diab.util.extensions.setPrecomputedText
import it.diab.util.extensions.setTextErrorStatus
import it.diab.viewmodels.glucose.EditorViewModel
import it.diab.viewmodels.glucose.EditorViewModelFactory

class EditorActivity : AppCompatActivity() {

    private lateinit var constraintView: ConstraintLayout
    private lateinit var valueView: TextView
    private lateinit var timeView: TextView
    private lateinit var eatView: EatBar
    private lateinit var insulinView: TextView
    private lateinit var basalView: TextView
    private lateinit var suggestionView: InsulinSuggestionView
    private lateinit var fabView: FloatingActionButton
    private lateinit var keyboardView: NumericKeyboardView

    private lateinit var viewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_glucose_editor)

        val factory = EditorViewModelFactory(
            GlucoseRepository.getInstance(this),
            InsulinRepository.getInstance(this)
        )
        viewModel = ViewModelProviders.of(this, factory)[EditorViewModel::class.java]

        constraintView = findViewById(R.id.editor_root)
        valueView = findViewById(R.id.editor_value)
        timeView = findViewById(R.id.editor_time)
        eatView = findViewById(R.id.editor_eat_bar)
        insulinView = findViewById(R.id.editor_insulin)
        basalView = findViewById(R.id.editor_basal)
        suggestionView = findViewById(R.id.editor_suggestion)
        fabView = findViewById(R.id.editor_fab)
        keyboardView = findViewById(R.id.editor_keyboard)

        viewModel.prepare(PluginManager(this), this::setup)
    }

    private fun setup() {
        val extraUid = intent.getLongExtra(EXTRA_UID, -1)
        viewModel.isEditMode = extraUid < 0
        viewModel.setGlucose(extraUid) {
            setupCommon()

            if (viewModel.isEditMode) {
                setupEdit(false)
            } else {
                setupView(false)
            }
        }
    }

    private fun setupCommon() {
        valueView.text = viewModel.glucose.value.toString()
        timeView.setPrecomputedText(viewModel.glucose.date.getDetailedString(), viewModel.viewModelScope)
        fabView.setOnClickListener { onFabClick() }
        eatView.progress = viewModel.glucose.eatLevel
    }

    private fun setupEdit(animate: Boolean) {
        ConstraintSet().run {
            clone(this@EditorActivity, R.layout.constraint_editor_edit)
            if (animate) {
                TransitionManager.beginDelayedTransition(constraintView)
            }
            applyTo(constraintView)
        }

        timeView.setOnClickListener { onDateClick() }
        eatView.unlock()
        keyboardView.bindTextView(valueView, this::checkForError)
        fabView.setImageResource(R.drawable.ic_done)

        if (valueView.text == "0") {
            // Make sure the user sets a value
            viewModel.setError(EditorViewModel.ERROR_VALUE)
        }
    }

    private fun setupView(animate: Boolean) {
        ConstraintSet().run {
            clone(this@EditorActivity, R.layout.constraint_editor_view)
            if (animate) {
                TransitionManager.beginDelayedTransition(constraintView)
            }
            applyTo(constraintView)
        }

        eatView.lock()
        setupInsulinView()
        insulinView.setOnClickListener { onInsulinClick(false) }
        setupBasalView()
        if (viewModel.hasPotentialBasal()) {
            basalView.apply {
                visibility = View.VISIBLE
                setOnClickListener { onInsulinClick(true) }
            }
        }
        suggestionView.bind(
            viewModel.glucose,
            viewModel.getInsulinByTimeFrame(),
            this::onSuggestionApplied
        )
        viewModel.getInsulinSuggestion(suggestionView::onSuggestionLoaded)
        fabView.setImageResource(R.drawable.ic_edit)
    }

    private fun setupInsulinView() {
        val uid = viewModel.glucose.insulinId0
        val value = viewModel.glucose.insulinValue0

        insulinView.text = if (uid > -1) {
            viewModel.getInsulin(uid).getDisplayedString(value)
        } else {
            getString(R.string.glucose_editor_insulin_add)
        }
    }

    private fun setupBasalView() {
        val uid = viewModel.glucose.insulinId1
        val value = viewModel.glucose.insulinValue1

        basalView.text = if (uid > -1) {
            viewModel.getInsulin(uid).getDisplayedString(value)
        } else {
            getString(R.string.glucose_editor_basal_add)
        }
    }

    private fun onFabClick() {
        if (viewModel.isEditMode) {
            save()
        } else {
            viewModel.isEditMode = true
            setupEdit(true)
        }
    }

    private fun onDateClick() {
        DateTimeDialog(this, this::onDateSelected).show(viewModel.glucose.date)
    }

    private fun onDateSelected(timeInMillis: Long) {
        if (timeInMillis > System.currentTimeMillis()) {
            if (!viewModel.hasError(EditorViewModel.ERROR_DATE)) {
                timeView.setErrorStatus(this, true)
            }

            viewModel.setError(EditorViewModel.ERROR_DATE)
        } else {
            viewModel.clearError(EditorViewModel.ERROR_DATE)
        }

        viewModel.glucose.date.time = timeInMillis
        timeView.text = viewModel.glucose.date.getDetailedString()
    }

    private fun onInsulinClick(isBasal: Boolean) {
        AddInsulinDialog(this, viewModel.glucose, isBasal).apply {
            setInsulins(viewModel.insulins.filter { it.isBasal == isBasal })
        }.show(
            { insulin, value -> onInsulinApply(insulin, value, isBasal) },
            { onInsulinApply(insulin { uid = -1 }, 0f, isBasal) }
        )
    }

    private fun checkForError(value: Int) {
        val hadError = viewModel.hasError(EditorViewModel.ERROR_VALUE)
        val hasError = value == 0

        if (hadError != hasError) {
            valueView.setTextErrorStatus(this, hasError)
        }

        if (hasError) {
            viewModel.setError(EditorViewModel.ERROR_VALUE)
        } else {
            viewModel.clearError(EditorViewModel.ERROR_VALUE)
        }
    }

    private fun onInsulinApply(insulin: Insulin, value: Float, isBasal: Boolean) {
        viewModel.glucose.apply {
            if (isBasal) {
                insulinId1 = insulin.uid
                insulinValue1 = value
                setupBasalView()
            } else {
                insulinId0 = insulin.uid
                insulinValue0 = value
                setupInsulinView()
            }
        }

        viewModel.save()
    }

    private fun onSuggestionApplied(value: Float, insulin: Insulin) {
        viewModel.applyInsulinSuggestion(value, insulin) {
            onInsulinApply(insulin, value, false)
            Snackbar.make(constraintView, R.string.insulin_suggestion_applied, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun save() {
        if (viewModel.hasErrors()) {
            VibrationUtil.vibrateForError(this)
            Snackbar.make(constraintView, R.string.glucose_editor_save_error, Snackbar.LENGTH_LONG).show()
            return
        }

        viewModel.glucose.apply {
            value = keyboardView.input
            eatLevel = eatView.progress
            timeFrame = date.asTimeFrame()
        }

        viewModel.save()
        Snackbar.make(constraintView, R.string.saved, 800).show()
        Handler().postDelayed(this::addToFit, 1000)
    }

    private fun addToFit() {
        val handler = SystemUtil.getOverrideObject(
            BaseFitHandler::class.java,
            this,
            R.string.config_class_fit_handler
        )

        if (!handler.hasFit(this)) {
            finish()
            return
        }

        handler.upload(
            this,
            viewModel.glucose,
            !viewModel.isEditMode,
            { finish() },
            { e -> Log.e(TAG, e.message) }
        )
    }

    companion object {
        const val EXTRA_UID = "glucose_uid"

        private const val TAG = "EditorActivity"
    }
}
