package com.wzl.originalcolor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wzl.originalcolor.utils.VibratorUtils


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

        val donatePreference = findPreference<Preference>("donate")
        donatePreference?.setOnPreferenceClickListener {
            VibratorUtils.vibrate(requireContext())
            donateClickTime += 1
            when (donateClickTime) {
                1 -> donatePreference.setTitle(R.string.donate_first_hint)
                2 -> donatePreference.setTitle(R.string.donate_second_hint)
                3 -> donatePreference.setTitle(R.string.donate_third_hint)
                4 -> {
                    donatePreference.setTitle(R.string.donate_last_hint)
                    MaterialAlertDialogBuilder(requireContext())
                        .setView(R.layout.layout_donate)
                        .show()
                }
                else -> {}
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

        val sp = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val vibrationPreference = findPreference<Preference>("vibration")
        vibrationPreference?.setDefaultValue(sp.getBoolean("vibration", true))
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
}