package com.music.streamx.skins.ipod

import com.music.streamx.skins.common.icons.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.streamx.skins.common.*
import kotlin.math.*
import androidx.compose.foundation.BorderStroke
import com.music.streamx.data.model.Account
import com.music.streamx.data.model.Song
import com.music.streamx.ui.components.BottomTab
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.model.ShelfItem
import androidx.compose.ui.graphics.graphicsLayer

@Composable internal fun IpodHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        // The menu screen: a title bar with the battery, then rows that highlight like the real menu.
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
            Column(Modifier.padding(6.dp).clip(RoundedCornerShape(8.dp)).background(c.surface)) {
                Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(c.surfaceVariant, c.surface))).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(SkinIcons.PlayArrow, null, Modifier.size(14.dp), tint = c.onSurfaceVariant)
                    Text("iPod", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Icon(SkinIcons.BatteryFull, null, Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                }
                MenuRow(account?.name ?: "Sign in", onAccount, true)
                MenuRow("History", onHistory, false)
                MenuRow("On device", onLocal, false)
                MenuRow("Explore", onExplore, false)
            }
        }
    }
}

@Composable private fun MenuRow(label: String, onClick: () -> Unit, highlighted: Boolean) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).background(if (highlighted) Brush.verticalGradient(listOf(c.primary, c.primary.copy(alpha = .75f))) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)))
        .padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = if (highlighted) c.onPrimary else c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(SkinIcons.ChevronRight, null, Modifier.size(18.dp), tint = if (highlighted) c.onPrimary else c.onSurfaceVariant)
    }
}

@Composable internal fun IpodBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    SkinDock(shape = RoundedCornerShape(16.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Box(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Brush.verticalGradient(listOf(c.primary, c.primary.copy(alpha = .75f))) else Brush.verticalGradient(listOf(c.surface, c.surface)))
                    .clickable { onTabSelected(index) }, contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(tab.icon, tab.label, Modifier.size(18.dp), tint = if (selected) c.onPrimary else c.onSurfaceVariant)
                        if (selected) { Spacer(Modifier.width(6.dp)); Text(tab.label, style = MaterialTheme.typography.labelLarge, color = c.onPrimary, maxLines = 1) }
                    }
                }
            }
        }
    }
}

@Composable internal fun IpodMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(Color(0xFF0E1620)).clickable(onClick = onExpand).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(artwork, null, Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(song.title, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, color = Color(0xFFB7C5D6), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box(Modifier.fillMaxWidth().padding(top = 5.dp).height(4.dp).clip(CircleShape).background(Color(0xFF263447))) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Color(0xFF9CC3F5))) }
                }
            }
            // A small click wheel: the centre button plays and pauses, the ring's right side skips.
            // Both are drawn full size so neither glyph gets lost.
            Box(Modifier.padding(start = 8.dp).size(60.dp).shadow(3.dp, CircleShape).clip(CircleShape)
                .background(Brush.verticalGradient(listOf(c.surface, c.surfaceVariant))).border(1.dp, c.outlineVariant, CircleShape)) {
                Box(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(20.dp).clickable(onClick = onNext), contentAlignment = Alignment.Center) {
                    Icon(SkinIcons.SkipNext, "Next", Modifier.size(16.dp), tint = c.onSurface)
                }
                Surface(onClick = onPlayPause, shape = CircleShape, color = c.primary, modifier = Modifier.align(Alignment.Center).offset(x = (-4).dp).size(32.dp), shadowElevation = 2.dp) {
                    Box(contentAlignment = Alignment.Center) {
                        if (loading) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = c.onPrimary)
                        else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, if (playing) "Pause" else "Play", Modifier.size(18.dp), tint = c.onPrimary)
                    }
                }
            }
        }
    }
}
