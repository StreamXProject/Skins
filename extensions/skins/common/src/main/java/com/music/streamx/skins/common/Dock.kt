package com.music.streamx.skins.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The bottom navigation as a floating dock: lifted off the screen edges and
 * the gesture bar, with a shadow, so the feed stays visible around and under
 * it instead of ending at a full-width band. Each skin keeps its own tabs and
 * materials inside; this only decides where the dock sits.
 */
@Composable
fun SkinDock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    /** Drawn over [color], for skins whose dock is a gradient (wood, brushed metal). */
    brush: Brush? = null,
    border: BorderStroke? = null,
    elevation: Dp = 10.dp,
    horizontal: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxWidth().navigationBarsPadding().padding(start = horizontal, end = horizontal, bottom = 10.dp)) {
        Surface(Modifier.fillMaxWidth(), shape = shape, color = color, border = border, shadowElevation = elevation) {
            if (brush != null) Box(Modifier.background(brush)) { content() } else content()
        }
    }
}
