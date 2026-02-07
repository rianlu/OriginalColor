package com.wzl.originalcolor

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wzl.originalcolor.databinding.ModalBottomSheetContentBinding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.BitmapUtil
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.QRCodeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import com.wzl.originalcolor.utils.BitmapExtensions
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @Author lu
 * @Date 2023/5/5 09:10
 * @ClassName: ModalBottomSheet
 * @Description:
 */
class ModalBottomSheet(private val originalColor: OriginalColor) : BottomSheetDialogFragment(), SensorEventListener {

    private lateinit var binding: ModalBottomSheetContentBinding
    private lateinit var sensorManager: SensorManager
    private var flowingAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalBottomSheetContentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        // 设置相机高度
        val distance = if (isPortrait) 2000 else 5000
        val scale = resources.displayMetrics.density
        binding.colorCardView.cameraDistance = distance * scale

        initSensorManager()
        val cardColor = originalColor.HEX.toColorInt()
        val isLightColor = cardColor.isLight()
        val textColor = originalColor.getRGBColor()
            .brightness(if (isLightColor) -0.1F else 0.1F)
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val sheetBackground = originalColor.getRGBColor().setAlpha(0.5F)
        binding.bottomSheetLayout.setCornerBackground(24, 24, 0, 0, sheetBackground)

        // 设置虚拟键同色
        dialog?.window?.run {
            navigationBarColor = sheetBackground
            WindowCompat.getInsetsController(this, this.decorView)
                .isAppearanceLightNavigationBars = true
        }

        binding.colorCardView.apply {
            setCardBackgroundColor(cardColor)
        }
        binding.colorPinyin.apply {
            text = originalColor.pinyin.uppercase()
            setTextColor(textColor.setAlpha(0.3F)
            )
        }
        binding.colorName.apply {
            text = originalColor.NAME
            setTextColor(textColor.setAlpha(0.9F)
            )
        }

        binding.copyColorLayout
            .setCornerBackground(24, originalColor.getRGBColor().setAlpha(0.1F))
        binding.colorHEX.apply {
            text = originalColor.HEX
            setOnClickListener {
                copyToClipboard(this, originalColor.HEX, clipboardManager)
            }
        }
        binding.colorRGB.apply {
            val rgbString = originalColor.RGB.arrayToString()
            text = rgbString
            setOnClickListener {
                copyToClipboard(this, rgbString, clipboardManager)
            }
        }
        binding.colorCMYK.apply {
            val cmykString = originalColor.CMYK.arrayToString()
            text = cmykString
            setOnClickListener {
                copyToClipboard(this, cmykString, clipboardManager)
            }
        }

