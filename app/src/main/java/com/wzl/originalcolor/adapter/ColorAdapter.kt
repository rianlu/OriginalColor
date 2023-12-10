package com.wzl.originalcolor.adapter

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseDifferAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.wzl.originalcolor.view.GradientLayout
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
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