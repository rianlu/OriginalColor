package com.wzl.originalcolor.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
        val textColor = Color.parseColor(item.HEX)
        val isLightMode = UiModeUtil.isLightMode(context)
        val isLightColor = textColor.isLight()
        holder.getView<TextView>(R.id.colorPinyin).apply {
            text = item.pinyin
            setTextColor(
                textColor.brightness(
                    if (isLightColor) -0.3F
                    else if (isLightMode) -0.1F else 0.3F).setAlpha(0.6F)
            )
        }
        holder.getView<TextView>(R.id.colorName).apply {
            text = item.NAME
            setTextColor(
                textColor.brightness(
                    if (isLightColor) -0.3F
                    else if (isLightMode) -0.1F else 0.3F
                )
            )
        }
        holder.getView<LinearLayout>(R.id.colorBackground).apply {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    if (isLightMode) item.getRGBColor().setAlpha(0.7F)
                    else item.getRGBColor().brightness(0.2F),
                    item.getRGBColor(), item.getRGBColor()
                )
            )
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