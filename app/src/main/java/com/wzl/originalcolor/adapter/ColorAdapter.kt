package com.wzl.originalcolor.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseDifferAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.google.android.material.card.MaterialCardView
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.R
import com.wzl.originalcolor.utils.InnerColorUtils
import com.wzl.originalcolor.utils.InnerColorUtils.isLight
import com.wzl.originalcolor.utils.InnerColorUtils.setAlpha
import com.wzl.originalcolor.utils.InnerColorUtils.setBrightness
import com.wzl.originalcolor.utils.PxUtils


/**
 * @Author lu
 * @Date 2023/5/3 00:46
 * @ClassName: ColorAdapter
 * @Description:
 */
class ColorAdapter : BaseDifferAdapter<OriginalColor, QuickViewHolder>(ColorDiffCallback()) {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): QuickViewHolder {
        return QuickViewHolder(R.layout.item_color, parent)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: OriginalColor?) {
        if (item == null) {
            return
        }
        // TODO Adapt Light Mode and Dark Mode
        val textColor = Color.parseColor(item.HEX)
        holder.getView<TextView>(R.id.colorPinyin).apply {
            text = item.pinyin
            setTextColor(Color.parseColor(item.HEX).setBrightness(if (textColor.isLight()) {
                -0.3F
            } else {
                0F
            }).setAlpha(0.6F))
        }
        holder.getView<TextView>(R.id.colorName).apply {
            text = item.NAME
            setTextColor(Color.parseColor(item.HEX).setBrightness(if (textColor.isLight()) {
                -0.3F
            } else {
                0F
            }))
        }
        holder.getView<LinearLayout>(R.id.colorBackground).apply {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(item.getRGBColor().setAlpha(0.6F), item.getRGBColor())
            )
            gradientDrawable.cornerRadius = PxUtils.dp2px(context, 16).toFloat()
            background = gradientDrawable
        }
    }
}

class ColorDiffCallback: DiffUtil.ItemCallback<OriginalColor>() {
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