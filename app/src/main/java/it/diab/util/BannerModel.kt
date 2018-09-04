package it.diab.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import it.diab.ui.BannerView

data class BannerModel(
    @StringRes var title: Int = 0,
    @DrawableRes var icon: Int = 0,
    @StringRes var positiveText: Int = 0,
    @StringRes var negativeText: Int = 0,
    var onPositive: (BannerView) -> Unit = {},
    var onNegative: (BannerView) -> Unit = {},
    var onAction: (BannerView) -> Unit = {}
)
