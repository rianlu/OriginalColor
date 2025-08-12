package com.wzl.originalcolor.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.content.FileProvider
import com.wzl.originalcolor.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * @Author lu
 * @Date 2023/5/10 02:06
 * @ClassName: BitmapUtils
 * @Description:
 */
object BitmapUtil {

    fun viewToBitmap(view: View, width: Int, height: Int, roundPx: Float = 0f): Bitmap {
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        // 透明底（供支持透明的分享方使用）
        if (roundPx > 0f) {
            val rectF = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
            val path = android.graphics.Path().apply {
                addRoundRect(rectF, roundPx, roundPx, android.graphics.Path.Direction.CW)
            }
            c.save()
            c.clipPath(path)
            c.drawColor(Color.TRANSPARENT)
            view.draw(c)
            c.restore()
        } else {
            c.drawColor(Color.TRANSPARENT)
            view.draw(c)
        }
        return bmp
    }

        fun viewToBitmapWithBackground(view: View, width: Int, height: Int, roundPx: Float = 0f, @androidx.annotation.ColorInt backgroundColor: Int): Bitmap {
            val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            view.measure(measuredWidth, measuredHeight)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            if (roundPx > 0f) {
                val rectF = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
                val path = android.graphics.Path().apply {
                    addRoundRect(rectF, roundPx, roundPx, android.graphics.Path.Direction.CW)
                }
                c.save()
                c.clipPath(path)
                c.drawColor(backgroundColor)
                view.draw(c)
                c.restore()
            } else {
                c.drawColor(backgroundColor)
                view.draw(c)
            }
            return bmp
        }

        fun shareBitmapJpeg(context: Context, bitmap: Bitmap, fileName: String) {
            val cachePath = File(context.externalCacheDir, "share_cards/")
            cachePath.mkdirs()
            val file = File(cachePath, "$fileName.jpg")
            try {
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
                }
            } catch (_: Exception) {}
            val myImageFileUri = FileProvider.getUriForFile(
                context, context.applicationContext.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri)
            intent.type = "image/jpeg"
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_to)))
        }


    fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String) {
        val cachePath = File(context.externalCacheDir, "share_cards/")
        cachePath.mkdirs()
        val file = File(cachePath, "$fileName.png")
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val myImageFileUri = FileProvider.getUriForFile(
            context, context.applicationContext.packageName + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri)
        intent.type = "image/png"
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_to)))
    }
}