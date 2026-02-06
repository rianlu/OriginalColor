package com.wzl.originalcolor.utils

import android.content.Context
import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.wzl.originalcolor.R
import com.wzl.originalcolor.utils.PxExtensions.dp
import kotlin.system.exitProcess

object ProtocolDialogUtil {

    fun show(context: Context, onAccepted: (() -> Unit)? = null) {
        if (SpUtil.getPrivacyPolicyState(context)) {
            onAccepted?.invoke()
            return
        }
        val hexColor = SpUtil.getLocalThemeColor(context)
        val themeColor = Color.parseColor(hexColor)
        val rootView =
            LayoutInflater.from(context)
                .inflate(R.layout.layout_app_protocol, null, false)
        rootView.findViewById<TextView>(R.id.userAgreement).apply {
            setTextColor(themeColor)
            setOnClickListener {
                showUserAgreement(context)
            }
        }
        rootView.findViewById<TextView>(R.id.privacyPolicy).apply {
            setTextColor(themeColor)
            setOnClickListener {
                showPrivacyPolicy(context)
            }
        }
        var accepted = false
        MaterialDialogThemeUtil.dynamicMaterialDialogBuilder(context, themeColor)
            .setTitle("使用须知")
            .setView(rootView)
            .setNegativeButton("不同意") { _, _ -> exitProcess(0) }
            .setPositiveButton("同意") { _, _ ->
                SpUtil.savePrivacyPolicyState(context, true)
                accepted = true
            }
            .setOnDismissListener {
                if (accepted) onAccepted?.invoke()
            }
            .setCancelable(false)
            .show().also { dialog ->
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(themeColor)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(themeColor)
            }
    }

    fun showUserAgreement(context: Context) {
        val hexColor = SpUtil.getLocalThemeColor(context)
        val themeColor = Color.parseColor(hexColor)
        
        val scrollView = androidx.core.widget.NestedScrollView(context)
        val textView = TextView(context).apply {
            text = HtmlCompat.fromHtml(
                context.getString(R.string.user_agreement_content),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            movementMethod = LinkMovementMethod.getInstance()
            // Removed maxLines limit, let ScrollView handle it
            textSize = 15f // Use standard text size, or keep explicit unit if needed. 
            // original logic: textSize = 6.dp(context).toFloat() which is likely px being treated as sp? 
            // 6dp is approx 18px on xxhdpi. setTextSize(float) is SP. 18sp is huge.
            // Let's stick to system default or the user's intended physical size.
            // If user meant 6dp physical size converted to SP...
            // setTextSize(TypedValue.COMPLEX_UNIT_PX, 6.dp(context).toFloat()) is correct usage if they want pixel specific.
            // But usually setTextSize(14f) (sp) is safer.
            // Let's assume they want readable text.
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            
            val textPadding = 16.dp(context)
            setPadding(textPadding, textPadding, textPadding, textPadding)
        }
        scrollView.addView(textView)

        MaterialDialogThemeUtil.dynamicMaterialDialogBuilder(context, themeColor)
            .setTitle(context.getString(R.string.user_agreement))
            .setView(scrollView)
            .setPositiveButton("我已知晓", null)
            .setCancelable(false)
            .show().also {
                it.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(themeColor)
                it.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(themeColor)
            }
    }

    fun showPrivacyPolicy(context: Context) {
        val hexColor = SpUtil.getLocalThemeColor(context)
        val themeColor = Color.parseColor(hexColor)
        
        val scrollView = androidx.core.widget.NestedScrollView(context)
        val textView = TextView(context).apply {
            text = HtmlCompat.fromHtml(
                context.getString(R.string.privacy_policy_content),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            movementMethod = LinkMovementMethod.getInstance()
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            val textPadding = 16.dp(context)
            setPadding(textPadding, textPadding, textPadding, textPadding)
        }
        scrollView.addView(textView)

        MaterialDialogThemeUtil.dynamicMaterialDialogBuilder(context, themeColor)
            .setTitle(context.getString(R.string.privacy_policy))
            .setView(scrollView)
            .setPositiveButton("我已知晓", null)
            .setCancelable(false)
            .show().also {
                it.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(themeColor)
                it.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(themeColor)
            }
    }
}