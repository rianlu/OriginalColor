package com.wzl.originalcolor.utils

import android.graphics.*
import java.util.Random

object BitmapExtensions {

    /**
     * 将普通的 View 截图转化为带有"纸张质感"和"立体阴影"的卡片图
     * @param source 原始 View 的 Bitmap
     * @return 处理后的 Bitmap
     */
    fun createPaperTextureCard(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height

        // 1. 设置边框宽度 (比如宽度的 5%)，模拟相框留白
        val borderSize = (width * 0.05f).toInt()
        val outWidth = width + borderSize * 2
        val outHeight = height + borderSize * 2

        // 创建最终输出的 Bitmap
        val output = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 2. 绘制卡片底色 (米白色，比纯白更有质感)
        canvas.drawColor(Color.parseColor("#F2F0EB")) // 暖调米白

        // 3. 绘制阴影 (Drop Shadow)
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A000000") // 柔和的黑
            // 模糊半径 40，X轴偏移 0，Y轴偏移 20
            maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
        }
        val cardRect = RectF(
            borderSize.toFloat(),
            borderSize.toFloat(),
            (outWidth - borderSize).toFloat(),
            (outHeight - borderSize).toFloat()
        )
        // 阴影稍微下移一点
        val shadowRect = RectF(cardRect).apply { offset(0f, 15f) }
        canvas.drawRect(shadowRect, shadowPaint)

        // 4. 绘制原本的内容
        canvas.drawBitmap(source, borderSize.toFloat(), borderSize.toFloat(), null)

        // 5. [核心步骤] 添加噪点纹理 (Noise)
        addNoiseTexture(canvas, outWidth, outHeight)

        return output
    }

    private fun addNoiseTexture(canvas: Canvas, width: Int, height: Int) {
        // 使用 PorterDuff.Mode.MULTIPLY 或 DARKEN 混合模式
        val noisePaint = Paint().apply {
            alpha = 18 // 透明度 (0-255)，15-25 之间比较合适，太高会脏
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        }

        // 生成一个小的噪点图块，然后平铺 (性能优化)
        val noiseTileSize = 256
        val noiseTile = createNoiseTile(noiseTileSize)

        // 使用 BitmapShader 进行平铺
        val shader = BitmapShader(noiseTile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        noisePaint.shader = shader

        // 绘制覆盖全图的矩形
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), noisePaint)

        noiseTile.recycle()
    }

    private fun createNoiseTile(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        val pixels = IntArray(size * size)
        val random = Random()

        for (i in pixels.indices) {
            // 生成随机灰度值 (200-255 之间，偏亮)
            val gray = 200 + random.nextInt(55)
            pixels[i] = Color.rgb(gray, gray, gray)
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }
}