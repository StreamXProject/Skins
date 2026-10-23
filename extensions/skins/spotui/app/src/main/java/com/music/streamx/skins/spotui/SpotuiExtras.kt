package com.music.streamx.skins.spotui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.music.streamx.ui.skins.SpotuiIcons

@Composable
internal fun SpotuiBottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) {
    val floor = if (pageColor.luminance() < .5f) Color.Black else MaterialTheme.colorScheme.background
    Box(modifier.fillMaxWidth().height(if (withMiniPlayer) 180.dp else 120.dp)
        .background(Brush.verticalGradient(0f to Color.Transparent, .55f to floor.copy(alpha = .65f), 1f to floor)))
}
@Composable
internal fun SpotuiLibraryStats(onOpen: () -> Unit) {
    TextButton(onClick = onOpen, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Listening stats") }
}
internal fun spotuiResourceIcon(name: String): ImageVector? = when (name) {
    "save" -> SpotuiIcons.Add
    "saved" -> SpotuiIcons.Check
    "next" -> SpotuiIcons.Next
    "previous" -> SpotuiIcons.Previous
    "play" -> SpotuiIcons.Play
    "pause" -> SpotuiIcons.Pause
    "shuffle" -> SpotuiIcons.Shuffle
    else -> null
}