        binding.shareColor.setOnClickListener {
            // 1. Inflate 新的海报布局
            val shareView = layoutInflater.inflate(R.layout.layout_share_poster, null, false)

            // 2. 获取控件
            val colorBlock = shareView.findViewById<View>(R.id.colorBlock)
            val tvName = shareView.findViewById<TextView>(R.id.tvColorName)
            val tvPinyin = shareView.findViewById<TextView>(R.id.tvColorPinyin)
            val tvHex = shareView.findViewById<TextView>(R.id.tvHexVal)
            val tvRgb = shareView.findViewById<TextView>(R.id.tvRgbVal)
            val tvCmyk = shareView.findViewById<TextView>(R.id.tvCmykVal)
            val tvDate = shareView.findViewById<TextView>(R.id.tvDate)
            val ivQrCode = shareView.findViewById<ImageView>(R.id.ivQrCode)

            // 3. 绑定颜色与智能文字变色
            val cardColorInt = Color.parseColor(originalColor.HEX)
            colorBlock.setBackgroundColor(cardColorInt)

            // 计算亮度 (0.0-1.0)，超过 0.7 认为是亮色背景
            val isLightBg = ColorUtils.calculateLuminance(cardColorInt) > 0.7

            // 【修复点】：Color.parseColor 是方法，需要用括号 ("#333333")
            val headerTextColor = if (isLightBg) Color.parseColor("#333333") else Color.WHITE

            tvName.setTextColor(headerTextColor)
            // 拼音稍微透明一点
            tvPinyin.setTextColor(ColorUtils.setAlphaComponent(headerTextColor, 200))

            // 如果背景是亮色，去掉文字阴影，否则看起来很脏
            if (isLightBg) {
                tvName.setShadowLayer(0f, 0f, 0f, 0)
                tvPinyin.setShadowLayer(0f, 0f, 0f, 0)
            }

            // 4. 绑定文本数据
            tvName.text = originalColor.NAME
            tvPinyin.text = originalColor.pinyin

            // 直接设置数据，不拼接，确保清晰
            tvHex.text = originalColor.HEX
            // 使用逗号加空格分隔，更易读
            tvRgb.text = originalColor.RGB.arrayToString().replace(" | ", ", ")
            tvCmyk.text = originalColor.CMYK.arrayToString().replace(" | ", ", ")

            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            tvDate.text = dateFormat.format(java.util.Date())

            // 5. 生成标准黑白二维码 (最清晰)
            val downloadUrl = "https://sj.qq.com/appdetail/com.wzl.originalcolor"
            // transparentBackground = false 表示生成白底黑码
            val qrBitmap = QRCodeUtil.createQRCode(downloadUrl, 200, transparentBackground = false)
            ivQrCode.setImageBitmap(qrBitmap)

            // 6. 测量与布局 (设定为 1080宽的竖屏海报)
            val targetWidth = 1080
            val targetHeight = 1920 // 16:9 的竖屏标准比例

            val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(targetHeight, View.MeasureSpec.EXACTLY)

            // 强制 View 按照 1080x1920 进行测量
            shareView.measure(widthSpec, heightSpec)
            // 强制布局到这个矩形区域内
            shareView.layout(0, 0, shareView.measuredWidth, shareView.measuredHeight)

            // 7. 绘图
            val rawBitmap = Bitmap.createBitmap(shareView.measuredWidth, shareView.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(rawBitmap)
            // 绘制纯白背景
            canvas.drawColor(Color.WHITE)
            shareView.draw(canvas)

            // 8. 强烈建议：保留纸张纹理
            val finalBitmap = BitmapExtensions.createPaperTextureCard(rawBitmap)

            // 回收资源
            rawBitmap.recycle()
            if (qrBitmap != null && !qrBitmap.isRecycled) {
                qrBitmap.recycle()
            }

            // 分享
            BitmapUtil.shareBitmapJpeg(requireContext(), finalBitmap, "OriginalColor_${originalColor.NAME}")
            dismiss()
        }

        // 显示动画
        val layoutAnimation = AnimationUtils.loadLayoutAnimation(
            context, R.anim.layout_bottom_to_top
        )
        binding.bottomSheetLayout.layoutAnimation = layoutAnimation

        flowingAnimator = ObjectAnimator
            .ofFloat(binding.colorCardView, "translationY", 0F, -15F, 0F, 15F, 0F)
            .setDuration(2000)
            .apply {
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        flowingAnimator?.start()
    }

    /**
     * 将 Bitmap 中的黑色像素替换为指定颜色 (用于二维码染色)
     * @param source 原始二维码图片 (通常是黑白的)
     * @param color 目标颜色
     */
    private fun tintBitmap(source: Bitmap, color: Int): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        // 使用 SRC_IN 模式：只在源图像非透明的地方绘制新颜色
        // 对于标准二维码，黑色部分是非透明的，白色部分通常是透明或白色
        // 如果你的二维码库生成的背景是白色，可能需要先去除白色背景，或者使用 ColorFilter
        val filter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        paint.colorFilter = filter

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return output
    }

    private fun initSensorManager() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // SENSOR_DELAY_GAME provides smoother updates for UI animations
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    // Physics constants
    private val ALPHA = 0.1f // Low-Pass Filter coefficient (Lower = smoother/heavier)
    private val MAX_ANGLE = 15f // Max rotation angle in degrees
    private val PARALLAX_FACTOR = 1.5f // Text movement multiplier
    private val SHEEN_FACTOR = 4.0f // Sheen movement multiplier

