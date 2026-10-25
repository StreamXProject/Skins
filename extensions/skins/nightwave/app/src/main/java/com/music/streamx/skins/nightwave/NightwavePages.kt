package com.music.streamx.skins.nightwave

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

/** Karaoke in the dark: big centred lines, the current one glowing in the artwork's colour. */
@Composable internal fun NightwaveLyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val seed = com.music.streamx.ui.skins.rememberSkinArtworkColor(model.artwork) ?: c.primary
    if (LyricsEmpty(lines, unavailable, modifier)) return
    LyricsScroller(lines!!, model.position.positionMs, offsetMs, model.reduceMotion, modifier, contentPadding = PaddingValues(horizontal = 28.dp, vertical = 60.dp), spacing = 26.dp, centerOffsetPx = -320) { _, line, state ->
        val scale by animateFloatAsState(if (state == LineState.ACTIVE) 1f else .86f, tween(if (model.reduceMotion) 0 else 320), label = "scale")
        Text(line.text.ifBlank { "•  •  •" }, Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }.graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (state == LineState.UPCOMING) .55f else if (state == LineState.SUNG) .3f else 1f },
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, shadow = if (state == LineState.ACTIVE) androidx.compose.ui.graphics.Shadow(seed, blurRadius = 28f) else null),
            color = if (state == LineState.ACTIVE) c.onBackground else c.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** Up next as large cards on black; the current track's card glows. */
@Composable internal fun NightwaveQueue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val seed = com.music.streamx.ui.skins.rememberSkinArtworkColor(model.artwork) ?: c.primary
    QueueList(model.queue, model.queueIndex, model.locked, onJump, onRemove, onMove, onClear, onPlayPause, modifier, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), header = { requestClear ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Up next", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
            Surface(onClick = onAutoplay, shape = CircleShape, color = if (autoplay) seed.copy(alpha = .22f) else c.surfaceVariant, contentColor = if (autoplay) seed else c.onSurfaceVariant) { Text("Autoplay", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium) }
            IconButton(requestClear) { Icon(SkinIcons.ClearAll, "Clear", tint = c.onSurfaceVariant) }
        }
    }) { _, _, song, current, actions ->
        Surface(onClick = actions.jump, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(22.dp),
            color = if (current) c.surfaceVariant else c.surface, border = BorderStroke(1.dp, if (current) seed.copy(alpha = .5f) else c.surfaceVariant)) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(song.thumbnailUrl, null, Modifier.size(64.dp).then(if (current) Modifier.shadow(16.dp, RoundedCornerShape(16.dp), spotColor = seed) else Modifier).clip(RoundedCornerShape(16.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleMedium, color = if (current) seed else c.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (current) Icon(if (model.isPlaying) SkinIcons.GraphicEq else SkinIcons.PlayArrow, null, tint = seed)
                else IconButton(actions.remove) { Icon(SkinIcons.Close, "Remove", Modifier.size(18.dp), tint = c.onSurfaceVariant) }
            }
        }
    }
}

@Composable internal fun NightwaveLibraryHeading(onNewPlaylist: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth()) {
        Canvas(Modifier.matchParentSize()) { drawCircle(Brush.radialGradient(listOf(c.primary.copy(alpha = .25f), Color.Transparent), center = Offset(size.width, 0f), radius = size.width * .6f), size.width * .6f, Offset(size.width, 0f)) }
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Your library", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Everything you kept.", style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
            }
            onNewPlaylist?.let { Surface(onClick = it, shape = CircleShape, color = c.primary.copy(alpha = .2f), contentColor = c.primary, modifier = Modifier.size(48.dp), border = BorderStroke(1.dp, c.primary.copy(alpha = .4f))) { Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Add, "New playlist") } } }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun NightwaveLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 5.dp).clip(RoundedCornerShape(22.dp)).background(c.surface).combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(item.thumbnailUrl, null, Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(6.dp).clip(CircleShape).background(c.primary))
    }
}
