package com.wzl.originalcolor.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import com.wzl.originalcolor.MainActivity
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp

object RemoteViewsUtil {

    fun getWideWidgetView(context: Context, randomColor: OriginalColor): RemoteViews {
        return generateWidgetView(context, randomColor, R.layout.layout_wide_widget)
    }

    fun getSmallWidgetView(context: Context, randomColor: OriginalColor): RemoteViews {
        return generateWidgetView(context, randomColor, R.layout.layout_small_widget)
    }

    private fun generateWidgetView(
        context: Context,
        randomColor: OriginalColor,
        @LayoutRes layoutId: Int
    ): RemoteViews {
        val isLightMode = UiModeUtil.isLightMode(context)
        val color = Color.parseColor(randomColor.HEX)
        val pinyinColor = color.brightness(
            if (color.isLight()) -0.3F
            else if (UiModeUtil.isLightMode(context)) -0.1F else 0.3F
        ).setAlpha(0.6F)
        val nameColor = color.brightness(
            if (color.isLight()) -0.3F
            else if (UiModeUtil.isLightMode(context)) -0.1F else 0.3F
        )
        return RemoteViews(context.packageName, layoutId).also {
            // 小部件点击打开app平滑过渡
            // https://developer.android.com/develop/ui/views/appwidgets/enhance#enable-smoother-transitions
            it.setInt(R.id.widgetBackground, "setBackgroundColor", color)
            it.setTextViewText(R.id.colorPinyin, randomColor.pinyin)
            it.setTextColor(R.id.colorPinyin, pinyinColor)
            it.setTextViewText(R.id.colorName, randomColor.NAME)
            it.setTextColor(R.id.colorName, nameColor)
            val intent = Intent(context, MainActivity::class.java).also { intent ->
                intent.putExtra("widgetColor", randomColor)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            it.setOnClickPendingIntent(R.id.colorBackground, pendingIntent)
        }.also {
            // 渐变背景
            val gradientBitmap = generateGradientBitmap(
                context,
                intArrayOf(if (isLightMode) color.setAlpha(0.7F)
                else color.brightness(0.2F), color, color),
                floatArrayOf(0F, 0.6F, 1F)
            )
            it.setImageViewBitmap(R.id.colorBackground, gradientBitmap)
        }
    }

    private fun generateGradientBitmap(
        context: Context,
        colors: IntArray,
        positions: FloatArray
    ): Bitmap? {
        val width = 600.dp(context)
        val height = 200.dp(context)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val gradient = LinearGradient(
            (width / 2).toFloat(), 0f, (width / 2).toFloat(), height.toFloat(),
            colors, positions, Shader.TileMode.CLAMP
        )
        val paint = Paint()
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }
}