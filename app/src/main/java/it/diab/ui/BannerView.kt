package it.diab.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.button.MaterialButton
import it.diab.R
import it.diab.util.BannerModel

class BannerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private lateinit var mIcon: ImageView
    private lateinit var mText: TextView
    private lateinit var mPositiveButton: MaterialButton
    private lateinit var mNegativeButton: MaterialButton

    private var mOnActionExecuted: (BannerView) -> Unit = {}

    init {
        View.inflate(context, R.layout.item_banner, this)
        setup()
    }

    fun setModel(model: BannerModel) {
        if (model.title != 0) {
            setText(model.title)
        }

        if (model.icon != 0) {
            setIcon(model.icon)
        }

        mOnActionExecuted = model.onAction

        if (model.positiveText != 0) {
            setPositiveButton(model.positiveText, model.onPositive)
        }

        if (model.negativeText != 0) {
            setNegativeButton(model.negativeText, model.onNegative)
        }
    }

    private fun setText(@StringRes bannerText: Int) {
        mText.text = resources.getString(bannerText)
    }

    private fun dismiss() {
        animate()
            .alpha(0f)
            .withEndAction {
                visibility = View.GONE
                alpha = 1f
            }
    }

    private fun setup() {
        mIcon = findViewById(R.id.banner_icon)
        mText = findViewById(R.id.banner_text)
        mPositiveButton = findViewById(R.id.banner_positive_button)
        mNegativeButton = findViewById(R.id.banner_negative_button)
    }

    private fun setIcon(@DrawableRes bannerImage: Int) {
        mIcon.apply {
            setImageResource(bannerImage)
            visibility = View.VISIBLE
        }
    }

    private fun setPositiveButton(@StringRes positiveText: Int, onClicked: (BannerView) -> Unit) {
        mPositiveButton.apply {
            text = resources.getString(positiveText)
            visibility = View.VISIBLE

            setOnClickListener {
                onClicked(this@BannerView)
                mOnActionExecuted(this@BannerView)
                dismiss()
            }
        }
    }

    private fun setNegativeButton(@StringRes negativeText: Int, onClicked: (BannerView) -> Unit) {
        mNegativeButton.apply {
            text = resources.getString(negativeText)
            visibility = View.VISIBLE

            setOnClickListener {
                onClicked(this@BannerView)
                mOnActionExecuted(this@BannerView)
                dismiss()
            }
        }
    }
}