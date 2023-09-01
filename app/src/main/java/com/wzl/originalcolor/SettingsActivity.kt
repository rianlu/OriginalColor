package com.wzl.originalcolor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.wzl.originalcolor.databinding.ActivitySettingsBinding
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.VibratorUtils
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
        val themeSp = getSharedPreferences(
            Config.SP_GLOBAL_THEME_COLOR, Context.MODE_PRIVATE)
        themeSp.getString(Config.SP_PARAM_THEME_COLOR, null)?.let { hex ->
            initCustomThemeColor(hex)
        }
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

        val settingsSp = getSharedPreferences(Config.SP_SETTINGS, Context.MODE_PRIVATE)
        binding.vibrationSwitch.isChecked = settingsSp.getBoolean(
            Config.SP_PARAM_VIBRATION, true)
        binding.vibrationSwitchItem.setOnClickListener {
            binding.vibrationSwitch.isChecked = !binding.vibrationSwitch.isChecked
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            settingsSp.edit().apply {
                putBoolean(Config.SP_PARAM_VIBRATION, isChecked)
                apply()
            }
            VibratorUtils.updateVibration(isChecked)
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
                AppWidgetManager.getInstance(this)
                    .requestPinAppWidget(serviceComponent, null, null)
            }
        }

        val cachePath = File(externalCacheDir, "share_cards/")
        var size = 0L
        cachePath.listFiles()?.forEach {
            size += it.length()
        }
        binding.cacheSize.text = calculateFileSize(size)
        binding.clearCacheItem.setOnClickListener {
            VibratorUtils.vibrate(this)
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
        binding.vibrationSwitch.apply {
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
}