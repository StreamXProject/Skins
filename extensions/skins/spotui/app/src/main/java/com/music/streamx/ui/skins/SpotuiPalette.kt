package com.music.streamx.ui.skins

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** Pick an artwork swatch with color rather than a black border; keep white controls readable. */
fun spotuiArtworkTint(colors: List<Color>): Color {
    var color = colors.maxByOrNull {
        (maxOf(it.red, it.green, it.blue) - minOf(it.red, it.green, it.blue)) * 2f + it.luminance() * .5f
    } ?: Color(0xFF503D66)
    if (maxOf(color.red, color.green, color.blue) < .12f) color = lerp(color, Color.White, .1f)
    while (color.luminance() > .16f) color = lerp(color, Color.Black, .15f)
    return color.copy(alpha = 1f)
}
