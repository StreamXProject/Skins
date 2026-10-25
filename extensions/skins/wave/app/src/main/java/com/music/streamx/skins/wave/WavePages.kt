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

/** A console log of the song: each line prefixed with its timecode, the current one on the prompt. */
@Composable internal fun WaveLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(c.primary.copy(alpha = .35f), c.surfaceVariant))).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("lyrics.txt — ${model.song.title}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (lines.isNullOrEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (unavailable) "> lyrics: not found (404)" else "> fetching lyrics…", style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.onSurfaceVariant) }; return@Column }
            LyricsScroller(lines, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(12.dp), spacing = 6.dp) { _, line, state ->
                Row(Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }.background(if (state == LineState.ACTIVE) c.primary.copy(alpha = .14f) else Color.Transparent).padding(horizontal = 6.dp, vertical = 3.dp)) {
                    Text("[${clock(line.timeMs)}]", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono), color = if (state == LineState.ACTIVE) c.primary else c.onSurfaceVariant.copy(alpha = .6f))
                    Text((if (state == LineState.ACTIVE) " > " else "   ") + line.text.ifBlank { "..." }, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono),
                        color = when (state) { LineState.ACTIVE -> c.primary; LineState.SUNG -> c.onSurfaceVariant.copy(alpha = .5f); else -> c.onSurface })
                }
            }
        }
    }
}

/** The playlist editor window: numbered monospace entries, the playing one highlighted. */
@Composable internal fun WaveQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, contentPadding = PaddingValues(vertical = 6.dp), header = { requestClear ->
            Column {
                Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(c.primary.copy(alpha = .35f), c.surfaceVariant))).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("playlist editor — ${model.queue.size} items", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("AUTO ${if (autoplay) "ON" else "OFF"}" to onAutoplay, "CLR LIST" to requestClear).forEach { (label, action) ->
                        Surface(onClick = action, shape = RoundedCornerShape(4.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) { Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono)) }
                    }
                }
            }
        }) { _, index, song, current, actions ->
            Row(Modifier.fillMaxWidth().clickable(onClick = actions.jump).background(if (current) c.primary.copy(alpha = .16f) else Color.Transparent).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${index + 1}.".padStart(4), Modifier.width(40.dp), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono), color = if (current) c.primary else c.onSurfaceVariant)
                Text("${song.artist} - ${song.title}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono), color = if (current) c.primary else c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.durationText ?: "", style = MaterialTheme.typography.bodySmall.copy(fontFamily = mono), color = c.onSurfaceVariant)
                if (!current) IconButton(actions.remove, Modifier.size(28.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(14.dp), tint = c.onSurfaceVariant) }
            }
        }
    }
}

@Composable internal fun WaveLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 6.dp), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(c.primary.copy(alpha = .35f), c.surfaceVariant))).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("WAVE — media library", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono))
                listOf(c.outlineVariant, c.outlineVariant, c.primary).forEach { Box(Modifier.padding(start = 4.dp).size(10.dp).border(1.dp, c.onSurfaceVariant, RoundedCornerShape(2.dp)).background(it)) }
            }
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("> ls ~/music", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.primary)
                onNewPlaylist?.let { Surface(onClick = it, shape = RoundedCornerShape(4.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) { Text("+ NEW .M3U", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono)) } }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun WaveLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(">", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.primary)
        AsyncImage(item.thumbnailUrl, null, Modifier.padding(start = 10.dp).size(40.dp).clip(RoundedCornerShape(3.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("/", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant)
    }
}
