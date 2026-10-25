package com.music.streamx.skins.studio

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

/** A cue sheet: timecodes down the left in monospace, the live cue lit amber. */
@Composable internal fun StudioLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Surface(modifier.padding(horizontal = 20.dp, vertical = 4.dp), shape = RoundedCornerShape(10.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("CUE SHEET", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono, letterSpacing = 2.sp), color = c.primary)
                Text("TC ${clock(model.position.positionMs)}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.onSurfaceVariant)
            }
            HorizontalDivider(color = c.outlineVariant)
            if (lines.isNullOrEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (unavailable) "NO CUES ON THIS TAKE" else "LOADING CUES…", style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.onSurfaceVariant) }; return@Column }
            LyricsScroller(lines, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(vertical = 8.dp), spacing = 0.dp) { index, line, state ->
                Row(Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }.background(if (state == LineState.ACTIVE) c.primary.copy(alpha = .12f) else Color.Transparent).padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (state == LineState.ACTIVE) c.primary else c.outlineVariant))
                    Text(clock(line.timeMs), Modifier.padding(start = 10.dp).width(44.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono), color = if (state == LineState.ACTIVE) c.primary else c.onSurfaceVariant)
                    Text(line.text.ifBlank { "(instrumental)" }, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = when (state) { LineState.ACTIVE -> c.onBackground; LineState.SUNG -> c.onSurfaceVariant.copy(alpha = .5f); else -> c.onSurfaceVariant })
                    Text((index + 1).toString().padStart(3, '0'), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = c.outlineVariant)
                }
            }
        }
    }
}

/** The channel list: every track on its own strip, the live one with its LED on and its meter moving. */
@Composable internal fun StudioQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, modifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), header = { requestClear ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("CHANNELS  ${model.queue.size.toString().padStart(2, '0')}", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono, letterSpacing = 2.sp), color = c.primary)
            Surface(onClick = onAutoplay, shape = RoundedCornerShape(6.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).clip(CircleShape).background(if (autoplay) c.primary else c.outlineVariant)); Spacer(Modifier.width(8.dp)); Text("AUTO", style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono)) }
            }
            Spacer(Modifier.width(6.dp))
            Surface(onClick = requestClear, shape = RoundedCornerShape(6.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) { Text("CLR", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono)) }
        }
    }) { _, index, song, current, actions ->
        Surface(onClick = actions.jump, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, if (current) c.primary.copy(alpha = .6f) else c.outlineVariant)) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (current) c.primary else c.outlineVariant))
                    Text("CH${(index + 1).toString().padStart(2, '0')}", Modifier.padding(top = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = c.onSurfaceVariant)
                }
                AsyncImage(song.thumbnailUrl, null, Modifier.padding(start = 10.dp).size(44.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall, color = if (current) c.primary else c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (current) StereoMeter(model.isPlaying, c.primary, Color(0xFFE5533D), Modifier.size(18.dp, 36.dp), segments = 10)
                else IconButton(actions.remove, Modifier.size(32.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(16.dp), tint = c.onSurfaceVariant) }
            }
        }
    }
}

@Composable internal fun StudioLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 6.dp), shape = RoundedCornerShape(10.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Box {
            listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd).forEach { Box(Modifier.align(it).padding(6.dp).size(7.dp).clip(CircleShape).background(c.outlineVariant)) }
            Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TAPE VAULT", style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace), letterSpacing = 3.sp), color = c.primary)
                    Text("Masters & mixes", style = MaterialTheme.typography.titleLarge)
                }
                onNewPlaylist?.let { Surface(onClick = it, shape = RoundedCornerShape(6.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) { Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(6.dp).clip(CircleShape).background(c.primary)); Spacer(Modifier.width(8.dp)); Text("NEW REEL", style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace))) } } }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun StudioLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp).clip(RoundedCornerShape(8.dp)).background(c.surface).border(1.dp, c.outlineVariant, RoundedCornerShape(8.dp))
        .combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(c.outlineVariant))
        AsyncImage(item.thumbnailUrl, null, Modifier.padding(start = 10.dp).size(46.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.width(22.dp).height(4.dp).clip(CircleShape).background(c.surfaceVariant)) { Box(Modifier.fillMaxWidth(.6f).fillMaxHeight().background(c.primary.copy(alpha = .7f))) }
    }
}
