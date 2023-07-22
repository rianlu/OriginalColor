package com.wzl.originalcolor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wzl.originalcolor.databinding.ModalBottomSheetContentBinding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.BitmapUtils
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.PxUtils

/**
 * @Author lu
 * @Date 2023/5/5 09:10
 * @ClassName: ModalBottomSheet
 * @Description:
 */
class ModalBottomSheet(private val originalColor: OriginalColor) : BottomSheetDialogFragment() {

    private lateinit var binding: ModalBottomSheetContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
    }

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

        binding.colorCardView.apply {
            setCardBackgroundColor(Color.parseColor(originalColor.HEX))
        }
        binding.colorName.apply {
            text = originalColor.NAME
            // TODO Adapt Light Mode and Dark Mode
            setTextColor(originalColor.getRGBColor().setAlpha(0.6F)
                .brightness(-0.1F))
        }
        binding.colorHEX.apply {
            text = originalColor.HEX
            setOnClickListener { copyToClipboardAndToast(originalColor.HEX, clipboardManager) }
        }
        binding.colorRGB.apply {
            val rgbString = originalColor.RGB.arrayToString()
            text = rgbString
            setOnClickListener { copyToClipboardAndToast(rgbString, clipboardManager) }
        }
        binding.colorCMYK.apply {
            val cmykString = originalColor.CMYK.arrayToString()
            text = cmykString
            setOnClickListener { copyToClipboardAndToast(cmykString, clipboardManager) }
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
                PxUtils.dp2px(requireContext(), 400),
                PxUtils.dp2px(requireContext(), 250),
                PxUtils.dp2px(requireContext(), 16).toFloat()
            )
            BitmapUtils.shareBitmap(requireContext(), bitmap, originalColor.NAME)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialogView = view?.parent as View
        val mDialogBehavior = BottomSheetBehavior.from(dialogView)
        mDialogBehavior.peekHeight = 2000
        mDialogBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun copyToClipboardAndToast(content: String, clipboardManager: ClipboardManager) {
        val clipData = ClipData.newPlainText("color", content)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(
            requireContext(),
            getString(R.string.copied_to_clipboard),
            Toast.LENGTH_SHORT
        ).show()
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