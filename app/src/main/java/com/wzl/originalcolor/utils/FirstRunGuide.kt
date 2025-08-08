package com.wzl.originalcolor.utils

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import com.wzl.originalcolor.Config

object FirstRunGuide {

    private var longPressRetry = 0
    private var awaitingLongPress = false
    private var longPressScrollListener: RecyclerView.OnScrollListener? = null
    private var longPressFallback: Runnable? = null
    private var pendingLongPressPosition: Int? = null

    fun maybeShow(activity: Activity, recyclerView: RecyclerView, topAppBar: View, randomButton: View) {
        if (!SpUtil.getPrivacyPolicyState(activity)) return
        if (!Once.shouldShow(activity, Config.SP_PARAM_GUIDE_RANDOM) &&
            !Once.shouldShow(activity, Config.SP_PARAM_GUIDE_LONG_PRESS) &&
            !Once.shouldShow(activity, Config.SP_PARAM_GUIDE_TITLE_TAP)) return

        // 如果是第一步“随机按钮”引导，优先直接等待导航按钮布局后展示，不依赖 RecyclerView
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_RANDOM)) {
            randomButton.post { startChain(activity, recyclerView, topAppBar, randomButton) }
            return
        }
        // 其它步骤需要等列表布局完成
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                startChain(activity, recyclerView, topAppBar, randomButton)
            }
        })
    }

    private fun startChain(activity: Activity, recyclerView: RecyclerView, topAppBar: View, randomButton: View) {
        // ① 随机按钮（使用具体的导航按钮 View，便于点击直接触发功能）
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_RANDOM)) {
            GuideManager.showRandom(activity, randomButton) { }
            return
        }
        // ② 长按卡片
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_LONG_PRESS)) {
            val lm = recyclerView.layoutManager
            val targetIndex = pendingLongPressPosition
            val targetView: View? = if (lm is androidx.recyclerview.widget.GridLayoutManager && targetIndex != null) {
                lm.findViewByPosition(targetIndex)
            } else if (lm is androidx.recyclerview.widget.LinearLayoutManager && targetIndex != null) {
                lm.findViewByPosition(targetIndex)
            } else {
                // 优先选择第二个可见item（通常完全可见），找不到才用第一个
                recyclerView.getChildAt(1) ?: recyclerView.getChildAt(0)
            }
            if (targetView == null) {
                if (longPressRetry < 10) {
                    longPressRetry++
                    recyclerView.postDelayed({ startChain(activity, recyclerView, topAppBar, randomButton) }, 120)
                }
                return
            }
            longPressRetry = 0
            val card = targetView.findViewById<View>(com.wzl.originalcolor.R.id.originalColorCard) ?: targetView
            // 确保卡片可见性与布局已稳定
            recyclerView.post {
                GuideManager.showLongPress(activity, card) {
                    Once.setShown(activity, Config.SP_PARAM_GUIDE_LONG_PRESS)
                    startChain(activity, recyclerView, topAppBar, randomButton)
                }
            }
            return
        }
        // ③ 标题点击
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_TITLE_TAP)) {
            GuideManager.showTitleTap(activity, topAppBar) {
                Once.setShown(activity, Config.SP_PARAM_GUIDE_TITLE_TAP)
            }
            return
        }
    }

    // 由外部实际动作推进
    fun onRandomClicked(activity: Activity, recyclerView: RecyclerView, topAppBar: View, randomButton: View, targetPosition: Int) {
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_RANDOM)) {
            GuideManager.dismiss()
            Once.setShown(activity, Config.SP_PARAM_GUIDE_RANDOM)
            pendingLongPressPosition = targetPosition
            // 1) 监听滚动停止
            longPressScrollListener?.let { recyclerView.removeOnScrollListener(it) }
            awaitingLongPress = true
            longPressScrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    if (awaitingLongPress && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        awaitingLongPress = false
                        rv.removeOnScrollListener(this)
                        longPressFallback?.let { rv.removeCallbacks(it) }
                        startChain(activity, recyclerView, topAppBar, randomButton)
                    }
                }
            }
            recyclerView.addOnScrollListener(longPressScrollListener!!)
            // 2) 兜底：有些滚动方式不触发 IDLE（例如立即定位），则 220ms 后强制触发
            longPressFallback?.let { recyclerView.removeCallbacks(it) }
            longPressFallback = Runnable {
                if (awaitingLongPress) {
                    awaitingLongPress = false
                    recyclerView.removeOnScrollListener(longPressScrollListener!!)
                    startChain(activity, recyclerView, topAppBar, randomButton)
                }
            }
            recyclerView.postDelayed(longPressFallback!!, 220)
        }
    }

    fun onLongPressed(activity: Activity, recyclerView: RecyclerView, topAppBar: View, randomButton: View) {
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_LONG_PRESS)) {
            GuideManager.dismiss()
            Once.setShown(activity, Config.SP_PARAM_GUIDE_LONG_PRESS)
            startChain(activity, recyclerView, topAppBar, randomButton)
        }
    }

    fun onTitleTapped(activity: Activity, recyclerView: RecyclerView, topAppBar: View, randomButton: View) {
        if (Once.shouldShow(activity, Config.SP_PARAM_GUIDE_TITLE_TAP)) {
            GuideManager.dismiss()
            Once.setShown(activity, Config.SP_PARAM_GUIDE_TITLE_TAP)
        }
    }
}

