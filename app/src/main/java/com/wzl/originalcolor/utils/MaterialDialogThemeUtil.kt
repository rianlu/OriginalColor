package com.wzl.originalcolor.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.shape.MaterialShapeDrawable

/**
 * 自定义 MaterialDialog 配置
 */
object MaterialDialogThemeUtil {

    /**
     * 生成 MaterialDialog背景
     * 注：此处 context 必须为dialog内部的 context！！！
     */
    fun generateMaterialShapeDrawable(context: Context, @ColorInt color: Int): MaterialShapeDrawable {
        val surfaceColor =
            MaterialColors.getColor(context, R.attr.colorSurface, javaClass.canonicalName)
        val materialShapeDrawable = MaterialShapeDrawable(
            context,
            null,
            R.attr.alertDialogStyle,
            R.style.MaterialAlertDialog_MaterialComponents
        )
        materialShapeDrawable.initializeElevationOverlay(context)
        materialShapeDrawable.fillColor = ColorStateList.valueOf(generateBackgroundViewColor(context, color))

        // dialogCornerRadius first appeared in Android Pie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val dialogCornerRadiusValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.dialogCornerRadius, dialogCornerRadiusValue, true)
            val dialogCornerRadius = dialogCornerRadiusValue.getDimension(
                context.resources.displayMetrics
            )
            if (dialogCornerRadiusValue.type == TypedValue.TYPE_DIMENSION && dialogCornerRadius >= 0) {
                materialShapeDrawable.setCornerSize(dialogCornerRadius)
            }
        }
        return materialShapeDrawable
    }

    /**
     * 生成动态背景色（使用SearchView的实现）
     */
    fun generateBackgroundViewColor(context: Context, @ColorInt color: Int): Int {
        val backgroundColor: Int =
            MaterialColors.getColor(context, R.attr.colorSurface, Color.WHITE)
        val elevation: Float = 16.5F
        val overlayAlphaFraction: Float =
            ElevationOverlayProvider(context).calculateOverlayAlphaFraction(elevation)
        val backgroundAlpha = Color.alpha(backgroundColor)
        val backgroundColorOpaque = ColorUtils.setAlphaComponent(backgroundColor, 255)
        var overlayColorOpaque =
            MaterialColors.layer(backgroundColorOpaque, color, overlayAlphaFraction)
        if (overlayAlphaFraction > 0 && color != Color.TRANSPARENT) {
            val overlayAccentColor =
                ColorUtils.setAlphaComponent(color, Math.round(0.02 * 255).toInt())
            overlayColorOpaque = MaterialColors.layer(overlayColorOpaque, overlayAccentColor)
        }
        return ColorUtils.setAlphaComponent(overlayColorOpaque, backgroundAlpha)
    }

    fun dynamicMaterialDialogBuilder(
        context: Context,
        themeColor: Int? = null
    ): MaterialAlertDialogBuilder {
        val themeColor = themeColor ?: Color.parseColor(SpUtil.getLocalThemeColor(context))
        val builder = MaterialAlertDialogBuilder(context)
        return builder
            .setBackground(
                generateMaterialShapeDrawable(
                    builder.context, themeColor
                )
            )
    }
}