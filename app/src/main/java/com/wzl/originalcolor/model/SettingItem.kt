package com.wzl.originalcolor.model

import android.graphics.Color
import androidx.annotation.ColorInt

sealed class SettingItem {
    data class Text(val title: String, var subTitle: String = "", @ColorInt val subTitleColor: Int = Color.TRANSPARENT, val onClick: (Int) -> Unit) :
        SettingItem()

    data class Switch(
        val title: String,
        var isChecked: Boolean,
        val onCheckedChange: (Int, Boolean) -> Unit,
    ) : SettingItem()
}