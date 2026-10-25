package com.music.streamx.skins.wave

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

@Composable internal fun WaveHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 10.dp)) {
        // A window: title bar with three buttons, a toolbar underneath.
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
            Column {
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(c.primary.copy(alpha = .35f), c.surfaceVariant))).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("WAVE — library.m3u", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    listOf(c.outlineVariant, c.outlineVariant, c.primary).forEach { Box(Modifier.padding(start = 4.dp).size(10.dp).border(1.dp, c.onSurfaceVariant, RoundedCornerShape(2.dp)).background(it)) }
                }
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = onAccount, shape = RoundedCornerShape(4.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant), modifier = Modifier.size(40.dp)) {
                        if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account", Modifier.size(18.dp), tint = c.primary) }
                    }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("> user: ${account?.name ?: "guest"}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("> ready. it really whips.", style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = c.onSurfaceVariant)
                    }
                }
                Row(Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("HIST" to onHistory, "LOCAL" to onLocal, "XPLR" to onExplore).forEach { (label, action) ->
                        Surface(onClick = action, modifier = Modifier.weight(1f).height(32.dp), shape = RoundedCornerShape(4.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                            Box(contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono), color = c.onSurface) }
                        }
                    }
                }
            }
        }
    }
}

@Composable internal fun WaveBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    SkinDock(shape = RoundedCornerShape(6.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Surface(onClick = { onTabSelected(index) }, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(4.dp),
                    color = if (selected) c.primary.copy(alpha = .18f) else c.surfaceVariant, contentColor = if (selected) c.primary else c.onSurfaceVariant,
                    border = BorderStroke(1.dp, if (selected) c.primary else c.outlineVariant)) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(tab.icon, tab.label, Modifier.size(18.dp))
                        Text(tab.label.lowercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable internal fun WaveMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Row(Modifier.clickable(onClick = onExpand).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(artwork, null, Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text("${song.title} — ${song.artist}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    LevelBars(playing, false, c.primary, Modifier.fillMaxWidth().height(16.dp).padding(top = 4.dp), bars = 28, gap = 2.dp, rounded = false)
                }
                IconButton(onPlayPause) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, null, tint = c.primary) }
                IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next") }
            }
            Box(Modifier.fillMaxWidth().height(4.dp).background(c.surfaceVariant)) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(c.primary)) }
        }
    }
}
