package com.wzl.originalcolor.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
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
object BitmapUtils {

    fun viewToBitmap(view: View, width: Int, height: Int, roundPx: Float = 0f): Bitmap {
        val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        view.measure(measuredWidth, measuredHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.TRANSPARENT)
        if (roundPx != 0f) {
            val rect = Rect(0, 0, bmp.width, bmp.height)
            val rectF = RectF(rect)
            val paint = Paint()
            paint.isAntiAlias = true
            c.drawRoundRect(rectF, roundPx, roundPx, paint)
        }
        view.draw(c)
        return bmp
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