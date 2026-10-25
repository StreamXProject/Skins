package com.music.streamx.skins.cassette

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

/** The J-card insert: lyrics in the owner's hand on lined card stock. */
@Composable internal fun CassetteLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(6.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Box(Modifier.fillMaxWidth().height(8.dp).background(c.primary))
            if (LyricsEmpty(lines, unavailable)) return@Column
            LyricsScroller(lines!!, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp), spacing = 0.dp) { _, line, state ->
                Column(Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }) {
                    Text(line.text.ifBlank { "~" }, Modifier.padding(top = 10.dp, bottom = 2.dp), style = MaterialTheme.typography.titleLarge.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic),
                        color = when (state) { LineState.ACTIVE -> c.primary; LineState.SUNG -> c.onSurfaceVariant.copy(alpha = .5f); else -> c.onSurface })
                    Box(Modifier.fillMaxWidth().height(1.dp).background(c.outlineVariant))
                }
            }
        }
    }
}

/** A tape index: a three-digit counter per entry and the side that holds it. */
@Composable internal fun CassetteQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(6.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, header = { requestClear ->
            Row(Modifier.fillMaxWidth().background(c.primary.copy(alpha = .14f)).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("TAPE INDEX", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp), color = c.onSurface)
                Text(if (autoplay) "AUTO-REVERSE ●" else "AUTO-REVERSE ○", Modifier.clickable(onClick = onAutoplay), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = c.primary)
                IconButton(requestClear, Modifier.size(32.dp)) { Icon(SkinIcons.DeleteSweep, "Clear", Modifier.size(18.dp), tint = c.onSurfaceVariant) }
            }
        }) { offset, index, song, current, actions ->
            Row(Modifier.fillMaxWidth().clickable(onClick = actions.jump).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text((index * 37 % 1000).toString().padStart(3, '0'), Modifier.width(40.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = if (current) c.primary else c.onSurfaceVariant)
                Text(if (offset % 2 == 0) "A" else "B", Modifier.width(18.dp), style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant, fontWeight = FontWeight.Black)
                Column(Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), color = if (current) c.primary else c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (current) Icon(if (model.isPlaying) SkinIcons.PlayArrow else SkinIcons.Pause, null, Modifier.size(18.dp), tint = c.primary)
                else IconButton(actions.remove, Modifier.size(32.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(16.dp), tint = c.onSurfaceVariant) }
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(c.outlineVariant))
        }
    }
}

@Composable internal fun CassetteLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 8.dp), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Box(Modifier.fillMaxWidth().height(10.dp).background(c.primary))
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SHOEBOX", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp), color = c.onSurfaceVariant)
                    Text("your tapes", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic))
                }
                onNewPlaylist?.let { Surface(onClick = it, shape = RoundedCornerShape(8.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant), shadowElevation = 2.dp) { Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(SkinIcons.Add, null, Modifier.size(16.dp), tint = c.primary); Spacer(Modifier.width(6.dp)); Text("Blank tape", style = MaterialTheme.typography.labelLarge) } } }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun CassetteLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    // A spine in the shoebox: label strip, two hubs, the title written along it.
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).clip(RoundedCornerShape(6.dp)).background(c.surfaceVariant).border(1.dp, c.outlineVariant, RoundedCornerShape(6.dp))
        .combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(item.thumbnailUrl, null, Modifier.size(42.dp).clip(RoundedCornerShape(4.dp)).background(c.surface), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp).clip(RoundedCornerShape(4.dp)).background(c.primary.copy(alpha = .12f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        repeat(2) { Box(Modifier.padding(start = 4.dp).size(14.dp).clip(CircleShape).background(Color(0xFF1A1715)), contentAlignment = Alignment.Center) { Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEDE4D8))) } }
    }
}
