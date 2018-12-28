/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.button.MaterialButton
import it.diab.R
import it.diab.util.BannerModel

class BannerView : FrameLayout {
    private lateinit var bannerIcon: ImageView
    private lateinit var bannerText: TextView
    private lateinit var bannerPositive: MaterialButton
    private lateinit var bannerNegative: MaterialButton

    private var onActionExecuted: (BannerView) -> Unit = {}

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

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

        onActionExecuted = model.onAction

        if (model.positiveText != 0) {
            setPositiveButton(model.positiveText, model.onPositive)
        }

        if (model.negativeText != 0) {
            setNegativeButton(model.negativeText, model.onNegative)
        }
    }

    private fun setText(@StringRes bannerText: Int) {
        this.bannerText.text = resources.getString(bannerText)
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
        bannerIcon = findViewById(R.id.banner_icon)
        bannerText = findViewById(R.id.banner_text)
        bannerPositive = findViewById(R.id.banner_positive_button)
        bannerNegative = findViewById(R.id.banner_negative_button)
    }

    private fun setIcon(@DrawableRes bannerImage: Int) {
        bannerIcon.apply {
            setImageResource(bannerImage)
            visibility = View.VISIBLE
        }
    }

    private fun setPositiveButton(@StringRes positiveText: Int, onClicked: (BannerView) -> Unit) {
        bannerPositive.apply {
            text = resources.getString(positiveText)
            visibility = View.VISIBLE

            setOnClickListener {
                onClicked(this@BannerView)
                onActionExecuted(this@BannerView)
                dismiss()
            }
        }
    }

    private fun setNegativeButton(@StringRes negativeText: Int, onClicked: (BannerView) -> Unit) {
        bannerNegative.apply {
            text = resources.getString(negativeText)
            visibility = View.VISIBLE

            setOnClickListener {
                onClicked(this@BannerView)
                onActionExecuted(this@BannerView)
                dismiss()
            }
        }
    }
}