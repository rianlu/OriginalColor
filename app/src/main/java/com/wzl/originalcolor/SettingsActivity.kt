package com.wzl.originalcolor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wzl.originalcolor.adapter.SettingsAdapter
import com.wzl.originalcolor.databinding.ActivitySettingsBinding
import com.wzl.originalcolor.model.SettingItem
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.HapticFeedbackUtil
import com.wzl.originalcolor.utils.MaterialDialogThemeUtil
import com.wzl.originalcolor.utils.ProtocolDialogUtil
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.RemoteViewsUtil
import com.wzl.originalcolor.utils.SpUtil
import com.wzl.originalcolor.utils.WorkManagerUtil
import com.wzl.originalcolor.utils.BitmapUtil
import com.wzl.originalcolor.utils.QRCodeUtil
import com.wzl.originalcolor.widget.ColorWidgetProvider
import java.io.File
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

/**
 * @Author lu
 * @Date 2023/5/9 22:56
 * @ClassName: SettingsActivity
 * @Description:
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var cachePath: File
    private lateinit var adapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cachePath = File(externalCacheDir, "share_cards/")
        val originalColor = ColorData.getThemeColor(this)
        val themeColorHex = originalColor.HEX
        val themeColor = themeColorHex.toColorInt()

        val settings = listOf(
            SettingItem.Text(getString(R.string.app_version), getAppVersion()) {
                // 处理点击 "应用版本" 项的逻辑
            },
            SettingItem.Text(
                getString(R.string.current_theme_color),
                originalColor.NAME,
                themeColor
            ) {
                val modalBottomSheet = ModalBottomSheet(originalColor)
                val existedBottomSheet =
                    supportFragmentManager.findFragmentByTag(ModalBottomSheet.TAG)
                if (existedBottomSheet != null && existedBottomSheet is ModalBottomSheet) {
                    existedBottomSheet.dismiss()
                }
                modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
            },
            SettingItem.Switch(
                getString(R.string.haptic_feedback),
                SpUtil.getHapticFeedbackState(this)
            ) { position, isChecked ->
                adapter.updateSwitchState(position, !isChecked)
                SpUtil.saveHapticFeedbackState(this, !isChecked)
                HapticFeedbackUtil.update(!isChecked)
            },
            SettingItem.Switch(
                getString(R.string.period_refresh_widget),
                SpUtil.getWidgetRefreshState(this)
            ) { position, isChecked ->
                val newState = !isChecked
                adapter.updateSwitchState(position, newState)
                SpUtil.saveWidgetRefreshState(this, newState)
                if (newState) {
                    WorkManagerUtil.startWork(this)
                } else {
                    WorkManagerUtil.cancelWork(this)
                }
            },
            SettingItem.Text(getString(R.string.share_app)) {
                // Inflate 海报布局
                val shareView = layoutInflater.inflate(R.layout.layout_share_app_poster, null, false)

                // 获取控件
                val logoCard = shareView.findViewById<androidx.cardview.widget.CardView>(R.id.logoCard)
                val ivAppIcon = shareView.findViewById<ImageView>(R.id.ivAppIcon)
                val ivQrCode = shareView.findViewById<ImageView>(R.id.ivQrCode)
                val tvDate = shareView.findViewById<TextView>(R.id.tvDate)

                // 设置卡片背景为 App 主题色
                logoCard.setCardBackgroundColor(themeColor)

                // 计算亮度 (0.0 是黑, 1.0 是白)
                val luminance = ColorUtils.calculateLuminance(themeColor)
                // 计算背景亮度 (0.0 - 1.0)，大于 0.7 算亮色（比如米色、淡黄）
//                val isLightBg = ColorUtils.calculateLuminance(themeColor) > 0.9
                val isExtremelyLight = luminance > 0.9
                val iconTintColor: Int
                    // 如果背景太亮，就把 Logo 染成黑色；否则染成白色
                if (isExtremelyLight) {
                    iconTintColor = Color.parseColor("#FF5722")
                } else {
                    // 其他所有情况（深色、中性色、普通的浅色），统统保持纯白
                    // 这样能最大程度保留你喜欢的“白色镂空”质感
                    iconTintColor = Color.WHITE
                }

                // 给我们新建的透明底 Logo 染色
                ivAppIcon.setColorFilter(iconTintColor)

                // 设置当天日期
                val dateFormat = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
                tvDate.text = dateFormat.format(java.util.Date())

                // 生成下载二维码
                val downloadUrl = "https://sj.qq.com/appdetail/com.wzl.originalcolor"
                val qrBitmap = QRCodeUtil.createQRCode(downloadUrl, 300, transparentBackground = true)
                if (qrBitmap != null) {
                    // 【智能染色逻辑】
                    // 计算主题色亮度。如果太浅(>0.7)，二维码用深灰(#333333)；否则用主题色。
                    // 这样既好看，又能保证能扫出来。
                    val isLight = ColorUtils.calculateLuminance(themeColor) > 0.7
                    val qrColor = if (isLight) Color.parseColor("#FF5722") else themeColor

                    // 染色！
                    val tintedQr = tintBitmap(qrBitmap, qrColor)
                    ivQrCode.setImageBitmap(tintedQr)
                }

                // 测量与布局 (1080px 宽竖版海报)
                val targetWidth = 1080
                // 高度自适应
                val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

                shareView.measure(widthSpec, heightSpec)
                shareView.layout(0, 0, shareView.measuredWidth, shareView.measuredHeight)

                // 绘图 (绘制白底)
                val rawBitmap = Bitmap.createBitmap(shareView.measuredWidth, shareView.measuredHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(rawBitmap)
                canvas.drawColor(Color.WHITE)
                shareView.draw(canvas)

                // 加上质感纹理 (重要！保持风格统一)
                val finalBitmap = com.wzl.originalcolor.utils.BitmapExtensions.createPaperTextureCard(rawBitmap)

                // 回收
                rawBitmap.recycle()
                if (qrBitmap != null && !qrBitmap.isRecycled) {
                    qrBitmap.recycle()
                }

                // 分享
                BitmapUtil.shareBitmap(this, finalBitmap, "OriginalColor_Share")
            },
            SettingItem.Text(getString(R.string.add_app_widget)) {
                showAddWidgetDialog(this, themeColorHex)
            },
            SettingItem.Text(getString(R.string.clear_cache), calculateFileSize(getCacheSize())) {
                clearShareCaches(cachePath)
                adapter.updateSubTitle(it, calculateFileSize(0))
            },
            SettingItem.Text(getString(R.string.user_agreement)) {
                ProtocolDialogUtil.showUserAgreement(this)
            },
            SettingItem.Text(getString(R.string.privacy_policy)) {
                ProtocolDialogUtil.showPrivacyPolicy(this)
            },
            SettingItem.Footer // Add Footer Item
        )

        binding.settingsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SettingsAdapter(settings, themeColor)
        binding.settingsRecyclerView.adapter = adapter

        initCustomThemeColor(themeColor)
        binding.settingsTopAppBar.setNavigationOnClickListener { finish() }

//        binding.hapticFeedbackSwitch.isChecked = SpUtil.getHapticFeedbackState(this)
//        binding.hapticFeedbackSwitchItem.setOnClickListener {
//            binding.hapticFeedbackSwitch.isChecked = !binding.hapticFeedbackSwitch.isChecked
//        }
//        binding.hapticFeedbackSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//            SpUtil.saveHapticFeedbackState(this, isChecked)
//            buttonView.addHapticFeedback()
//            HapticFeedbackUtil.update(isChecked)
//        }
    }

    private fun tintBitmap(source: Bitmap, color: Int): Bitmap {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        val filter = android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        paint.colorFilter = filter

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
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
                        builder.context, themeColor
                    )
                )
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
//        if (!SpUtil.getWidgetRefreshState(this)) {
//            SpUtil.saveWidgetColor(this, themeColorHex)
//        }
        val originalColor = ColorData.getThemeColor(this)
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
    }

    private fun getAppVersion(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName
        } else {
            packageManager.getPackageInfo(packageName, 0).versionName
        }
    }

    private fun getCacheSize(): Long {
        var size = 0L
        cachePath.listFiles()?.forEach {
            size += it.length()
        }
        return size
    }
}