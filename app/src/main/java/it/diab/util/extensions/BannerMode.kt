package it.diab.util.extensions

import it.diab.util.BannerModel

fun bannerModel(block: BannerModel.() -> Unit) = BannerModel().apply(block)
