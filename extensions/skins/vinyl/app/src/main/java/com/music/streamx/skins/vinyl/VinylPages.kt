package com.music.streamx.skins.vinyl

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

/** Liner notes: a serif column with a rule down the left, the sung line marked by a running needle. */
@Composable internal fun VinylLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = 24.dp)) {
        Text("LINER NOTES", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp), color = c.primary)
        Text(model.song.albumName ?: model.song.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Serif), fontStyle = FontStyle.Italic), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = c.outlineVariant)
        if (LyricsEmpty(lines, unavailable)) return
        LyricsScroller(lines!!, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(vertical = 8.dp), spacing = 14.dp) { _, line, state ->
            Row(Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }) {
                Box(Modifier.width(14.dp).padding(top = 8.dp), contentAlignment = Alignment.TopCenter) {
                    if (state == LineState.ACTIVE) Box(Modifier.size(8.dp).clip(CircleShape).background(c.primary)) else Box(Modifier.width(1.dp).height(28.dp).background(c.outlineVariant))
                }
                Text(line.text.ifBlank { "♪" }, Modifier.weight(1f).padding(start = 14.dp), style = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Serif)),
                    color = when (state) { LineState.ACTIVE -> c.onBackground; LineState.SUNG -> c.onSurfaceVariant.copy(alpha = .5f); else -> c.onSurfaceVariant })
            }
        }
    }
}

/** The back of the sleeve: a numbered tracklist with sides, the current track marked with a spinning disc. */
@Composable internal fun VinylQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val spin = rememberSpin(model.isPlaying, model.reduceMotion, rpm = 33.3f)
    QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, modifier.padding(horizontal = 24.dp), header = { requestClear ->
        Column(Modifier.padding(bottom = 6.dp)) {
            Text("SIDE A", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp), color = c.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tracklist", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = skinFont(FontFamily.Serif)))
                TextButton(onAutoplay) { Text(if (autoplay) "Auto-flip on" else "Auto-flip off", style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Serif))) }
                IconButton(requestClear) { Icon(SkinIcons.DeleteSweep, "Clear queue", tint = c.onSurfaceVariant) }
            }
            HorizontalDivider(color = c.onSurface)
        }
    }) { offset, _, song, current, actions ->
        Row(Modifier.fillMaxWidth().clickable(onClick = actions.jump).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (current) Box(Modifier.size(28.dp).spin(spin).clip(CircleShape).background(Color(0xFF151312)), contentAlignment = Alignment.Center) { Box(Modifier.size(9.dp).clip(CircleShape).background(c.primary)) }
            else Text("A${offset + 1}", Modifier.width(28.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Serif)), color = c.onSurfaceVariant)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Serif)), color = if (current) c.primary else c.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(song.durationText ?: "", style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Serif)), color = c.onSurfaceVariant)
            if (!current) IconButton(actions.remove, Modifier.size(32.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(16.dp), tint = c.onSurfaceVariant) }
        }
        HorizontalDivider(color = c.outlineVariant.copy(alpha = .6f))
    }
}

@Composable internal fun VinylLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 6.dp)) {
        Text("THE COLLECTION", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp), color = c.primary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Shelved", Modifier.weight(1f), style = MaterialTheme.typography.displaySmall.copy(fontFamily = skinFont(FontFamily.Serif)))
            onNewPlaylist?.let { Surface(onClick = it, shape = CircleShape, color = c.primary, contentColor = c.onPrimary, modifier = Modifier.size(44.dp)) { Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Add, "New playlist") } } }
        }
        HorizontalDivider(Modifier.padding(top = 10.dp), color = c.onSurface)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun VinylLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        // A sleeve with the record peeking out.
        Box(Modifier.size(64.dp, 52.dp)) {
            Box(Modifier.align(Alignment.CenterEnd).size(48.dp).clip(CircleShape).background(Color(0xFF151312)), contentAlignment = Alignment.Center) { Box(Modifier.size(14.dp).clip(CircleShape).background(c.primary)) }
            AsyncImage(item.thumbnailUrl, null, Modifier.align(Alignment.CenterStart).size(50.dp).clip(RoundedCornerShape(3.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Serif)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
