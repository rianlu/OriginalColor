package com.wzl.originalcolor.utils

import android.view.HapticFeedbackConstants
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object HapticFeedbackUtil {

    private var state = true

    fun View.addHapticFeedback(feedbackConstants: Int = HapticFeedbackConstants.CLOCK_TICK) {
        if (!state) return
        performHapticFeedback(feedbackConstants, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
    }

    fun View.addStrongHapticFeedback() {
        addHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun update(state: Boolean) {
        this.state = state
    }
}