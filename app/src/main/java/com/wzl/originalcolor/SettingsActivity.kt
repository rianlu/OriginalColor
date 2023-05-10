package com.wzl.originalcolor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wzl.originalcolor.databinding.ActivitySettingsBinding

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

        binding.settingsTopAppBar.setNavigationOnClickListener {
            finish()
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}