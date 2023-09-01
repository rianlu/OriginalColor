package com.wzl.originalcolor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.widget.RemoteViews
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp


class ColorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val randomColor = ColorData.getRandomColor(context)
        val color = Color.parseColor(randomColor.HEX)
        val pinyinColor = color.brightness(
            if (color.isLight()) -0.3F else -0.1F
        ).setAlpha(0.6F)
        val nameColor = color.brightness(
            if (color.isLight()) -0.3F else -0.1F
        )
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color.setAlpha(0.7F), color, color)
        )
        gradientDrawable.cornerRadius = 16.dp(context).toFloat()
        appWidgetIds.forEach { appWidgetId ->
            val remoteViews = RemoteViews(
                context.packageName,
                R.layout.layout_wide_widget
            )
            remoteViews.apply {
//                setInt(R.id.colorBackground, "setBackgroundColor", color)
                setTextViewText(R.id.colorPinyin, randomColor.pinyin)
                setTextColor(R.id.colorPinyin, pinyinColor)
                setTextViewText(R.id.colorName, randomColor.NAME)
                setTextColor(R.id.colorName, nameColor)
                // 渐变背景
                val gradientBitmap = generateGradientBitmap(context, color)
                setImageViewBitmap(R.id.colorBackground, gradientBitmap)
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0,intent,
                    PendingIntent.FLAG_IMMUTABLE)
                setOnClickPendingIntent(R.id.colorBackground, pendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun generateGradientBitmap(context: Context, color: Int): Bitmap? {
        val width = 500.dp(context) // Width in pixels
        val height = 250.dp(context) // Height in pixels
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val gradient = LinearGradient(
            0F, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(color.setAlpha(0.7F), color, color),
            floatArrayOf(0F, 0.6F, 1F), Shader.TileMode.CLAMP
        )
        val paint = Paint()
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }
}