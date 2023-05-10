package com.wzl.originalcolor

import com.google.gson.annotations.SerializedName

/**
 * @Author lu
 * @Date 2023/4/29 01:16
 * @ClassName: OriginalColor
 * @Description:
 */
data class OriginalColor(
    // CMYK 四色
    val CMYK: IntArray = intArrayOf(0, 0, 0, 0),
    val RGB: IntArray = intArrayOf(0, 0, 0),
    @SerializedName("hex")
    val HEX: String = "#000000",
    @SerializedName("name")
    val NAME: String = ""
    // 中文拼音
    // private val pinyin: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OriginalColor

        if (!CMYK.contentEquals(other.CMYK)) return false
        if (!RGB.contentEquals(other.RGB)) return false
        if (HEX != other.HEX) return false
        if (NAME != other.NAME) return false

        return true
    }

    override fun hashCode(): Int {
        var result = CMYK.contentHashCode()
        result = 31 * result + RGB.contentHashCode()
        result = 31 * result + HEX.hashCode()
        result = 31 * result + NAME.hashCode()
        return result
    }
}