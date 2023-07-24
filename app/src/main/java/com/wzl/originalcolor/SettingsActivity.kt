package com.wzl.originalcolor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.wzl.originalcolor.databinding.ActivitySettingsBinding
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

        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.enterTransition = Fade()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsTopAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.appVersion.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
        } else {
            packageManager.getPackageInfo(packageName, 0).versionName
        }

        val sp = getSharedPreferences("settings", Context.MODE_PRIVATE)
        binding.vibrationSwitch.isChecked = sp.getBoolean("vibration", true)
        binding.vibrationSwitchItem.setOnClickListener {
            binding.vibrationSwitch.isChecked = !binding.vibrationSwitch.isChecked
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            sp.edit().apply {
                putBoolean("vibration", isChecked)
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
            "${size/1024} MB"
        } else if (size >= 1024) {
            "${size/1024} KB"
        } else  "$size B"
    }
}