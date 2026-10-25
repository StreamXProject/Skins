package com.music.streamx.skins.discman

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

/** Lyrics on the segment display: green monospace, the current line bright and the rest dimmed like unlit segments. */
@Composable internal fun DiscmanLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFF17201B), border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text("LYRIC  TRK ${(model.queueIndex + 1).toString().padStart(2, '0')}", Modifier.weight(1f), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono))
                Text(clock(model.position.positionMs), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono))
            }
            if (lines.isNullOrEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (unavailable) "NO LYRIC DATA" else "READING…", color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono)) }; return@Column }
            LyricsScroller(lines, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), spacing = 12.dp) { _, line, state ->
                Text(line.text.ifBlank { "- - -" }.uppercase(), Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }, style = MaterialTheme.typography.titleMedium.copy(fontFamily = mono),
                    color = when (state) { LineState.ACTIVE -> Color(0xFF9FE1B8); LineState.SUNG -> Color(0xFF2E4A39); else -> Color(0xFF5E9A74) }, textAlign = TextAlign.Center)
            }
        }
    }
}

/** The disc's table of contents: TRK numbers, lengths, and a PROGRAM row to clear or reorder. */
@Composable internal fun DiscmanQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFF17201B), border = BorderStroke(1.dp, c.outlineVariant)) {
        QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, header = { requestClear ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("PROGRAM  ${model.queue.size.toString().padStart(2, '0')} TRK", Modifier.weight(1f), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono))
                Text(if (autoplay) "CONT" else "1DISC", Modifier.clickable(onClick = onAutoplay).border(1.dp, Color(0xFF5E9A74), RoundedCornerShape(3.dp)).padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono))
                Spacer(Modifier.width(8.dp))
                Text("CLR", Modifier.clickable(onClick = requestClear).border(1.dp, Color(0xFF5E9A74), RoundedCornerShape(3.dp)).padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono))
            }
        }) { _, index, song, current, actions ->
            Row(Modifier.fillMaxWidth().clickable(onClick = actions.jump).background(if (current) Color(0xFF1F2E25) else Color.Transparent).padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text((if (current) "▶" else " ") + (index + 1).toString().padStart(2, '0'), Modifier.width(44.dp), color = if (current) Color(0xFF9FE1B8) else Color(0xFF5E9A74), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = if (current) Color(0xFF9FE1B8) else Color(0xFFBFE6CC), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(song.durationText ?: "--:--", color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono))
                if (!current) IconButton(actions.remove, Modifier.size(30.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(14.dp), tint = Color(0xFF5E9A74)) }
            }
        }
    }
}

@Composable internal fun DiscmanLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 8.dp), shape = RoundedCornerShape(10.dp), color = Color(0xFF17201B), border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("DISC CHANGER", color = Color(0xFF9FE1B8), style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Monospace)))
                Text("SELECT A DISC", color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)))
            }
            onNewPlaylist?.let { Text("+ NEW", Modifier.clickable(onClick = it).border(1.dp, Color(0xFF5E9A74), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace))) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun DiscmanLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 20.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) { drawCircle(Brush.sweepGradient(listOf(Color(0xFFE9E4F5), Color(0xFFCFE9F7), Color(0xFFDDF2E1), Color(0xFFF9EBD5), Color(0xFFE9E4F5))), size.minDimension / 2) }
            AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxSize(.64f).clip(CircleShape).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize(.16f).clip(CircleShape).background(c.background))
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(SkinIcons.Eject, null, Modifier.size(18.dp), tint = c.onSurfaceVariant)
    }
}
