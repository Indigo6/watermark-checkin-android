package com.checkin.watermark.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import com.checkin.watermark.watermark.BitmapWatermarkRenderer
import java.io.File
import java.io.FileOutputStream

class PhotoSaver(
    private val context: Context,
    private val renderer: BitmapWatermarkRenderer = BitmapWatermarkRenderer(),
) {
    fun renderWatermarkAndSaveToGallery(
        sourceFile: File,
        watermarkLines: List<String>,
    ): Boolean {
        val sourceBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return false
        val outputBitmap = renderer.render(sourceBitmap, watermarkLines)
        val displayName = "watermark-checkin-${System.currentTimeMillis()}.jpg"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WatermarkCheckin")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            context.contentResolver.openOutputStream(uri)?.use {
                outputBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 94, it)
            } ?: return false
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            true
        } else {
            val outputFile = File(context.getExternalFilesDir("Pictures"), displayName)
            FileOutputStream(outputFile).use {
                outputBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 94, it)
            }
            true
        }
    }
}
