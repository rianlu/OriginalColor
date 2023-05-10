package com.wzl.originalcolor

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wzl.originalcolor.databinding.ModalBottomSheetContentBinding
import com.wzl.originalcolor.utils.BlurViewUtils
import com.wzl.originalcolor.utils.InnerColorUtils


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
        val darkColor = InnerColorUtils.getDarkerColor(Color.parseColor(originalColor.HEX))
        val hexColor = String.format("#%06X", 0xFFFFFF and darkColor)
        val builder = StringBuilder(hexColor)
        // 80 99 B3 CC
        builder.insert(1, "B3")
        BlurViewUtils.initBlurView(
            requireActivity(),
            binding.blurView,
            Color.parseColor(builder.toString()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding.colorCardView.apply {
            setCardBackgroundColor(Color.parseColor(originalColor.HEX))
        }
        binding.colorName.text = originalColor.NAME
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

    fun IntArray.arrayToString(): String {
        val builder = StringBuilder()
        this.forEachIndexed { index, i ->
            builder.append(i)
            if (index != this.size - 1) {
                builder.append(", ")
            }
        }
        return builder.toString()
    }
}