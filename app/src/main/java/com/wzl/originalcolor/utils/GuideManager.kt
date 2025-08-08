package com.wzl.originalcolor.utils

import android.app.Activity
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.wzl.originalcolor.R

object GuideManager {

    private var current: TapTargetView? = null

    fun showRandom(activity: Activity, navView: View, onNext: () -> Unit) {
        dismiss()
        val outer = getOuterColor(activity)
        val text = getTextOn(outer)
        val dim = getDimColor(activity)
        val rect = Rect()
        navView.getGlobalVisibleRect(rect)
        val target = TapTarget.forBounds(rect, activity.getString(R.string.guide_random))
            .outerCircleColorInt(outer)
            .targetCircleColorInt(text)
            .titleTextColorInt(text)
            .descriptionTextColorInt(text)
            .dimColorInt(dim)
            .drawShadow(true)
            .cancelable(false)
            .transparentTarget(true)
            .tintTarget(true)
        current = TapTargetView.showFor(activity, target, object: TapTargetView.Listener() {
            override fun onTargetClick(view: TapTargetView?) {
                // 转发点击到实际导航按钮，触发功能
                navView.performClick()
                // 不在此推进链路，等待 MainActivity 的真实回调推进
            }

            override fun onOuterCircleClick(view: TapTargetView?) {
                // 点击外圈遮罩也将事件转发给目标，增强触发成功率
                navView.performClick()
            }
        })
    }

    fun showLongPress(activity: Activity, anchorView: View, onNext: () -> Unit) {
        dismiss()
        val outer = getOuterColor(activity)
        val text = getTextOn(outer)
        val dim = getDimColor(activity)
        // 使用 forBounds + 透明目标，避免 forView 造成的目标重复绘制
        val loc = IntArray(2)
        anchorView.getLocationInWindow(loc)
        val rect = android.graphics.Rect(loc[0], loc[1], loc[0] + anchorView.width, loc[1] + anchorView.height)
        val target = TapTarget.forBounds(rect, activity.getString(R.string.guide_long_press))
            .outerCircleColorInt(outer)
            .titleTextColorInt(text)
            .descriptionTextColorInt(text)
            .dimColorInt(dim)
            .drawShadow(true)
            .cancelable(false)
            .transparentTarget(true)
            .tintTarget(false)
            .id(1002)
        current = TapTargetView.showFor(activity, target, object: TapTargetView.Listener() {
            override fun onTargetLongClick(view: TapTargetView?) {
                anchorView.performLongClick()
            }
            override fun onTargetClick(view: TapTargetView?) {
                anchorView.performLongClick()
            }
        })
        // 确保在最顶层：将 TapTargetView 提到 DecorView 顶部
        current?.bringToFront()
        current?.parent?.let {
            if (it is View) (it as View).bringToFront()
        }
    }

    fun showTitleTap(activity: Activity, toolbar: View, onNext: () -> Unit) {
        dismiss()
        val outer = getOuterColor(activity)
        val text = getTextOn(outer)
        val dim = getDimColor(activity)
        val target = TapTarget.forView(toolbar, activity.getString(R.string.guide_title_tap))
            .outerCircleColorInt(outer)
            .targetCircleColorInt(text)
            .titleTextColorInt(text)
            .descriptionTextColorInt(text)
            .dimColorInt(dim)
            .drawShadow(true)
            .cancelable(false)
            .transparentTarget(true)
            .tintTarget(false)
        current = TapTargetView.showFor(activity, target, object: TapTargetView.Listener() {
            override fun onTargetClick(view: TapTargetView?) {
                // 点击高亮区域即转发给标题栏，触发实际点击
                toolbar.performClick()
            }
        })
    }

    fun dismiss() {
        current?.dismiss(false)
        current = null
    }

    private fun getOuterColor(activity: Activity): Int {
        // 使用当前主题色，确保在浅色/深色下都保持一致的品牌感
        return Color.parseColor(SpUtil.getLocalThemeColor(activity))
    }

    private fun getTextOn(bg: Int): Int {
        val r = Color.red(bg)
        val g = Color.green(bg)
        val b = Color.blue(bg)
        // YIQ 判断，阈值可按需微调
        val yiq = (r * 299 + g * 587 + b * 114) / 1000
        return if (yiq >= 180) Color.BLACK else Color.WHITE
    }

    private fun getDimColor(activity: Activity): Int {
        // 轻度遮罩，兼容浅色/深色模式
        return Color.parseColor("#99000000")
    }
}

