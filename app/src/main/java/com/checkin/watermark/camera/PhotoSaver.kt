package com.checkin.watermark.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.checkin.watermark.record.CaptureRecord
import com.checkin.watermark.record.CaptureRecordFactory
import com.checkin.watermark.record.JpegRotation
import com.checkin.watermark.domain.LocationSnapshot
import com.checkin.watermark.watermark.BitmapWatermarkRenderer
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PhotoSaver(
    private val context: Context,
    private val renderer: BitmapWatermarkRenderer = BitmapWatermarkRenderer(),
) {
    data class SavedPhoto(
        val uri: Uri?,
        val displayName: String,
        val record: CaptureRecord,
    )

    private val displayNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    fun renderWatermarkAndSaveToGallery(
        sourceFile: File,
        watermarkLines: List<String>,
        location: LocationSnapshot,
    ): SavedPhoto? {
        val originalBytes = sourceFile.readBytes()
        val decodedBitmap = BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size) ?: return null
        val sourceBitmap = rotateBitmapIfNeeded(decodedBitmap, sourceFile)
        val outputBitmap = renderer.render(sourceBitmap, watermarkLines)
        val nowMillis = System.currentTimeMillis()
        val displayName = "IMG_${displayNameFormatter.format(LocalDateTime.now())}.jpg"
        val outputBytesFile = File(context.cacheDir, "watermarked-${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputBytesFile).use {
            outputBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 94, it)
        }
        val outputBytes = outputBytesFile.readBytes()
        val record = CaptureRecordFactory.create(
            capturedAt = Instant.now(),
            location = location,
            originalBytes = originalBytes,
            outputBytes = outputBytes,
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.TITLE, displayName.removeSuffix(".jpg"))
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_TAKEN, nowMillis)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WatermarkCheckin")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(outputBytes)
            } ?: return null
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            SavedPhoto(uri = uri, displayName = displayName, record = record)
        } else {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.TITLE, displayName.removeSuffix(".jpg"))
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_TAKEN, nowMillis)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(outputBytes)
            } ?: return null
            SavedPhoto(uri = uri, displayName = displayName, record = record)
        }
    }

    private fun rotateBitmapIfNeeded(
        bitmap: android.graphics.Bitmap,
        sourceFile: File,
    ): android.graphics.Bitmap {
        val exif = ExifInterface(sourceFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
        val degrees = JpegRotation.degreesForExifOrientation(orientation)
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
