package com.wzl.originalcolor.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wzl.originalcolor.utils.PxExtensions.dp

/**
 * @Author lu
 * @Date 2023/5/3 01:14
 * @ClassName: ColorItemDecoration
 * @Description:
 */
class ColorItemDecoration(
    private val context: Context,
    private val gridCount: Int = PHONE_GRID_COUNT
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
//        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val margin = 16.dp(context)
        if (gridCount == PHONE_GRID_COUNT) {
            if (position == 0) {
                outRect.top = margin
            }
            outRect.left = margin
            outRect.right = margin
            outRect.bottom = margin
        } else if (gridCount == TABLET_GRID_COUNT) {
            if (position < gridCount) {
                outRect.top = margin
            } else {
                outRect.top = 0
            }
            if (position % gridCount == 0) {
                outRect.left = margin
                outRect.right = margin / 2
            } else {
                outRect.left = margin / 2
                outRect.right = margin
            }
        } else {
            outRect.top = margin
            outRect.left = margin
            outRect.right = margin
        }
        outRect.bottom = margin
    }
}

const val PHONE_GRID_COUNT = 1
const val TABLET_GRID_COUNT = 2