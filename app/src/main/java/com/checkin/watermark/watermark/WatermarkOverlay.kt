package com.checkin.watermark.watermark

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WatermarkOverlay(
    lines: List<String>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .widthIn(max = 360.dp)
            .background(Color.White.copy(alpha = 0.90f))
            .border(width = 4.dp, color = Color(0xFF0F766E))
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        lines.forEachIndexed { index, line ->
            Text(
                text = line,
                color = Color(0xFF17212B),
                fontSize = if (index == 0) 16.sp else 13.sp,
                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