    private var lastX = 0f
    private var lastY = 0f
    private var baseDegreeX = 0f
    private var isBaseSet = false

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        // 1. Raw Data Mapping
        // Map sensor data to rotation degrees.
        // In portrait: X-axis sensor affects Y-axis rotation (tipping left/right)
        // Y-axis sensor affects X-axis rotation (tipping up/down)
        val targetX = (if (isPortrait) values[1] else values[0]) * 5f // Sensitivity adjusted
        val targetY = (if (isPortrait) values[0] else values[1]) * 5f

        // Set baseline on first frame to prevent initial jump
        if (!isBaseSet) {
            baseDegreeX = targetX
            isBaseSet = true
        }

        // 2. Low-Pass Filter (Smoothing)
        // smooth = old * (1-alpha) + new * alpha
        lastX = lastX + ALPHA * ((targetX - baseDegreeX) - lastX)
        lastY = lastY + ALPHA * (targetY - lastY)

        // 3. Clamping (prevent extreme rotation)
        val rotateX = lastX.coerceIn(-MAX_ANGLE, MAX_ANGLE)
        val rotateY = lastY.coerceIn(-MAX_ANGLE, MAX_ANGLE)

        // 4. Apply Transformations
        // Card Rotation
        binding.colorCardView.rotationX = rotateX
        binding.colorCardView.rotationY = rotateY

        // Specular Highlight (The "Glaze" Light)
        // Light moves significantly        // Specular Highlight (The "Glaze" Light)
        val sheen = binding.colorCardView.findViewById<View>(R.id.colorSheen)
        sheen?.apply {
            // Base alpha 0.0 + dynamic up to 0.4 (Subtle but visible)
            alpha = (0.0f + (Math.abs(rotateX) + Math.abs(rotateY)) / 30f).coerceIn(0f, 0.4f)
            translationX = -rotateY * SHEEN_FACTOR
            translationY = -rotateX * SHEEN_FACTOR
        }

        // Parallax Text (Floating Effect)
        // Text moves slightly opposite to rotation to appear "floating above"
        binding.colorName.translationX = rotateY * PARALLAX_FACTOR
        binding.colorName.translationY = rotateX * PARALLAX_FACTOR
        
        binding.colorPinyin.translationX = rotateY * PARALLAX_FACTOR
        binding.colorPinyin.translationY = rotateX * PARALLAX_FACTOR
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        flowingAnimator?.cancel()
    }
    private fun copyToClipboard(
        view: TextView,
        content: String,
        clipboardManager: ClipboardManager
    ) {
        val clipData = ClipData.newPlainText("color", content)
        clipboardManager.setPrimaryClip(clipData)
        CoroutineScope(Dispatchers.Main).launch {
            view.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_check, 0
            )
            delay(700)
            view.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_copy, 0
            )
        }
        Toast.makeText(
            requireContext(),
            getString(R.string.copied_to_clipboard),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun View.setCornerBackground(radius: Int, @ColorInt color: Int) {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius.dp(requireContext()).toFloat())
            }
        }
        setCornerBackground(radius, radius, radius, radius, color)
    }

    private fun View.setCornerBackground(
        leftRadius: Int,
        topRadius: Int,
        rightRadius: Int,
        bottomRadius: Int,
        @ColorInt color: Int
    ) {
        val shape = ShapeDrawable(
            RoundRectShape(
                floatArrayOf(
                    leftRadius.dp(requireContext()).toFloat(),
                    leftRadius.dp(requireContext()).toFloat(),
                    topRadius.dp(requireContext()).toFloat(),
                    topRadius.dp(requireContext()).toFloat(),
                    rightRadius.dp(requireContext()).toFloat(),
                    rightRadius.dp(requireContext()).toFloat(),
                    bottomRadius.dp(requireContext()).toFloat(),
                    bottomRadius.dp(requireContext()).toFloat(),
                ), null, null
            )
        )
        shape.paint.color = color
        this.background = shape
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    private fun IntArray.arrayToString(): String {
        val builder = StringBuilder()
        this.forEachIndexed { index, i ->
            builder.append(i)
            if (index != this.size - 1) {
                builder.append(" | ")
            }
        }
        return builder.toString()
    }
}