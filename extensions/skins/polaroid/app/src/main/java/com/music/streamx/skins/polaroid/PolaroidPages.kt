package com.music.streamx.skins.polaroid

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

/** Handwritten on a lined notepad, the current line underlined in the sticker colour. */
@Composable internal fun PolaroidLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    // A full page of paper glares at night, so on a dark page the notepad is dark too: same ruled lines, light ink.
    val dark = c.background.luminance() < .4f
    val pad = if (dark) c.surfaceVariant else printPaper()
    val ink = if (dark) c.onSurface else Color(0xFF2E2A26)
    val faded = if (dark) c.onSurfaceVariant.copy(alpha = .45f) else Color(0xFFB5AFA6)
    val pencil = if (dark) c.onSurfaceVariant else Color(0xFF6B655E)
    val rule = if (dark) c.outline.copy(alpha = .22f) else Color(0xFFCFE3F2)
    Box(modifier.padding(horizontal = 22.dp, vertical = 4.dp)) {
        Column(Modifier.fillMaxSize().rotate(-.6f).shadow(10.dp, RoundedCornerShape(4.dp)).background(pad, RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxWidth().height(22.dp).background(c.secondary.copy(alpha = if (dark) .35f else .55f)))
            if (lines.isNullOrEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (unavailable) "no words for this one :(" else "writing it down…", style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Cursive)), color = pencil) }; return@Column }
            LyricsScroller(lines, model.position.positionMs, offsetMs, model.reduceMotion, contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp), spacing = 0.dp) { _, line, state ->
                Column(Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }) {
                    Text(line.text.ifBlank { "♡" }, Modifier.padding(top = 12.dp, bottom = 2.dp), style = MaterialTheme.typography.titleLarge.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic),
                        color = when (state) { LineState.ACTIVE -> ink; LineState.SUNG -> faded; else -> pencil })
                    Box(Modifier.fillMaxWidth().height(if (state == LineState.ACTIVE) 2.dp else 1.dp).background(if (state == LineState.ACTIVE) c.primary else rule))
                }
            }
        }
    }
}

/** A photo strip: the queue as small prints with captions, the current one upright and the rest leaning. */
@Composable internal fun PolaroidQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, modifier, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp), header = { requestClear ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("coming up ✎", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic))
            Surface(onClick = onAutoplay, shape = CircleShape, color = if (autoplay) c.primary.copy(alpha = .25f) else c.surfaceVariant) { Text("keep going", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Cursive))) }
            IconButton(requestClear) { Icon(SkinIcons.DeleteSweep, "Clear", tint = c.onSurfaceVariant) }
        }
    }) { offset, _, song, current, actions ->
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.rotate(if (current) 0f else if (offset % 2 == 0) -3f else 3f).shadow(5.dp, RoundedCornerShape(2.dp)).background(printPaper(), RoundedCornerShape(2.dp)).clickable(onClick = actions.jump).padding(4.dp, 4.dp, 4.dp, 10.dp)) {
                AsyncImage(song.thumbnailUrl, null, Modifier.size(52.dp).background(Color(0xFFE9E4DB)), contentScale = ContentScale.Crop)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp).clickable(onClick = actions.jump)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), color = if (current) c.primary else c.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (current) Box(Modifier.rotate(-8f).size(44.dp, 16.dp).background(c.secondary.copy(alpha = .7f), RoundedCornerShape(2.dp)))
            else IconButton(actions.remove) { Icon(SkinIcons.Close, "Remove", Modifier.size(18.dp), tint = c.onSurfaceVariant) }
        }
    }
}

@Composable internal fun PolaroidLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 12.dp, bottom = 8.dp)) {
        Surface(Modifier.fillMaxWidth().padding(top = 8.dp).rotate(1f), shape = RoundedCornerShape(3.dp), color = printPaper(), shadowElevation = 6.dp) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("the album", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), color = Color(0xFF2E2A26))
                    Text("every print you kept", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = skinFont(FontFamily.Cursive)), color = Color(0xFF6B655E))
                }
                onNewPlaylist?.let { Surface(onClick = it, shape = CircleShape, color = c.primary, modifier = Modifier.size(42.dp)) { Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Add, "New playlist", tint = c.onPrimary) } } }
            }
        }
        Box(Modifier.align(Alignment.TopCenter).rotate(4f).size(70.dp, 20.dp).background(c.secondary.copy(alpha = .7f), RoundedCornerShape(2.dp)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PolaroidLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 22.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.rotate(-2f).shadow(4.dp, RoundedCornerShape(2.dp)).background(printPaper(), RoundedCornerShape(2.dp)).padding(3.dp, 3.dp, 3.dp, 8.dp)) {
            AsyncImage(item.thumbnailUrl, null, Modifier.size(42.dp).background(Color(0xFFE9E4DB)), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(10.dp).clip(CircleShape).background(listOf(c.primary, c.secondary, Color(0xFFCFE3F2))[item.title.length % 3]))
    }
}
