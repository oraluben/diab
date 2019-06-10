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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import it.diab.core.override.BaseFitHandler
import it.diab.core.util.Activities
import it.diab.core.util.ComposableError
import it.diab.core.util.PreferencesUtil
import it.diab.core.util.SystemUtil
import it.diab.core.util.extensions.setPrecomputedText
import it.diab.data.entities.Glucose
import it.diab.data.entities.Insulin
import it.diab.data.plugin.PluginManager
import it.diab.data.repositories.GlucoseRepository
import it.diab.data.repositories.InsulinRepository
import it.diab.glucose.R
import it.diab.glucose.suggestion.CheckAgainSuggestion
import it.diab.glucose.suggestion.InsulinSuggestion
import it.diab.glucose.ui.DateTimeDialog
import it.diab.glucose.util.VibrationUtil
import it.diab.glucose.util.extensions.forEachUntilTrue
import it.diab.glucose.util.extensions.getDetailedString
import it.diab.glucose.util.extensions.setErrorStatus
import it.diab.glucose.util.extensions.setTextErrorStatus
import it.diab.glucose.viewmodels.EditorViewModel
import it.diab.glucose.viewmodels.EditorViewModelFactory
import it.diab.glucose.widget.EatBar
import it.diab.glucose.widget.NumericKeyboardView
import it.diab.glucose.widget.SuggestionView
import java.util.Date

class EditorFragment : Fragment() {

    private lateinit var constraintView: ConstraintLayout
    private lateinit var closeView: ImageView
    private lateinit var valueView: TextView
    private lateinit var timeView: TextView
    private lateinit var eatView: EatBar
    private lateinit var insulinView: TextView
    private lateinit var basalView: TextView
    private lateinit var suggestionView: SuggestionView
    private lateinit var fabView: FloatingActionButton
    private lateinit var keyboardView: NumericKeyboardView

    private lateinit var viewModel: EditorViewModel

    private val error = ComposableError()

    private val supportedSuggestions = arrayOf(
        this::setupInsulinSuggestion,
        this::setupCheckAgainSuggestion
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return
        val factory = EditorViewModelFactory(
            GlucoseRepository.getInstance(context),
            InsulinRepository.getInstance(context)
        )
        viewModel = ViewModelProviders.of(this, factory)[EditorViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editor, container, false)

        constraintView = view.findViewById(R.id.editor_root)
        closeView = view.findViewById(R.id.editor_close)
        valueView = view.findViewById(R.id.editor_value)
        timeView = view.findViewById(R.id.editor_time)
        eatView = view.findViewById(R.id.editor_eat_level)
        insulinView = view.findViewById(R.id.editor_insulin)
        basalView = view.findViewById(R.id.editor_basal)
        suggestionView = view.findViewById(R.id.editor_suggestion)
        fabView = view.findViewById(R.id.editor_fab)
        keyboardView = view.findViewById(R.id.editor_keyboard)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = arguments?.getLong(Activities.Glucose.Editor.EXTRA_UID, -1L) ?: -1L

        val context = context ?: return
        viewModel.prepare(uid, PluginManager(context)) {
            // Bind glucose observer now that everything is ready
            viewModel.glucose.observe(this, Observer(this::setup))
        }
    }

    private fun setup(glucose: Glucose?) {
        if (glucose == null) {
            return
        }

        closeView.setOnClickListener { activity?.onBackPressed() }

        eatView.setProgress(glucose.eatLevel)
        eatView.lock()
        valueView.text = glucose.value.toString()
        timeView.setPrecomputedText(glucose.date.getDetailedString())
        fabView.setOnClickListener { onFabClick() }

        if (viewModel.isEditMode) {
            setupEdit(false)
        } else {
            setupView(glucose)
        }
    }

    private fun setupEdit(animate: Boolean) {
        switchToEditConstraint(animate)

        timeView.setOnClickListener { onDateClick() }
        eatView.unlock()
        keyboardView.bindTextView(valueView, this::checkForValueError)
        fabView.setImageResource(R.drawable.ic_done)

        if (valueView.text == "0") {
            // Make sure the user sets a value
            error += ERROR_VALUE
        }
    }

    private fun setupView(glucose: Glucose) {
        switchToViewConstraint()

        setupInsulinView(glucose)
        setupBasalView(glucose, viewModel.hasPotentialBasal())

        fabView.setImageResource(R.drawable.ic_edit)

        // Try to run all the suggestion setups until one completes successfully
        supportedSuggestions.forEachUntilTrue { it(glucose) }
    }

    private fun setupInsulinSuggestion(glucose: Glucose): Boolean {
        val insulinSuggestion = InsulinSuggestion(
            glucose,
            viewModel.getInsulinByTimeFrame(),
            this::onSuggestionApplied
        )

        if (!suggestionView.applyConfig(insulinSuggestion)) {
            return false
        }

        viewModel.getInsulinSuggestion { suggestionView.onSuggestionLoaded(it, insulinSuggestion) }
        return true
    }

