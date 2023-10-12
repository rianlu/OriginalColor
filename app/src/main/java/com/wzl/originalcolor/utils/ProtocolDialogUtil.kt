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

    fun show(context: Context) {
//        if ((SpUtil.getPrivacyPolicyState(context)) &&
//                    (BuildConfig.FLAVOR == BuildConfig.var_base)
//        )
        if (SpUtil.getPrivacyPolicyState(context)) return
        val hexColor = SpUtil.getLocalThemeColor(context)
        val rootView =
            LayoutInflater.from(context)
                .inflate(R.layout.layout_app_protocol, null, false)
        rootView.findViewById<TextView>(R.id.userAgreement).apply {
            setTextColor(Color.parseColor(hexColor))
            setOnClickListener {
                showUserAgreement(context)
            }
        }
        rootView.findViewById<TextView>(R.id.privacyPolicy).apply {
            setTextColor(Color.parseColor(hexColor))
            setOnClickListener {
                showPrivacyPolicy(context)
            }
        }
        AlertDialog.Builder(context)
            .setTitle("使用须知")
            .setView(rootView)
            .setNegativeButton("不同意") { p0, p1 -> exitProcess(0) }
            .setPositiveButton("同意") { p0, p1 ->
                SpUtil.savePrivacyPolicyState(
                    context,
                    true
                )
            }
            .setCancelable(false)
            .show()
    }

    fun showUserAgreement(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.user_agreement))
            .setView(TextView(context).apply {
                text = HtmlCompat.fromHtml(
                    context.getString(R.string.user_agreement_content),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                movementMethod = LinkMovementMethod.getInstance()
                maxLines = 10

                textSize = 6.dp(context).toFloat()
                val textPadding = 16.dp(context)
                setPadding(textPadding, textPadding, textPadding, textPadding)
            })
            .setPositiveButton("我已知晓", null)
            .setCancelable(false)
            .show()
    }

    fun showPrivacyPolicy(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.privacy_policy))
            .setView(TextView(context).apply {
                text = HtmlCompat.fromHtml(
                    context.getString(R.string.privacy_policy_content),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                movementMethod = LinkMovementMethod.getInstance()
                maxLines = 10

                textSize = 6.dp(context).toFloat()
                val textPadding = 16.dp(context)
                setPadding(textPadding, textPadding, textPadding, textPadding)
            })
            .setPositiveButton("我已知晓", null)
            .setCancelable(false)
            .show()
    }
}