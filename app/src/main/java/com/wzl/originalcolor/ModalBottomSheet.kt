package com.wzl.originalcolor

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
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
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        val cardColor = Color.parseColor(originalColor.HEX)
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
            val shareView = layoutInflater.inflate(R.layout.layout_share_color_card, null, false)
            shareView.findViewById<TextView>(R.id.colorName).apply {
                text = originalColor.NAME
                setTextColor(cardColor.brightness(-0.1f))
            }
            shareView.findViewById<TextView>(R.id.colorHEX).text = originalColor.HEX
            shareView.findViewById<TextView>(R.id.colorRGB).text = originalColor.RGB.arrayToString()
            shareView.findViewById<TextView>(R.id.colorCMYK).text =
                originalColor.CMYK.arrayToString()
            shareView.findViewById<View>(R.id.colorDisplayView)
                .setCornerBackground(16, cardColor)
            val backgroundColor = ColorUtils.blendARGB(cardColor, Color.WHITE, 0.5F)
            shareView.findViewById<ConstraintLayout>(R.id.shareCardView)
                .setCornerBackground(16, backgroundColor)

            val bitmap = BitmapUtil.viewToBitmap(
                shareView,
                400.dp(requireContext()),
                250.dp(requireContext()),
                16.dp(requireContext()).toFloat()
            )
            BitmapUtil.shareBitmap(requireContext(), bitmap, originalColor.NAME)
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

    private fun initSensorManager() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private var initDegree = -1F
    override fun onSensorChanged(p0: SensorEvent?) {
        val values = p0?.values ?: return
//        Log.i("sensor: ", values.contentToString())
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val degreeX = if (isPortrait) values[1] * 9F else values[0] * 9F
        val degreeY = if (isPortrait) values[0] * 9F else values[1] * 9F
        if (initDegree == -1F) initDegree = degreeX
        binding.colorCardView.animate()
            .rotationX(degreeX - initDegree)
            .rotationY(degreeY)
            .setDuration(200)
            .start()
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