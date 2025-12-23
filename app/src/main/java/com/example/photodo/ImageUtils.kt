package com.example.photodo // 确保包名和你的一致

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.IOException

object ImageUtils {

    /**
     * 1. 安全地从 Uri 获取 Bitmap (兼容 Android 新旧版本)
     */
    @Throws(IOException::class)
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9.0 (API 28) 及以上使用 ImageDecoder
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true // 允许修改 Bitmap (为了后续缩放)
            }
        } else {
            // 旧版本使用 MediaStore
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    /**
     * 2. 检查并放大 Bitmap (解决 ML Kit 要求最小 32x32 的限制)
     */
    fun ensureMinSize(originalBitmap: Bitmap): Bitmap {
        val minSize = 32
        val width = originalBitmap.width
        val height = originalBitmap.height

        // 如果原本就够大，直接返回原图，不浪费性能
        if (width >= minSize && height >= minSize) {
            return originalBitmap
        }

        // 计算需要放大的倍数
        val scaleWidth = if (width < minSize) minSize.toFloat() / width else 1f
        val scaleHeight = if (height < minSize) minSize.toFloat() / height else 1f
        // 取最大倍数，保持长宽比
        val scaleFactor = maxOf(scaleWidth, scaleHeight)

        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)

        // 生成放大的新图
        return Bitmap.createBitmap(
            originalBitmap, 0, 0, width, height, matrix, true
        )
    }
    // 新增：获取今天的日期字符串，格式必须和 OCR 结果一致
    fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.CHINA)
        return sdf.format(java.util.Date())
    }
}