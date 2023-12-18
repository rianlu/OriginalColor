package com.wzl.originalcolor.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.wzl.originalcolor.R
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.UiModeUtil

open class GradientLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val isLightMode = UiModeUtil.isLightMode(context)
    @ColorInt var cardColor = resources.getColor(R.color.primary_color, null)
    private var gradientDrawable = GradientDrawable()
    private var radius = 16.dp(context).toFloat()
    private var offsets = floatArrayOf(0F, 0.3F, 1F)

    init {
        initGradientBackground()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initGradientBackground()
    }

    fun setGradientColor(@ColorInt cardColor: Int, offsets: FloatArray? = null) {
        this.cardColor = cardColor
        offsets?.let { this.offsets = it }
        invalidate()
    }

    private fun initGradientBackground() {
        val startColor = if (isLightMode) cardColor.setAlpha(0.7F)
        else cardColor.brightness(0.2F)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            gradientDrawable.setColors(
                intArrayOf(startColor, startColor, cardColor),
                offsets
            )
        } else {
            gradientDrawable.colors = intArrayOf(startColor, cardColor)
        }
        gradientDrawable.cornerRadius = radius
        background = gradientDrawable
    }
}