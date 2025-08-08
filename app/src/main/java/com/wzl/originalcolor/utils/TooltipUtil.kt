package com.wzl.originalcolor.utils

import android.app.Activity
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wzl.originalcolor.R

object TooltipUtil {

    data class Handle(val popup: PopupWindow)

    fun show(activity: Activity, anchor: View, textRes: Int, preferAbove: Boolean = true, alignStart: Boolean = false): Handle {
        val inflater = LayoutInflater.from(activity)
        val content = inflater.inflate(R.layout.view_tooltip, null)
        val container = content.findViewById<View>(R.id.tooltipContainer)
        val arrowBottom = content.findViewById<ImageView>(R.id.tooltipArrow)
        val arrowTop = content.findViewById<ImageView>(R.id.tooltipArrowTop)
        val textView = content.findViewById<TextView>(R.id.tooltipText)
        textView.setText(textRes)

        val isLight = UiModeUtil.isLightMode(activity)
        val bg: Drawable? = ContextCompat.getDrawable(activity, if (!isLight) R.drawable.bg_tooltip_dark else R.drawable.bg_tooltip)
        container.background = bg
        val arrowDrawableBottom = ContextCompat.getDrawable(activity, R.drawable.ic_tooltip_triangle)
        val arrowDrawableTop = ContextCompat.getDrawable(activity, R.drawable.ic_tooltip_triangle_up)
        val textColor = if (isLight) ContextCompat.getColor(activity, android.R.color.black) else ContextCompat.getColor(activity, android.R.color.white)
        arrowDrawableBottom?.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        arrowDrawableTop?.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
        arrowBottom.setImageDrawable(arrowDrawableBottom)
        arrowTop.setImageDrawable(arrowDrawableTop)
        textView.setTextColor(textColor)

        val window = PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false)
        window.isOutsideTouchable = true
        window.isFocusable = false
        // 触摸转发：允许点击提示覆盖区域时触发锚点点击（避免阻挡）
        window.setTouchInterceptor { _, _ ->
            anchor.performClick()
            if (window.isShowing) window.dismiss()
            true
        }

        anchor.post {
            // 计算屏幕安全区域，智能选择上/下方显示，避免越界
            val screenRect = Rect()
            anchor.rootView.getWindowVisibleDisplayFrame(screenRect)

            // 获取 anchor 在屏幕中的绝对坐标
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val anchorX = location[0]
            val anchorY = location[1]

            // 测量内容尺寸
            content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val contentW = content.measuredWidth
            val contentH = content.measuredHeight

            val anchorW = anchor.width
            val anchorH = anchor.height

            val spaceAbove = anchorY - screenRect.top
            val spaceBelow = screenRect.bottom - (anchorY + anchorH)
            val showAbove = if (preferAbove) contentH <= spaceAbove || spaceAbove >= spaceBelow else spaceBelow >= contentH

            var xOff = if (alignStart) 0 else anchorW/2 - contentW/2
            var yOff = if (showAbove) -contentH else 0

            // 水平边界修正
            val left = anchorX + xOff
            val right = left + contentW
            if (left < screenRect.left) xOff += screenRect.left - left
            if (right > screenRect.right) xOff -= right - screenRect.right

            // y 偏移考虑箭头高度
            val arrowH = arrowBottom.measuredHeight.takeIf { it > 0 } ?: (6 * activity.resources.displayMetrics.density).toInt()
            yOff += if (showAbove) -arrowH else arrowH

            // 箭头可见性：上方显示用底部箭头；下方显示用顶部箭头
            arrowTop.visibility = if (showAbove) View.GONE else View.VISIBLE
            arrowBottom.visibility = if (showAbove) View.VISIBLE else View.GONE

            window.showAsDropDown(anchor, xOff, yOff, Gravity.START)
        }
        return Handle(window)
    }

    fun showAbove(activity: Activity, anchor: View, textRes: Int): Handle = show(activity, anchor, textRes, preferAbove = true)
    fun showBelow(activity: Activity, anchor: View, textRes: Int): Handle = show(activity, anchor, textRes, preferAbove = false)
}

