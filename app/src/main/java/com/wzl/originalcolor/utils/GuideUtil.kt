package com.wzl.originalcolor.utils

import android.app.Activity
import android.view.View
import com.wzl.originalcolor.R

object GuideUtil {

    fun showLongPressGuide(activity: Activity, anchor: View): TooltipUtil.Handle {
        // 卡片在列表中，容易靠近屏幕下方，优先展示在上方
        return TooltipUtil.show(activity, anchor, R.string.guide_long_press, preferAbove = true)
    }

    fun showTitleTapGuide(activity: Activity, anchor: View): TooltipUtil.Handle {
        // 标题栏在顶部，优先展示在下方，且和左对齐更自然
        return TooltipUtil.show(activity, anchor, R.string.guide_title_tap, preferAbove = false, alignStart = true)
    }

    fun showRandomGuide(activity: Activity, anchor: View): TooltipUtil.Handle {
        // 左上角导航按钮，优先展示在下方，并与按钮左对齐，避免偏到屏幕外
        return TooltipUtil.show(activity, anchor, R.string.guide_random, preferAbove = false, alignStart = true)
    }
}

