package com.music.streamx.skins.paper

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

/** A printed poem: centred serif, a drop rule above, the current line set in roman and the rest in grey italics. */
@Composable internal fun PaperLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val serif = skinFont(FontFamily.Serif)
    Column(modifier.padding(horizontal = 28.dp)) {
        Text(model.song.title.uppercase(), Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelLarge.copy(fontFamily = serif, letterSpacing = 3.sp), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).width(40.dp).height(1.dp).background(c.onSurface))
        if (LyricsEmpty(lines, unavailable, message = "This one was never written down.", searching = "Setting the type…")) return
        LyricsScroller(lines!!, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(vertical = 20.dp), spacing = 12.dp) { _, line, state ->
            Text(line.text.ifBlank { "⁂" }, Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) },
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = serif, fontStyle = if (state == LineState.ACTIVE) FontStyle.Normal else FontStyle.Italic),
                color = when (state) { LineState.ACTIVE -> c.onBackground; LineState.SUNG -> c.onSurfaceVariant.copy(alpha = .45f); else -> c.onSurfaceVariant }, textAlign = TextAlign.Center)
        }
    }
}

/** A table of contents: entries with dotted leaders out to their running time. */
@Composable internal fun PaperQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val serif = skinFont(FontFamily.Serif)
    QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, modifier.padding(horizontal = 28.dp), header = { requestClear ->
        Column {
            Text("CONTENTS", Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelLarge.copy(fontFamily = serif, letterSpacing = 4.sp), textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (autoplay) "Continue reading: on" else "Continue reading: off", Modifier.clickable(onClick = onAutoplay), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif, fontStyle = FontStyle.Italic), color = c.onSurfaceVariant)
                Text("Clear", Modifier.clickable(onClick = requestClear), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), color = c.onSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(top = 8.dp), color = c.onSurface)
        }
    }) { _, index, song, current, actions ->
        Row(Modifier.fillMaxWidth().clickable(onClick = actions.jump).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(toRoman(index + 1), Modifier.width(40.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = serif), color = if (current) c.onBackground else c.onSurfaceVariant)
            Text(song.title, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = serif, fontWeight = if (current) FontWeight.Bold else FontWeight.Normal), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            Text(" " + ".".repeat(60), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = serif), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Clip)
            Text(song.durationText ?: "—", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = serif), color = c.onSurfaceVariant)
            if (!current) IconButton(actions.remove, Modifier.size(28.dp)) { Icon(SkinIcons.Close, "Remove", Modifier.size(14.dp), tint = c.onSurfaceVariant) }
        }
    }
}

private fun toRoman(n: Int): String {
    val values = listOf(50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
    var rest = n.coerceIn(1, 99); return buildString { for ((v, s) in values) while (rest >= v) { append(s); rest -= v } }
}

@Composable internal fun PaperLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    val serif = skinFont(FontFamily.Serif)
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 12.dp, bottom = 8.dp)) {
        Text("THE ARCHIVE", Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = serif, letterSpacing = 4.sp), textAlign = TextAlign.Center)
        Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(2.dp).background(c.onSurface)); Box(Modifier.fillMaxWidth().padding(top = 2.dp).height(1.dp).background(c.onSurface))
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Collected volumes", style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif, fontStyle = FontStyle.Italic), color = c.onSurfaceVariant)
            onNewPlaylist?.let { Text("Begin a new volume", Modifier.clickable(onClick = it), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), color = c.onSurface) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PaperLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(item.thumbnailUrl, null, Modifier.size(46.dp).border(1.dp, c.onSurface).padding(3.dp), contentScale = ContentScale.Crop, colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Serif)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall.copy(fontFamily = skinFont(FontFamily.Serif), fontStyle = FontStyle.Italic), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("→", style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Serif)), color = c.onSurfaceVariant)
    }
    HorizontalDivider(Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = c.outlineVariant)
}
