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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wzl.originalcolor.databinding.ModalBottomSheetContentBinding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.BitmapUtils
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxExtensions
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.UiModeUtils
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

        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        binding.bottomSheetLayout.setCornerBackground(24, originalColor.getRGBColor().setAlpha(0.5F))
        binding.colorCardView.apply {
            setCardBackgroundColor(Color.parseColor(originalColor.HEX))
        }
        binding.colorPinyin.setTextColor(
            originalColor.getRGBColor()
                .brightness(if (UiModeUtils.isLightMode(requireContext())) -0.1F else 0.3F)
                .setAlpha(0.3F)
        )
        binding.colorName.apply {
            text = originalColor.NAME
            setTextColor(originalColor.getRGBColor()
                .brightness(if (UiModeUtils.isLightMode(requireContext())) -0.1F else 0.3F)
            )
        }

        binding.copyColorLayout.setCornerBackground(24, originalColor.getRGBColor().setAlpha(0.1F))
        binding.colorHEX.apply {
            text = originalColor.HEX
            setOnClickListener {
                copyToClipboardAndToast(this, originalColor.HEX, clipboardManager)
            }
        }
        binding.colorRGB.apply {
            val rgbString = originalColor.RGB.arrayToString()
            text = rgbString
            setOnClickListener {
                copyToClipboardAndToast(this, rgbString, clipboardManager)
            }
        }
        binding.colorCMYK.apply {
            val cmykString = originalColor.CMYK.arrayToString()
            text = cmykString
            setOnClickListener {
                copyToClipboardAndToast(this, cmykString, clipboardManager)
            }
        }

        binding.shareColor.setOnClickListener {
            val shareView = layoutInflater.inflate(R.layout.layout_share_color_card, null, false)
            shareView.findViewById<TextView>(R.id.colorName).text = originalColor.NAME
            shareView.findViewById<TextView>(R.id.colorHEX).text = originalColor.HEX
            shareView.findViewById<TextView>(R.id.colorRGB).text = originalColor.RGB.arrayToString()
            shareView.findViewById<TextView>(R.id.colorCMYK).text =
                originalColor.CMYK.arrayToString()
            shareView.findViewById<View>(R.id.colorDisplayView)
                .setBackgroundColor(Color.parseColor(originalColor.HEX))
            val brighterColor =
                Color.parseColor(originalColor.HEX).brightness(0.3F)
            shareView.findViewById<CardView>(R.id.shareCardView)
                .setCardBackgroundColor(brighterColor)

            val bitmap = BitmapUtils.viewToBitmap(
                shareView,
                400.dp(requireContext()),
                250.dp(requireContext()),
                16.dp(requireContext()).toFloat()
            )
            BitmapUtils.shareBitmap(requireContext(), bitmap, originalColor.NAME)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun copyToClipboardAndToast(view: TextView, content: String, clipboardManager: ClipboardManager) {
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
        val cornerRadius = radius.toFloat()
        val shape = ShapeDrawable(RoundRectShape(
            floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                cornerRadius, cornerRadius, cornerRadius, cornerRadius
            ), null, null)
        )
        shape.paint.color = color
        this.background = shape
    }

    private fun View.setCornerBackground(leftRadius: Int, topRadius: Int, rightRadius: Int, bottomRadius: Int, @ColorInt color: Int) {
        val shape = ShapeDrawable(RoundRectShape(
            floatArrayOf(
                leftRadius.dp(requireContext()).toFloat(),
                leftRadius.dp(requireContext()).toFloat(),
                topRadius.dp(requireContext()).toFloat(),
                topRadius.dp(requireContext()).toFloat(),
                rightRadius.dp(requireContext()).toFloat(),
                rightRadius.dp(requireContext()).toFloat(),
                bottomRadius.dp(requireContext()).toFloat(),
                bottomRadius.dp(requireContext()).toFloat(),
            ), null, null)
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