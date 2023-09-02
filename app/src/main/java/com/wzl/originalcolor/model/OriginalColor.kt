package com.wzl.originalcolor.model

import android.graphics.Color
import androidx.annotation.ColorInt
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @Author lu
 * @Date 2023/4/29 01:16
 * @ClassName: OriginalColor
 * @Description:
 */
data class OriginalColor(
    // CMYK 四色
    @SerializedName("CMYK")
    val CMYK: IntArray = intArrayOf(0, 0, 0, 0),
    @SerializedName("RGB")
    val RGB: IntArray = intArrayOf(0, 0, 0),
    @SerializedName("hex")
    val HEX: String = "#000000",
    @SerializedName("name")
    val NAME: String = "",
    // 中文拼音
    val pinyin: String = ""
): Serializable {

   @ColorInt
    fun getRGBColor(): Int {
       if (this.RGB.size != 3) {
           return Color.WHITE
       }
       this.RGB.let { rgb ->
           return Color.rgb(rgb[0], rgb[1], rgb[2])
       }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OriginalColor

        if (!CMYK.contentEquals(other.CMYK)) return false
        if (!RGB.contentEquals(other.RGB)) return false
        if (HEX != other.HEX) return false
        if (NAME != other.NAME) return false
        if (pinyin != other.pinyin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = CMYK.contentHashCode()
        result = 31 * result + RGB.contentHashCode()
        result = 31 * result + HEX.hashCode()
        result = 31 * result + NAME.hashCode()
        result = 31 * result + pinyin.hashCode()
        return result
    }
}