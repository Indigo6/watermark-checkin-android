package com.checkin.watermark.watermark

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.max

class BitmapWatermarkRenderer {
    fun render(
        bitmap: Bitmap,
        lines: List<String>,
    ): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val scale = max(1f, output.width / 1080f)
        val margin = 24f * scale
        val padding = 18f * scale
        val titleSize = 36f * scale
        val bodySize = 28f * scale
        val lineGap = 12f * scale

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(23, 33, 43)
            textSize = titleSize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(23, 33, 43)
            textSize = bodySize
        }
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(232, 255, 255, 255)
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 118, 110)
        }

        val textWidth = lines.maxOfOrNull { line ->
            val paint = if (lines.indexOf(line) == 0) titlePaint else bodyPaint
            paint.measureText(line)
        } ?: 0f
        val textHeight = lines.mapIndexed { index, _ ->
            if (index == 0) titleSize else bodySize
        }.sum() + lineGap * (lines.size - 1).coerceAtLeast(0)

        val width = (textWidth + padding * 2).coerceAtMost(output.width - margin * 2)
        val height = textHeight + padding * 2
        val left = margin
        val top = output.height - margin - height
        val rect = RectF(left, top, left + width, top + height)

        canvas.drawRoundRect(rect, 8f * scale, 8f * scale, backgroundPaint)
        canvas.drawRect(left, top, left + 8f * scale, top + height, accentPaint)

        var baseline = top + padding + titleSize
        lines.forEachIndexed { index, line ->
            val paint = if (index == 0) titlePaint else bodyPaint
            canvas.drawText(line, left + padding + 8f * scale, baseline, paint)
            baseline += (if (index == 0) titleSize else bodySize) + lineGap
        }

        return output
    }
}
