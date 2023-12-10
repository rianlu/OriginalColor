package com.wzl.originalcolor.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.view.setPadding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp

class OriginalColorCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GradientLayout(context, attrs, defStyleAttr) {

    private var originalColor: OriginalColor
    private var textColor = cardColor.brightness(
        if (cardColor.isLight()) -0.3F
        else -0.1F
    )

    private val colorPinyin = TextView(context)
    private val colorName = TextView(context)
    init {
        originalColor = OriginalColor(
            CMYK = intArrayOf(0, 69, 86, 0),
            RGB = intArrayOf(248, 107, 29),
            HEX = "#f86b1d",
            NAME = "金莲花橙",
            pinyin = "jinlianhuacheng"
        )
        orientation = VERTICAL
        setPadding(16.dp(context))
        colorPinyin.isAllCaps = true
        colorPinyin.setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6)
        colorName.apply {
            setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline4)
            setTypeface(typeface, Typeface.BOLD)
        }
        addView(colorPinyin)
        addView(colorName)
//        addView(colorName, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
//            setMargins(marginStart, marginTop + 4.dp(context), marginEnd, marginBottom)
//        })
        setOriginalColor(originalColor)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initOriginalColorCard()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = MeasureSpec.getSize(heightMeasureSpec)
//        setMeasuredDimension(wSpecSize, 240.dp(context))
        setMeasuredDimension(wSpecSize, desiredHeight)
    }

    fun setOriginalColor(originalColor: OriginalColor? = null, offsets: FloatArray? = null) {
        originalColor?.let { this.originalColor = it }
        setGradientColor(this.originalColor.getRGBColor(), offsets)
        textColor = cardColor.brightness(
            if (cardColor.isLight()) -0.3F
            else -0.1F
        )
        invalidate()
    }

    private fun initOriginalColorCard() {
        colorPinyin.apply {
            text = originalColor.pinyin
            setTextColor(textColor.setAlpha(0.6F))
        }
        colorName.apply {
            text = originalColor.NAME
            setTextColor(textColor)
        }
    }
}
