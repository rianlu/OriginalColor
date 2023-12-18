package com.wzl.originalcolor.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseDifferAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.view.OriginalColorCard


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
        return QuickViewHolder(R.layout.item_color_card, parent)
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: OriginalColor?) {
        item?.let { originalColor ->
            holder.getView<OriginalColorCard>(R.id.originalColorCard)
                .setOriginalColor(originalColor)
            val symbol = if (originalColor.NAME.contains("星")) {
                R.drawable.symbol_star
            } else if (originalColor.NAME.contains("鱼")) {
                R.drawable.symbol_fish
            } else if (originalColor.NAME.contains("叶")) {
                R.drawable.symbol_leaf
            } else {
                null
            }
            holder.getView<ImageView>(R.id.symbolImg)
                .apply {
                    if (symbol == null) {
                        isVisible = false
                    } else {
                        isVisible = true
                        setImageResource(symbol)
                        val cardColor = originalColor.getRGBColor()
                        setColorFilter(cardColor)
                        if (!cardColor.isLight()) {
                            alpha = 0.5F
                        }
                    }
                }
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