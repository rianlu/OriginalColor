package com.wzl.originalcolor.utils

import android.R.attr
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.view.View


/**
 * @Author lu
 * @Date 2023/5/10 02:06
 * @ClassName: BitmapUtils
 * @Description:
 */
object BitmapUtils {

    fun viewToBitmap(view: View, width: Int, height: Int): Bitmap {
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)
        view.draw(c)
        return bmp
    }

    fun getRoundBitmap(context: Context, bitmap: Bitmap, cornerRadius: Int): Bitmap {
        val radius = PxUtils.dp2px(context, cornerRadius).toFloat()
        val mPaint = Paint()
        val bitmapShader = BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP)
        mPaint.isAntiAlias = true
        mPaint.shader = bitmapShader
        val output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        canvas.drawRoundRect(rectF, radius, radius, mPaint)
        canvas.drawBitmap(bitmap, rect, rect, mPaint)
        return output
    }
}