package com.checkin.watermark.watermark

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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
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
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 6.dp, vertical = 6.dp),
    ) {
        lines.forEachIndexed { index, line ->
            if (index > 0) {
                Spacer(Modifier.height(3.dp))
            }
            Text(
                text = line,
                color = Color.White,
                fontSize = if (index == 0) 17.sp else 14.sp,
                fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Medium,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.72f),
                        offset = Offset(x = 1.5f, y = 1.5f),
                        blurRadius = 4f,
                    ),
                ),
            )
        }
    }
}
