package com.checkin.watermark.watermark

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
            .background(Color(0xF2FFFFFF))
            .border(width = 3.dp, color = Color(0xFF10B981))
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        lines.forEachIndexed { index, line ->
            if (index == 1) {
                Spacer(Modifier.height(3.dp))
            }
            Text(
                text = line,
                color = if (index == 0) Color(0xFF065F46) else Color(0xFF17212B),
                fontSize = if (index == 0) 17.sp else 13.sp,
                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