    private fun setupCheckAgainSuggestion(glucose: Glucose): Boolean {
        val context = context ?: return false

        val checkAgainSuggestion = CheckAgainSuggestion(
            glucose.timeFrame,
            PreferencesUtil.getGlucoseLowThreshold(context)
        )

        if (!suggestionView.applyConfig(checkAgainSuggestion)) {
            return false
        }

        suggestionView.onSuggestionLoaded(glucose.value, checkAgainSuggestion)
        return true
    }

    private fun onSuggestionApplied(value: Float, insulin: Insulin) {
        viewModel.applyInsulinSuggestion(value, insulin) {
            Snackbar.make(constraintView, R.string.insulin_suggestion_applied,
                Snackbar.LENGTH_LONG).show()
        }
    }

    private fun switchToEditConstraint(animate: Boolean) {
        val context = context ?: return
        ConstraintSet().apply {
            clone(context, R.layout.constraint_editor_edit)

            if (animate) {
                TransitionManager.beginDelayedTransition(constraintView)
            }
            applyTo(constraintView)
        }
    }

    private fun switchToViewConstraint() {
        val context = context ?: return
        ConstraintSet().apply {
            clone(context, R.layout.constraint_editor_view)
            applyTo(constraintView)
        }
    }

    private fun setupInsulinView(glucose: Glucose) {
        val uid = glucose.insulinId0
        val value = glucose.insulinValue0

        insulinView.apply {
            text = if (uid > 0L)
                viewModel.getInsulin(uid).getDisplayedString(value)
            else
                getString(R.string.glucose_editor_insulin_add)

            setOnClickListener { onInsulinClick(glucose.uid, false) }
        }
    }

    private fun setupBasalView(glucose: Glucose, hasBasal: Boolean) {
        val uid = glucose.insulinId1
        val value = glucose.insulinValue1

        basalView.apply {
            text = if (uid > 0L)
                viewModel.getInsulin(uid).getDisplayedString(value)
            else
                getString(R.string.glucose_editor_basal_add)

            visibility = if (hasBasal)
                View.VISIBLE
            else
                View.GONE

            setOnClickListener { onInsulinClick(glucose.uid, true) }
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
        DateTimeDialog(requireActivity(), this::onDateSelected).show(viewModel.date)
    }

    private fun onInsulinClick(uid: Long, wantsBasal: Boolean) {
        val fragment = InsulinDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(Activities.Glucose.Editor.EXTRA_UID, uid)
                putBoolean(Activities.Glucose.Editor.EXTRA_INSULIN_BASAL, wantsBasal)
            }
        }

        childFragmentManager.beginTransaction()
            .add(fragment, "insulin_dialog")
            .commit()
    }

    private fun onDateSelected(timeInMillis: Long) {
        val context = context ?: return

        if (timeInMillis > System.currentTimeMillis()) {
            error += ERROR_DATE
            timeView.setErrorStatus(context, true)
        } else {
            error -= ERROR_DATE
            timeView.setErrorStatus(context, false)
        }

        val date = Date(timeInMillis)
        timeView.text = date.getDetailedString()
        viewModel.date = date
    }

    private fun checkForValueError(value: Int) {
        val context = context ?: return

        val hadError = ERROR_VALUE in error
        val hasError = value == 0

        if (hadError != hasError) {
            valueView.setTextErrorStatus(context, hasError)
        }

        if (hasError) {
            error += ERROR_VALUE
        } else {
            error -= ERROR_VALUE
        }
    }

    private fun save() {
        if (error.hasAny()) {
            val context = context ?: return
            VibrationUtil.vibrateForError(context)
            Snackbar.make(constraintView, R.string.glucose_editor_save_error, Snackbar.LENGTH_LONG)
                .show()
            return
        }

        viewModel.apply {
            value = keyboardView.input
            eatLevel = eatView.getProgress()
            save()
        }
        Handler().postDelayed(this::addToFit, 500)
        Snackbar.make(constraintView, R.string.glucose_editor_saved, 800).show()
    }

    private fun addToFit() {
        val activity = activity ?: return
        val handler = SystemUtil.getOverrideObject(
            BaseFitHandler::class.java,
            activity,
            R.string.config_class_fit_handler
        )

        if (!handler.hasFit(activity)) {
            activity.finish()
            return
        }

        handler.upload(
            activity,
            viewModel.glucose,
            !viewModel.isEditMode,
            activity::finish
        ) { e -> Log.e(TAG, e.message) }
    }

    companion object {
        private const val TAG = "EditorFragment"

        private const val ERROR_VALUE = 1
        private const val ERROR_DATE = 1 shl 1
    }
}