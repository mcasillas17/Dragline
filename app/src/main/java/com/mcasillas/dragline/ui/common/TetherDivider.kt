package com.mcasillas.dragline.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mcasillas.dragline.ui.theme.TetherAmber
import com.mcasillas.dragline.ui.theme.TetherCyan

/**
 * Subtle dragline tether divider motif.
 * Draws a taut silk line with tension anchor nodes.
 */
@Composable
fun TetherDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val accentColor = TetherCyan.copy(alpha = 0.6f)
    val nodeColor = TetherAmber.copy(alpha = 0.8f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .padding(vertical = 4.dp)
    ) {
        val y = size.height / 2f
        val width = size.width
        val centerX = width / 2f

        // Base taut tether line
        drawLine(
            color = outlineColor.copy(alpha = 0.4f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = thickness.toPx(),
            cap = StrokeCap.Round
        )

        // Subtle electric accent over center span
        drawLine(
            color = accentColor,
            start = Offset(centerX - 40.dp.toPx(), y),
            end = Offset(centerX + 40.dp.toPx(), y),
            strokeWidth = (thickness * 1.5f).toPx(),
            cap = StrokeCap.Round
        )

        // Center geometric tension node
        drawCircle(
            color = nodeColor,
            radius = 3.dp.toPx(),
            center = Offset(centerX, y)
        )
    }
}
