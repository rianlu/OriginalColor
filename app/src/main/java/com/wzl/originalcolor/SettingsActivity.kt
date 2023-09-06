package com.wzl.originalcolor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.wzl.originalcolor.databinding.ActivitySettingsBinding
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.RemoteViewsUtil
import com.wzl.originalcolor.utils.SpUtil
import com.wzl.originalcolor.utils.VibratorUtil
import com.wzl.originalcolor.utils.WorkManagerUtil
import com.wzl.originalcolor.widget.ColorWidgetProvider
import java.io.File

/**
 * @Author lu
 * @Date 2023/5/9 22:56
 * @ClassName: SettingsActivity
 * @Description:
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 文本包含链接挑传
        binding.copyrightText.movementMethod = LinkMovementMethod.getInstance()
        initCustomThemeColor(SpUtil.getLocalThemeColor(this))
        binding.settingsTopAppBar
            .setNavigationOnClickListener { finish() }

        binding.appVersion.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName
        } else {
            packageManager.getPackageInfo(packageName, 0).versionName
        }

        binding.vibrationSwitch.isChecked = SpUtil.getVibrationState(this)
        binding.vibrationSwitchItem.setOnClickListener {
            binding.vibrationSwitch.isChecked = !binding.vibrationSwitch.isChecked
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SpUtil.saveVibrationState(this, isChecked)
            VibratorUtil.updateVibration(isChecked)
        }

        binding.periodRefreshSwitch.isChecked = SpUtil.getWidgetRefreshState(this)
        binding.periodRefreshSwitchItem.setOnClickListener {
            binding.periodRefreshSwitch.isChecked = !binding.periodRefreshSwitch.isChecked
        }
        binding.periodRefreshSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SpUtil.saveWidgetRefreshState(this, isChecked)
            if (isChecked) {
                WorkManagerUtil.startWork(this)
            } else {
                WorkManagerUtil.cancelWork(this)
            }
        }

        binding.shareAppItem.setOnClickListener {
            startActivity(Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_content))
                type = "text/plain"
            }, getString(R.string.share_to)))
        }

        binding.addAppWidgetItem.setOnClickListener {
            val serviceComponent = ComponentName(this, ColorWidgetProvider::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val extras = Bundle()
                // 恢复为主题色
                if (!SpUtil.getWidgetRefreshState(this)) {
                    SpUtil.saveWidgetColor(this, SpUtil.getLocalThemeColor(this))
                }
                val originalColor = ColorData.getWidgetColor(this)
                val remoteViews = RemoteViewsUtil.getWideWidgetView(this, originalColor)
                extras.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
                AppWidgetManager.getInstance(this)
                    .requestPinAppWidget(serviceComponent, extras, null)
            }
        }

        val cachePath = File(externalCacheDir, "share_cards/")
        var size = 0L
        cachePath.listFiles()?.forEach {
            size += it.length()
        }
        binding.cacheSize.text = calculateFileSize(size)
        binding.clearCacheItem.setOnClickListener {
            VibratorUtil.vibrate(this)
            clearShareCaches(cachePath)
            binding.cacheSize.text = calculateFileSize(0)
        }
    }

    private fun clearShareCaches(folder: File) {
        if (!folder.exists()) {
            return
        }
        folder.listFiles()?.forEach {
            it.delete()
        }
        folder.delete()
    }

    private fun calculateFileSize(size: Long): String {
        return if (size >= 1024 * 1024) {
            "${size / 1024} MB"
        } else if (size >= 1024) {
            "${size / 1024} KB"
        } else "$size B"
    }

    private fun initCustomThemeColor(hex: String) {
        val themeColor = Color.parseColor(hex)
        // Copyright
        binding.copyrightText.setLinkTextColor(themeColor)
        // Author
        binding.authorText.setTextColor(themeColor)
        binding.vibrationSwitch.updateTheme(themeColor)
        binding.periodRefreshSwitch.updateTheme(themeColor)
    }

    private fun MaterialSwitch.updateTheme(@ColorInt themeColor: Int) {
        val trackStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val trackColors = intArrayOf(
            themeColor, themeColor.setAlpha(0.1F)
        )
        trackTintList = ColorStateList(trackStates, trackColors)
        trackDecorationTintList = ColorStateList.valueOf(themeColor)
        val thumbStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val thumbColors = intArrayOf(
            Color.WHITE, themeColor
        )
        thumbTintList = ColorStateList(thumbStates, thumbColors)
    }
}