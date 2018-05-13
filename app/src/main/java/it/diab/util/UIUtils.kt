package it.diab.util

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory

object UIUtils {

    fun createRoundDrawable(resources: Resources, size: Int, @ColorInt color: Int): Drawable {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        val rect = RectF(Rect(0, 0, size, size))

        paint.isAntiAlias = true
        paint.color = color
        canvas.drawRoundRect(rect, size / 2f, size / 2f, paint)

        return RoundedBitmapDrawableFactory.create(resources, bitmap)
    }
}