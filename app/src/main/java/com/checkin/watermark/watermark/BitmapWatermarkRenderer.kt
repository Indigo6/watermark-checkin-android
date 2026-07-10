package com.checkin.watermark.watermark

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max

class BitmapWatermarkRenderer {
    fun render(
        bitmap: Bitmap,
        lines: List<String>,
    ): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        if (lines.isEmpty()) return output
        val canvas = Canvas(output)
        val scale = max(1f, output.width / 1080f)
        val margin = 56f * scale
        val titleSize = 56f * scale
        val bodySize = 44f * scale
        val lineGap = 14f * scale
        val textLeft = margin
        val maxTextWidth = output.width - margin * 2

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = titleSize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(5f * scale, 2f * scale, 2f * scale, Color.argb(190, 0, 0, 0))
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = bodySize
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(5f * scale, 2f * scale, 2f * scale, Color.argb(190, 0, 0, 0))
        }

        val fittedLines = lines.mapIndexed { index, line ->
            val paint = if (index == 0) titlePaint else bodyPaint
            line.fitToWidth(paint, maxTextWidth)
        }
        val lineMetrics = fittedLines.mapIndexed { index, _ ->
            val paint = if (index == 0) titlePaint else bodyPaint
            paint.fontMetrics
        }
        val textHeight = lineMetrics.sumOf { metrics ->
            (metrics.descent - metrics.ascent).toDouble()
        }.toFloat() + lineGap * (lines.size - 1).coerceAtLeast(0)

        var baseline = output.height - margin - textHeight - lineMetrics.first().ascent
        fittedLines.forEachIndexed { index, line ->
            val paint = if (index == 0) titlePaint else bodyPaint
            canvas.drawText(line, textLeft, baseline, paint)
            val metrics = lineMetrics[index]
            baseline += (metrics.descent - metrics.ascent) + lineGap
        }

        return output
    }

    private fun String.fitToWidth(paint: Paint, maxWidth: Float): String {
        if (paint.measureText(this) <= maxWidth) return this
        var end = length
        while (end > 1 && paint.measureText(take(end) + "...") > maxWidth) {
            end--
        }
        return take(end) + "..."
    }
}
