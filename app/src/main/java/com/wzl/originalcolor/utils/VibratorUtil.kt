package com.wzl.originalcolor.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * @Author lu
 * @Date 2023/5/10 00:21
 * @ClassName: VibratorUtils
 * @Description:
 */
object VibratorUtil {

    private var vibrationState = true

    fun vibrate(context: Context, milliseconds: Long = 100) {
        if (!vibrationState) return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val vibrationWaveFormDurationPattern =
            longArrayOf(0, milliseconds)
        vibrator.vibrate(VibrationEffect.createWaveform(vibrationWaveFormDurationPattern, -1))
    }

    fun updateVibration(state: Boolean) {
        vibrationState = state
    }
}