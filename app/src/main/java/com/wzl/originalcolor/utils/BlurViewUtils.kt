package com.wzl.originalcolor.utils

import android.app.Activity
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

/**
 * @Author lu
 * @Date 2023/5/7 22:57
 * @ClassName: BlurViewUtils
 * @Description:
 */
object BlurViewUtils {

    fun initBlurView(activity: Activity, blurView: BlurView, @ColorInt overlayColor: Int, blurRadius: Float = 20f) {
        val decorView = activity.window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = decorView.background
        blurView
            .setupWith(rootView, object : RenderScriptBlur(activity) {})
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(blurRadius)
            .setOverlayColor(overlayColor)
        blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurView.clipToOutline = true
    }

}