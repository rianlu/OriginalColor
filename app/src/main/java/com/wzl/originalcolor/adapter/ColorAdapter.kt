package com.wzl.originalcolor.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseDifferAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.UiModeUtil


/**
 * @Author lu
 * @Date 2023/5/3 00:46
 * @ClassName: ColorAdapter
 * @Description:
 */
class ColorAdapter : BaseDifferAdapter<OriginalColor, QuickViewHolder>(ColorDiffCallback()) {

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_color, parent)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: OriginalColor?) {
        if (item == null) {
            return
        }
        val cardColor = Color.parseColor(item.HEX)
        val isLightMode = UiModeUtil.isLightMode(context)
        val isLightColor = cardColor.isLight()
        val textColor = cardColor.brightness(
            if (isLightColor) -0.3F
            else -0.1F
        )
        holder.getView<TextView>(R.id.colorPinyin).apply {
            text = item.pinyin
            setTextColor(textColor.setAlpha(0.6F))
        }
        holder.getView<TextView>(R.id.colorName).apply {
            text = item.NAME
            setTextColor(textColor)
        }
        holder.getView<LinearLayout>(R.id.colorBackground).apply {
            val gradientDrawable = GradientDrawable()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val startColor = if (isLightMode) cardColor.setAlpha(0.7F)
                else cardColor.brightness(0.2F)
                gradientDrawable.setColors(
                    intArrayOf(startColor, startColor, cardColor),
                    floatArrayOf(0F, 0.3F, 1F)
                )
            } else {
                gradientDrawable.colors = intArrayOf(
                    if (isLightMode) cardColor.setAlpha(0.7F)
                    else item.getRGBColor().brightness(0.2F),
                    cardColor
                )
            }
            gradientDrawable.cornerRadius = 16.dp(context).toFloat()
            background = gradientDrawable
        }
    }
}

class ColorDiffCallback : DiffUtil.ItemCallback<OriginalColor>() {
    override fun areItemsTheSame(oldItem: OriginalColor, newItem: OriginalColor): Boolean {
        return oldItem.HEX == newItem.HEX
    }

    override fun areContentsTheSame(oldItem: OriginalColor, newItem: OriginalColor): Boolean {
        return oldItem.HEX == newItem.HEX
    }

    override fun getChangePayload(oldItem: OriginalColor, newItem: OriginalColor): Any? {
        return null
    }
}