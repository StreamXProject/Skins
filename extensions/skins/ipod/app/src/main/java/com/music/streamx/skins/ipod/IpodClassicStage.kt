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

/**
 * A click wheel. The screen above shows the track; the wheel below is the
 * whole transport: MENU opens options, the sides skip, the bottom and the
 * centre play/pause, and running a finger around the ring scrubs.
 */
@Composable
internal fun IpodStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Column(modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // The screen: a bezelled panel, always dark like the real LCD backlight.
        Surface(Modifier.fillMaxWidth().weight(1f).padding(top = 4.dp, bottom = 18.dp), shape = RoundedCornerShape(14.dp),
            color = c.surfaceVariant, border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant)) {
            Column(Modifier.padding(6.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF0E1620)).padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Now Playing", color = Color(0xFFDCE6F2), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${model.queueIndex + 1} of ${model.queue.size}", color = Color(0xFF8FA3BA), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(8.dp))
                    Icon(if (model.isPlaying) SkinIcons.PlayArrow else SkinIcons.Pause, null, Modifier.size(14.dp), tint = Color(0xFF8FA3BA))
                }
                Row(Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model.artwork, null, Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(Color(0xFF1B2634)), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1.2f).padding(start = 14.dp)) {
                        Text(model.song.title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(model.song.artist, color = Color(0xFFB7C5D6), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                        model.song.albumName?.let { Text(it, color = Color(0xFF8FA3BA), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        Row(Modifier.padding(top = 8.dp)) {
                            if (model.liked) Icon(SkinIcons.Favorite, null, Modifier.size(14.dp), tint = Color(0xFFFF6B8B))
                            if (model.shuffleEnabled) Icon(SkinIcons.Shuffle, null, Modifier.size(14.dp).padding(start = 2.dp), tint = Color(0xFF8FA3BA))
                            if (model.repeatMode != 0) Icon(SkinIcons.Repeat, null, Modifier.size(14.dp).padding(start = 2.dp), tint = Color(0xFF8FA3BA))
                        }
                    }
                }
                SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.padding(top = 6.dp), track = Color(0xFF263447), fill = Color(0xFF9CC3F5), thickness = 6.dp, thumbRadius = 0.dp)
                TimeRow(model, color = Color(0xFFB7C5D6))
            }
        }
        ClickWheel(model, actions, Modifier.size(if (model.compact) 220.dp else 260.dp).padding(bottom = 8.dp))
    }
}

@Composable
private fun ClickWheel(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    var scrub by remember { mutableStateOf<Float?>(null) }
    var lastAngle by remember { mutableFloatStateOf(0f) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().shadow(10.dp, CircleShape).clip(CircleShape).background(c.surfaceVariant)
            .pointerInput(model.locked) {
                detectTapGestures { p ->
                    val dx = p.x - size.width / 2f; val dy = p.y - size.height / 2f
                    val r = hypot(dx, dy) / (size.width / 2f)
                    if (r < .34f) return@detectTapGestures // centre button has its own surface
                    val a = ((atan2(dy, dx) * 180 / PI) + 360) % 360
                    when {
                        a in 225.0..315.0 -> actions.openMenu()
                        a in 315.0..360.0 || a < 45.0 -> actions.next()
                        a in 135.0..225.0 -> actions.previous()
                        else -> actions.playPause()
                    }
                }
            }
            .pointerInput(model.locked, model.durationMs) {
                detectDragGestures(
                    onDragStart = { p -> lastAngle = atan2(p.y - size.height / 2f, p.x - size.width / 2f); scrub = model.fraction },
                    onDragEnd = { scrub?.let(actions.seekFraction); scrub = null },
                    onDragCancel = { scrub = null },
                ) { change, _ ->
                    val a = atan2(change.position.y - size.height / 2f, change.position.x - size.width / 2f)
                    var d = a - lastAngle
                    val turn = (2 * PI).toFloat()
                    if (d > turn / 2) d -= turn; if (d < -turn / 2) d += turn
                    lastAngle = a
                    // One full turn of the wheel is one third of the track.
                    scrub = ((scrub ?: model.fraction) + d / (2 * PI.toFloat()) / 3f).coerceIn(0f, 1f)
                    change.consume()
                }
            }) {
            val label = c.onSurfaceVariant
            Text("MENU", Modifier.align(Alignment.TopCenter).padding(top = 18.dp), color = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Icon(SkinIcons.FastForward, "Next", Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(22.dp), tint = label)
            Icon(SkinIcons.FastRewind, "Previous", Modifier.align(Alignment.CenterStart).padding(start = 20.dp).size(22.dp), tint = label)
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)) {
                Icon(SkinIcons.PlayArrow, null, Modifier.size(18.dp), tint = label); Icon(SkinIcons.Pause, "Play or pause", Modifier.size(18.dp), tint = label)
            }
            scrub?.let { Text(clock((it * model.durationMs).toLong()), Modifier.align(Alignment.TopCenter).padding(top = 44.dp), color = c.primary, style = MaterialTheme.typography.labelSmall) }
        }
        Surface(onClick = actions.playPause, modifier = Modifier.fillMaxSize(.34f), shape = CircleShape, color = c.surface, shadowElevation = 2.dp) {
            Box(contentAlignment = Alignment.Center) {
                if (model.isLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Icon(if (model.isPlaying) SkinIcons.Pause else SkinIcons.PlayArrow, null, Modifier.size(26.dp), tint = c.onSurfaceVariant.copy(alpha = .35f))
            }
        }
    }
    LaunchedEffect(scrub != null) { if (scrub != null) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress) }
}
