package com.wzl.originalcolor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wzl.originalcolor.databinding.ModalBottomSheetContentBinding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.BitmapUtil
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.ScreenUtil
import com.wzl.originalcolor.utils.UiModeUtil
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
class ModalBottomSheet(private val originalColor: OriginalColor) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

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

        val isLightMode = UiModeUtil.isLightMode(requireContext())
        val cardColor = Color.parseColor(originalColor.HEX)

        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val sheetBackground = originalColor.getRGBColor().setAlpha(0.5F)
        binding.bottomSheetLayout.setCornerBackground(24, 24, 0, 0, sheetBackground)

        // 设置虚拟键同色
        dialog?.window?.run {
            navigationBarColor = sheetBackground
            WindowCompat.getInsetsController(this, this.decorView).isAppearanceLightNavigationBars =
                true
        }

        binding.colorCardView.apply {
            setCardBackgroundColor(cardColor)
        }
        binding.colorPinyin.apply {
            text = originalColor.pinyin.uppercase()
            setTextColor(
                originalColor.getRGBColor()
                    .brightness(if (isLightMode) -0.1F else 0.3F)
                    .setAlpha(0.3F)
            )
        }
        binding.colorName.apply {
            text = originalColor.NAME
            setTextColor(
                originalColor.getRGBColor()
                    .brightness(if (isLightMode) -0.1F else 0.3F)
                    .setAlpha(0.9F)
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
        startOrderedAnimation()
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
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

    private fun startOrderedAnimation() {
        binding.colorName.animationPrepare()
        binding.colorCardView.animationPrepare()
        binding.copyColorLayout.animationPrepare()
        val isPad = ScreenUtil.isPad(requireContext())
        CoroutineScope(Dispatchers.Main).launch {
            if (isPad) {
                binding.colorPinyin.animationPrepare()
                binding.colorPinyin.translationAnimation().start()
                delay(100)
            }
            binding.colorName.translationAnimation().start()
            delay(100)
            binding.colorCardView.translationAnimation().start()
            if (isPad) {
                delay(100)
            }
            binding.copyColorLayout.translationAnimation().start()
        }
    }

    private fun View.animationPrepare() {
        this.alpha = 0F
        this.translationY = 100F
    }

    private fun View.translationAnimation(): ViewPropertyAnimator {
        return animate()
            .alpha(1F)
            .translationY(0F)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(3F))
    }
}