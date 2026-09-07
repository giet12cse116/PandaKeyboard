package com.panda.keyboards.ui.themeeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Utility functions for memory-safe image decoding, cropping, downscaling, and persistence
 * for custom keyboard theme backgrounds.
 */
object ImageUtils {

    /**
     * Efficiently decodes a [Bitmap] from a content [Uri], downscaling large images (e.g. 12MP photos)
     * during decode using [BitmapFactory.Options.inSampleSize] to protect process memory.
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
        return try {
            // First pass: decode dimensions only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            // Calculate sample size for initial downscaling
            var sampleSize = 1
            while ((origWidth / sampleSize) > maxDimension || (origHeight / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            // Second pass: decode actual bitmap with downsampling
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crops a sub-region from [source] defined by normalized or pixel coordinates,
     * and downscales the result to a maximum resolution suitable for keyboard backgrounds (e.g., max 1080x720).
     */
    fun cropAndDownscaleBitmap(
        source: Bitmap,
        cropRect: RectF,
        targetMaxWidth: Int = 1080,
        targetMaxHeight: Int = 720
    ): Bitmap {
        // Clamp crop bounds to source dimensions
        val left = cropRect.left.coerceIn(0f, source.width.toFloat()).toInt()
        val top = cropRect.top.coerceIn(0f, source.height.toFloat()).toInt()
        val right = cropRect.right.coerceIn(left + 1f, source.width.toFloat()).toInt()
        val bottom = cropRect.bottom.coerceIn(top + 1f, source.height.toFloat()).toInt()

        val width = max(1, right - left)
        val height = max(1, bottom - top)

        // 1. Crop sub-bitmap
        val cropped = Bitmap.createBitmap(source, left, top, width, height)

        // 2. Compute downscale factor if cropped dimensions exceed target bounds
        val scaleX = targetMaxWidth.toFloat() / width
        val scaleY = targetMaxHeight.toFloat() / height
        val scale = min(1.0f, min(scaleX, scaleY)) // Only scale down, never scale up

        if (scale >= 1.0f) {
            return cropped
        }

        val scaledWidth = max(1, (width * scale).toInt())
        val scaledHeight = max(1, (height * scale).toInt())

        val scaled = Bitmap.createScaledBitmap(cropped, scaledWidth, scaledHeight, true)
        if (scaled != cropped) {
            cropped.recycle()
        }
        return scaled
    }

    /**
     * Saves [bitmap] as a compressed JPEG to app-private storage (`context.filesDir/custom_theme_images/`).
     * Returns the absolute file path string.
     */
    fun saveBitmapToAppPrivateStorage(
        context: Context,
        bitmap: Bitmap,
        prefix: String = "custom_bg"
    ): String {
        val imagesDir = File(context.filesDir, "custom_theme_images").apply {
            if (!exists()) mkdirs()
        }
        val file = File(imagesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return file.absolutePath
    }

    /**
     * Safely decodes a bitmap from local file path with optional downscaling.
     */
    fun loadDownscaledBitmapFromFile(
        filePath: String,
        reqWidth: Int = 1080,
        reqHeight: Int = 720
    ): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) return null

        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            var sampleSize = 1
            val (width: Int, height: Int) = options.outWidth to options.outHeight
            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2
                while (halfHeight / sampleSize >= reqHeight && halfWidth / sampleSize >= reqWidth) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
