package com.wzl.originalcolor.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp

class AppWidgetPreview @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : OriginalColorCard(context, attrs, defStyleAttr) {

    init {
        setOriginalColor(null, floatArrayOf(0F, 0.4F, 1F))
    }
}
