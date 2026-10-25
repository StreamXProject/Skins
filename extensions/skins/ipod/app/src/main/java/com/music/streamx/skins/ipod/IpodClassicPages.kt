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

/** Lyrics on the LCD: a dark screen, the sung line carried in the menu highlight band. */
@Composable internal fun IpodLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 24.dp, vertical = 4.dp), shape = RoundedCornerShape(14.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
        Box(Modifier.padding(6.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF0E1620))) {
            Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF1B2634), Color(0xFF0E1620)))).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Lyrics", Modifier.weight(1f), color = Color(0xFFDCE6F2), style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
            if (LyricsEmpty(lines, unavailable, Modifier.padding(top = 40.dp))) return@Box
            LyricsScroller(lines!!, model.position.positionMs, offsetMs, model.reduceMotion, Modifier.padding(top = 36.dp), contentPadding = PaddingValues(vertical = 12.dp), spacing = 0.dp) { _, line, state ->
                Row(Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }
                    .background(if (state == LineState.ACTIVE) Brush.verticalGradient(listOf(c.primary, c.primary.copy(alpha = .75f))) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)))
                    .padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text(line.text.ifBlank { "♪" }, Modifier.weight(1f), color = when (state) { LineState.ACTIVE -> Color.White; LineState.SUNG -> Color(0xFF6E7F93); else -> Color(0xFFDCE6F2) }, style = MaterialTheme.typography.bodyLarge)
                    if (state == LineState.ACTIVE) Icon(SkinIcons.ChevronRight, null, Modifier.size(18.dp), tint = Color.White)
                }
            }
        }
    }
}

/** The queue as an iPod menu: a highlight band, numbers down the side, chevrons. */
@Composable internal fun IpodQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 24.dp, vertical = 4.dp), shape = RoundedCornerShape(14.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
        Box(Modifier.padding(6.dp).clip(RoundedCornerShape(10.dp)).background(c.surface)) {
            QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, header = { requestClear ->
                Row(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(c.surfaceVariant, c.surface))).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Now Playing", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(if (autoplay) "Autoplay on" else "Autoplay off", Modifier.clickable(onClick = onAutoplay), style = MaterialTheme.typography.labelSmall, color = c.primary)
                    Spacer(Modifier.width(10.dp)); Text("Clear", Modifier.clickable(onClick = requestClear), style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant)
                }
            }) { offset, _, song, current, actions ->
                Row(Modifier.fillMaxWidth().clickable(onClick = actions.jump)
                    .background(if (current) Brush.verticalGradient(listOf(c.primary, c.primary.copy(alpha = .75f))) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)))
                    .padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (current) (if (model.isPlaying) "▶" else "❚❚") else "${offset}", Modifier.width(26.dp), style = MaterialTheme.typography.labelMedium, color = if (current) c.onPrimary else c.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text(song.title, style = MaterialTheme.typography.bodyLarge, color = if (current) c.onPrimary else c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = if (current) c.onPrimary.copy(alpha = .8f) else c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (!current) { IconButton(actions.remove, Modifier.size(32.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(16.dp), tint = c.onSurfaceVariant) } }
                    Icon(SkinIcons.ChevronRight, null, Modifier.size(18.dp), tint = if (current) c.onPrimary else c.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable internal fun IpodLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp).clip(RoundedCornerShape(10.dp)).background(Brush.verticalGradient(listOf(c.surfaceVariant, c.surface))).border(1.dp, c.outlineVariant, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(SkinIcons.ChevronLeft, null, Modifier.size(16.dp), tint = c.onSurfaceVariant)
        Text("Music", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        onNewPlaylist?.let { IconButton(it, Modifier.size(28.dp)) { Icon(SkinIcons.Add, "New playlist", Modifier.size(18.dp), tint = c.primary) } }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun IpodLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(item.thumbnailUrl, null, Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(SkinIcons.ChevronRight, null, Modifier.size(18.dp), tint = c.onSurfaceVariant)
    }
    HorizontalDivider(Modifier.padding(start = 72.dp, end = 20.dp), thickness = 0.5.dp, color = c.outlineVariant)
}
