package com.wzl.originalcolor.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author lu
 * @Date 2023/5/3 01:14
 * @ClassName: ColorItemDecoration
 * @Description:
 */
class ColorItemDecoration(private val context: Context, private val gridCount: Int = 1): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val margin = PxUtils.dp2px(context, 16)
        if (gridCount == 1) {
            if (position == 0) {
                outRect.top = margin
            }
            outRect.left = margin
            outRect.right = margin
            outRect.bottom = margin
        } else if (gridCount == 3) {
            if (position % 3 == 1) {
                outRect.left = 0
                outRect.right = 0
            } else {
                outRect.left = margin
                outRect.right = margin
            }
            if (position < 3) {
                outRect.top = margin
            } else {
                outRect.top = 0
            }
        }
        else {
            outRect.top = margin
            outRect.left = margin
            outRect.right = margin
            outRect.bottom = margin
        }
        outRect.bottom = margin
    }
}