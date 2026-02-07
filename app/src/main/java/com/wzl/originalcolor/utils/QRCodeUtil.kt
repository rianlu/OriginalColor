package com.wzl.originalcolor.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Hashtable

object QRCodeUtil {

    /**
     * 生成二维码 Bitmap (强制去白边)
     *
     * @param content   二维码内容
     * @param size      期望生成的 Bitmap 宽/高 (像素)
     * @param transparentBackground 是否透明背景
     */
    fun createQRCode(content: String, size: Int, transparentBackground: Boolean = true): Bitmap? {
        if (content.isEmpty()) return null

        try {
            // 1. 配置参数
            val hints = Hashtable<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            // 虽然这里设为0，但 ZXing 往往还会保留一点边距，所以下面需要手动裁剪
            hints[EncodeHintType.MARGIN] = 0

            // 2. 生成原始矩阵
            val qrCodeWriter = QRCodeWriter()
            val originalMatrix = qrCodeWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            // 3. 【核心步骤】计算去白边后的有效区域
            // enclosingRectangle 返回一个数组 [left, top, width, height]
            // 这代表了二维码真正有内容的矩形区域
            val rec = originalMatrix.enclosingRectangle ?: return null

            val left = rec[0]
            val top = rec[1]
            val matrixWidth = rec[2]
            val matrixHeight = rec[3]

            // 4. 创建像素数组
            // 我们需要生成的最终尺寸是 size * size
            val pixels = IntArray(size * size)

            // 5. 遍历目标像素，反向映射到矩阵的有效区域
            // 这种算法不仅去除了白边，还能保证生成的二维码填满指定的 size
            for (y in 0 until size) {
                for (x in 0 until size) {
                    // 坐标映射：目标坐标 -> 矩阵有效区域坐标
                    // srcX = left + (x * matrixWidth / size)
                    val srcX = left + (x * matrixWidth / size)
                    val srcY = top + (y * matrixHeight / size)

                    if (originalMatrix[srcX, srcY]) {
                        // 有点的地方：黑色
                        pixels[y * size + x] = Color.BLACK
                    } else {
                        // 没点的地方：背景色
                        pixels[y * size + x] = if (transparentBackground) Color.TRANSPARENT else Color.WHITE
                    }
                }
            }

            // 6. 创建 Bitmap
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)

            return bitmap

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // createQRCodeBitmap 方法建议也用上面的逻辑替换，或者直接删除用 createQRCode 即可。
}