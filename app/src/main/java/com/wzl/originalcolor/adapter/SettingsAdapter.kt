package com.wzl.originalcolor.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.SettingItem
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.HapticFeedbackUtil.addHapticFeedback

class SettingsAdapter(private val settings: List<SettingItem>, private val themeColor: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TEXT = 0
        const val VIEW_TYPE_SWITCH = 1
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingsTextContainer: FrameLayout = itemView.findViewById(R.id.settingsTextContainer)
        val titleTextView: TextView = itemView.findViewById(R.id.settingTitle)
        val subTitleTextView: TextView = itemView.findViewById(R.id.settingSubTitle)
    }

    class SwitchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val settingsSwitchContainer: FrameLayout = itemView.findViewById(R.id.settingsSwitchContainer)
        val titleTextView: TextView = itemView.findViewById(R.id.settingTitle)
        val switchView: MaterialSwitch = itemView.findViewById(R.id.settingSwitch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_item_text, parent, false)
                TextViewHolder(itemView)
            }
            VIEW_TYPE_SWITCH -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_item_switch, parent, false)
                SwitchViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val setting = settings[position]
        when (holder) {
            is TextViewHolder -> {
                if (setting is SettingItem.Text) {
                    holder.settingsTextContainer.setOnClickListener { setting.onClick(position) }
                    holder.titleTextView.text = setting.title
                    holder.subTitleTextView.text = setting.subTitle
                    // 默认文本色
                    if (setting.subTitleColor != Color.TRANSPARENT) {
                        holder.subTitleTextView.apply {
                            setTextColor(themeColor)
                            typeface = Typeface.DEFAULT_BOLD
                            alpha = 1F
                        }
                    }
                }
            }
            is SwitchViewHolder -> {
                if (setting is SettingItem.Switch) {
                    holder.settingsSwitchContainer.addHapticFeedback()
                    holder.settingsSwitchContainer.setOnClickListener { setting.onCheckedChange(position, setting.isChecked) }
                    holder.titleTextView.text = setting.title
                    holder.switchView.isChecked = setting.isChecked
                    holder.switchView.updateTheme(themeColor)
                }
            }
        }
    }

    override fun getItemCount(): Int = settings.size

    override fun getItemViewType(position: Int): Int {
        val setting = settings[position]
        return when (setting) {
            is SettingItem.Text -> VIEW_TYPE_TEXT
            is SettingItem.Switch -> VIEW_TYPE_SWITCH
            else -> throw IllegalArgumentException("Invalid setting type")
        }
    }

    fun updateSubTitle(position: Int, subTitle: String) {
        if (settings[position] is SettingItem.Text) {
            (settings[position] as SettingItem.Text).subTitle = subTitle
            notifyItemChanged(position)
        }
    }

    fun updateSwitchState(position: Int, isChecked: Boolean) {
        if (settings[position] is SettingItem.Switch) {
            (settings[position] as SettingItem.Switch).isChecked = isChecked
            notifyItemChanged(position)
        }
    }

    private fun MaterialSwitch.updateTheme(@ColorInt themeColor: Int) {
        val trackStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val trackColors = intArrayOf(
            themeColor, themeColor.setAlpha(0.1F)
        )
        trackTintList = ColorStateList(trackStates, trackColors)
        trackDecorationTintList = ColorStateList.valueOf(themeColor)
        val thumbStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val thumbColors = intArrayOf(
            Color.WHITE, themeColor
        )
        thumbTintList = ColorStateList(thumbStates, thumbColors)
    }
}