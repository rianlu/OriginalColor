package com.wzl.originalcolor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wzl.originalcolor.utils.VibratorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


/**
 * @Author lu
 * @Date 2023/5/9 22:49
 * @ClassName: SettingsFragment
 * @Description:
 */
class SettingsFragment : PreferenceFragmentCompat() {

    private var donateClickTime = 0
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        val versionPreference = findPreference<Preference>("version")
        val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, PackageInfoFlags.of(0)).versionName
        } else {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        }
        versionPreference?.summary = versionName

        val donatePreference = findPreference<Preference>("donate")
        donatePreference?.setOnPreferenceClickListener {
            VibratorUtils.vibrate(requireContext())
            donateClickTime += 1
            when (donateClickTime) {
                1 -> donatePreference.setTitle(R.string.donate_first_hint)
                2 -> donatePreference.setTitle(R.string.donate_second_hint)
                3 -> donatePreference.setTitle(R.string.donate_third_hint)
                else -> {
                    donatePreference.setTitle(R.string.donate_last_hint)
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        MaterialAlertDialogBuilder(requireContext())
                            .setView(R.layout.layout_donate)
                            .show()
                    }
                }
            }
            true
        }

        val sharePreference = findPreference<Preference>("share")
        sharePreference?.setOnPreferenceClickListener {
            startActivity(Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app))
                type = "text/plain"
            }, getString(R.string.share_to)))
            true
        }

        val clearPreference = findPreference<Preference>("clear")
        val cachePath = File(requireContext().externalCacheDir, "share_cards/")
        var size = 0L
        cachePath.listFiles()?.forEach {
            size += it.length()
        }
        clearPreference?.summary = calculateFileSize(size)
        clearPreference?.setOnPreferenceClickListener {
            VibratorUtils.vibrate(requireContext())
            clearShareCaches(cachePath)
            clearPreference.summary = calculateFileSize(0)
                true
        }

        val sp = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val vibrationPreference = findPreference<SwitchPreferenceCompat>("vibration")
        vibrationPreference?.isChecked = sp.getBoolean("vibration", false)
        vibrationPreference?.setOnPreferenceChangeListener { preference, newValue ->
            val state = newValue as Boolean
            sp.edit().apply {
                putBoolean("vibration", state)
                apply()
            }
            VibratorUtils.updateVibration(state)
            true
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