package com.wzl.originalcolor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.wzl.originalcolor.databinding.ActivitySettingsBinding
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.HapticFeedbackUtil
import com.wzl.originalcolor.utils.HapticFeedbackUtil.addHapticFeedback
import com.wzl.originalcolor.utils.MaterialDialogThemeUtil
import com.wzl.originalcolor.utils.ProtocolDialogUtil
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.RemoteViewsUtil
import com.wzl.originalcolor.utils.SpUtil
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

        // 文本包含链接跳转
        binding.copyrightText.movementMethod = LinkMovementMethod.getInstance()
        val originalColor = ColorData.getThemeColor(this)
        val themeColorHex = originalColor.HEX
        val themeColor = Color.parseColor(themeColorHex)
        initCustomThemeColor(themeColor)
        binding.settingsTopAppBar.setNavigationOnClickListener { finish() }

        binding.appVersion.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName
        } else {
            packageManager.getPackageInfo(packageName, 0).versionName
        }

        binding.themeColor.apply {
            text = originalColor.NAME
            setTextColor(originalColor.getRGBColor())
        }
        binding.themeColorItem.setOnClickListener {
            val modalBottomSheet = ModalBottomSheet(originalColor)
            val existedBottomSheet = supportFragmentManager.findFragmentByTag(ModalBottomSheet.TAG)
            if (existedBottomSheet != null && existedBottomSheet is ModalBottomSheet) {
                existedBottomSheet.dismiss()
            }
            modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
        }

        binding.hapticFeedbackSwitch.isChecked = SpUtil.getHapticFeedbackState(this)
        binding.hapticFeedbackSwitchItem.setOnClickListener {
            binding.hapticFeedbackSwitch.isChecked = !binding.hapticFeedbackSwitch.isChecked
        }
        binding.hapticFeedbackSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SpUtil.saveHapticFeedbackState(this, isChecked)
            buttonView.addHapticFeedback()
            HapticFeedbackUtil.update(isChecked)
        }

        binding.periodRefreshSwitch.isChecked = SpUtil.getWidgetRefreshState(this)
        binding.periodRefreshSwitchItem.setOnClickListener {
            binding.periodRefreshSwitch.isChecked = !binding.periodRefreshSwitch.isChecked
        }
        binding.periodRefreshSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            SpUtil.saveWidgetRefreshState(this, isChecked)
            buttonView.addHapticFeedback()
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
            showAddWidgetDialog(this, themeColorHex)
        }

        val cachePath = File(externalCacheDir, "share_cards/")
        var size = 0L
        cachePath.listFiles()?.forEach {
            size += it.length()
        }
        binding.cacheSize.text = calculateFileSize(size)
        binding.clearCacheItem.setOnClickListener {
            it.addHapticFeedback()
            clearShareCaches(cachePath)
            binding.cacheSize.text = calculateFileSize(0)
        }

        binding.userAgreementItem.setOnClickListener {
            ProtocolDialogUtil.showUserAgreement(this)
        }

        binding.privacyPolicyItem.setOnClickListener {
            ProtocolDialogUtil.showPrivacyPolicy(this)
        }
        binding.appVersionItem.setOnClickListener { }
    }

    private fun showAddWidgetDialog(context: Context, themeColorHex: String) {
        val themeColor = Color.parseColor(themeColorHex)
        val system = Build.MANUFACTURER
        // TODO 目前暂时无法获取MIUI系统中创建快捷方式的权限情况
        if (system == "Xiaomi" && !SpUtil.getMIUIShortcutState(context)) {
            val builder = MaterialAlertDialogBuilder(context)
            val dialog = builder
                .setBackground(
                    MaterialDialogThemeUtil.generateMaterialShapeDrawable(
                        builder.context, themeColor))
                .setTitle(getString(R.string.dialog_shortcut_permission_title))
                .setMessage(getString(R.string.dialog_shortcut_permission_message))
                .setPositiveButton(getString(R.string.dialog_shortcut_permission_position_button)) { dialog, which ->
                    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                    intent.putExtra("extra_pkgname", packageName)
                    val componentName = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    intent.component = componentName
                    startActivity(intent)
                    SpUtil.saveMIUIShortcutState(this, true)
                }.create()
            dialog.show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).apply {
                setTextColor(themeColor)
            }
            return
        }
        val serviceComponent = ComponentName(this, ColorWidgetProvider::class.java)
        val extras = Bundle()
        // 恢复为主题色
        if (!SpUtil.getWidgetRefreshState(this)) {
            SpUtil.saveWidgetColor(this, themeColorHex)
        }
        val originalColor = ColorData.getWidgetColor(this)
        val remoteViews = RemoteViewsUtil.getWideWidgetView(this, originalColor)
        extras.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
        AppWidgetManager.getInstance(this)
            .requestPinAppWidget(serviceComponent, extras, null)
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

    private fun initCustomThemeColor(@ColorInt themeColor: Int) {
        // Toolbar
        binding.settingsTopAppBar.apply {
            setTitleTextColor(themeColor)
            setNavigationIconTint(themeColor)
        }

        // Copyright
        binding.copyrightText.apply {
            setLinkTextColor(themeColor)
            val radius = 16.dp(this@SettingsActivity).toFloat()
            background = ShapeDrawable(RoundRectShape(
                floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius), null, null
            )).apply {
                paint.color = MaterialDialogThemeUtil.generateBackgroundViewColor(this@SettingsActivity, themeColor)
            }
            setOnClickListener {  }
        }

        // Author
        binding.authorText.setTextColor(themeColor)
        binding.hapticFeedbackSwitch.updateTheme(themeColor)
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