/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import it.diab.R

object UIUtils {

    fun createRoundDrawable(resources: Resources, size: Int, @ColorInt color: Int): Drawable {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val rect = RectF(Rect(0, 0, size, size))
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color
        }

        Canvas(bitmap).drawRoundRect(rect, size / 2f, size / 2f, paint)

        return RoundedBitmapDrawableFactory.create(resources, bitmap)
    }

    fun setStyleMode(value: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (value) {
                "0" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                "2" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    @SuppressLint("PrivateResource")
    fun setWhiteNavBarIfNeeded(context: Context, dialog: Dialog) {
        val isLight = context.resources.getBoolean(R.bool.is_light)
        if (!isLight) {
            return
        }

        val window = dialog.window ?: return
        val metrics = DisplayMetrics().apply {
            window.windowManager.defaultDisplay.getMetrics(this)
        }
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(context, R.color.dim_foreground_material_light))
            alpha = 50
        }
        val navBarDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(context, R.color.navigationBar_color))
        }

        val background = LayerDrawable(arrayOf(bgDrawable, navBarDrawable)).apply {
            setLayerInsetTop(1, metrics.heightPixels)
        }

        window.setBackgroundDrawable(background)
    }

    fun supportsAutoStyleMode() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> true
        Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1 ->
            SystemUtil.getProp("ro.lineage.build.version.plat.sdk", "") == "9"
        else -> false
    }
}