package com.music.streamx.skins.common

import com.music.streamx.skins.common.icons.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * A seek bar drawn by hand: a track, a fill and a thumb, with drag and tap.
 * The committed position is only sent on release, so a drag does not fire a
 * seek per pixel.
 */
@Composable
fun SeekBar(fraction: Float, enabled: Boolean, onSeek: (Float) -> Unit, modifier: Modifier = Modifier,
    track: Color = MaterialTheme.colorScheme.outlineVariant, fill: Color = MaterialTheme.colorScheme.primary,
    thickness: Dp = 4.dp, thumbRadius: Dp = 7.dp, onDraft: ((Float?) -> Unit)? = null) {
    var draft by remember { mutableStateOf<Float?>(null) }
    var width by remember { mutableFloatStateOf(1f) }
    val shown = draft ?: fraction
    Canvas(modifier.fillMaxWidth().height(thumbRadius * 2 + 12.dp).semantics { contentDescription = "Playback position" }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures { pos -> onSeek((pos.x / size.width).coerceIn(0f, 1f)) }
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectHorizontalDragGestures(
                onDragStart = { pos -> draft = (pos.x / size.width).coerceIn(0f, 1f); onDraft?.invoke(draft) },
                onDragEnd = { draft?.let(onSeek); draft = null; onDraft?.invoke(null) },
                onDragCancel = { draft = null; onDraft?.invoke(null) },
            ) { change, _ -> draft = (change.position.x / size.width).coerceIn(0f, 1f); onDraft?.invoke(draft) }
        }) {
        width = size.width
        val y = size.height / 2
        val t = thickness.toPx()
        drawRoundRect(track, Offset(0f, y - t / 2), Size(size.width, t), CornerRadius(t / 2))
        drawRoundRect(fill, Offset(0f, y - t / 2), Size(size.width * shown, t), CornerRadius(t / 2))
        if (thumbRadius > 0.dp) drawCircle(fill, thumbRadius.toPx(), Offset(size.width * shown, y))
    }
}

/** Elapsed and total, under a seek bar. */
@Composable
fun TimeRow(model: NowPlayingModel, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(clock(model.position.positionMs), style = MaterialTheme.typography.labelSmall, color = color)
        Text(if (model.durationMs > 0) clock(model.durationMs) else "–:––", style = MaterialTheme.typography.labelSmall, color = color)
    }
}

/** Title and artist, the artist tappable. */
@Composable
fun TrackTitles(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier = Modifier,
    titleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineSmall,
    color: Color = MaterialTheme.colorScheme.onBackground, secondary: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    centered: Boolean = false) {
    Column(modifier, horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start) {
        Text(model.song.title, style = titleStyle, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(model.song.artist, style = MaterialTheme.typography.bodyMedium, color = secondary, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp).then(Modifier.pointerInput(Unit) { detectTapGestures { actions.openArtist() } }))
    }
}

/** Shuffle · previous · play/pause · next · repeat, in a row. */
@Composable
fun TransportRow(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground, accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary, playSize: Dp = 68.dp, showModes: Boolean = true) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
        if (showModes) IconButton(actions.toggleShuffle) { Icon(SkinIcons.Shuffle, "Shuffle", tint = if (model.shuffleEnabled) accent else tint.copy(alpha = .6f)) }
        IconButton(actions.previous, enabled = model.hasPrevious, modifier = Modifier.size(56.dp)) { Icon(SkinIcons.SkipPrevious, "Previous", Modifier.size(34.dp), tint = tint) }
        PlayPauseButton(model, actions.playPause, Modifier.size(playSize), accent, onAccent)
        IconButton(actions.next, enabled = model.hasNext, modifier = Modifier.size(56.dp)) { Icon(SkinIcons.SkipNext, "Next", Modifier.size(34.dp), tint = tint) }
        if (showModes) IconButton(actions.cycleRepeat) {
            Icon(if (model.repeatMode == 1) SkinIcons.RepeatOne else SkinIcons.Repeat, "Repeat", tint = if (model.repeatMode != 0) accent else tint.copy(alpha = .6f))
        }
    }
}

/** A round play/pause button with a spinner while the stream is still loading. */
@Composable
fun PlayPauseButton(model: NowPlayingModel, onClick: () -> Unit, modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary, content: Color = MaterialTheme.colorScheme.onPrimary) {
    Surface(onClick = onClick, modifier = modifier, shape = androidx.compose.foundation.shape.CircleShape, color = color, contentColor = content) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (model.isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = content, strokeWidth = 2.5.dp)
            else Icon(if (model.isPlaying) SkinIcons.Pause else SkinIcons.PlayArrow, if (model.isPlaying) "Pause" else "Play", Modifier.fillMaxSize(.5f))
        }
    }
}

/** Like · output · menu, as a small secondary row. */
@Composable
fun SecondaryRow(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(actions.toggleLike) { Icon(if (model.liked) SkinIcons.Favorite else SkinIcons.FavoriteBorder, "Like", tint = if (model.liked) MaterialTheme.colorScheme.primary else tint) }
        TextButton(actions.openOutput, Modifier.weight(1f)) {
            Icon(SkinIcons.SpeakerGroup, null, Modifier.size(16.dp), tint = tint); Spacer(Modifier.width(6.dp))
            Text(model.trackFormat.ifBlank { "Audio output" }, style = MaterialTheme.typography.labelMedium, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(actions.openMenu) { Icon(SkinIcons.MoreHoriz, "Track options", tint = tint) }
    }
}

/**
 * Degrees of rotation that advance while [playing] and hold still otherwise,
 * so a record or reel stops where it was rather than snapping home. Frozen
 * when the listener asked for reduced motion.
 */
@Composable
fun rememberSpin(playing: Boolean, reduceMotion: Boolean, rpm: Float = 33f): Float {
    var angle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(playing, reduceMotion, rpm) {
        if (!playing || reduceMotion) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            angle = (angle + (now - last) / 1_000_000_000f * rpm * 6f) % 360f
            last = now
        }
    }
    return angle
}

/**
 * The live spectrum off the host's meter tap — see [com.music.streamx.playback.AudioLevels].
 * Null when the host has no tap on this path, so a caller can fall back to a decoration.
 */
@Composable
fun rememberLiveLevels(playing: Boolean): com.music.streamx.playback.AudioLevels.Frame? {
    val available by com.music.streamx.playback.AudioLevels.available.collectAsStateWithLifecycle()
    val frame by com.music.streamx.playback.AudioLevels.current.collectAsStateWithLifecycle()
    if (!available) return null
    return if (playing) frame else com.music.streamx.playback.AudioLevels.SILENT
}

/**
 * A row of bars driven by the real signal: the host's meter publishes a
 * fourteen-band spectrum and this spreads it across [bars]. Where no meter
 * exists (a passthrough bitstream, an older host) it falls back to a gentle
 * decorative sway so the panel is never dead, and it goes flat on pause.
 */
@Composable
fun LevelBars(playing: Boolean, reduceMotion: Boolean, color: Color, modifier: Modifier = Modifier, bars: Int = 24, gap: Dp = 3.dp, rounded: Boolean = true,
    /** Draw as discrete LED segments rather than solid bars. */ segments: Int = 0, dim: Color = color.copy(alpha = .14f)) {
    val live = rememberLiveLevels(playing)
    val t by rememberInfiniteTransition(label = "levels").animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "phase")
    Canvas(modifier) {
        val g = gap.toPx()
        val w = (size.width - g * (bars - 1)) / bars
        for (i in 0 until bars) {
            val level = when {
                live != null -> {
                    // Resample the bands onto the bars with linear interpolation.
                    val x = i.toFloat() / (bars - 1).coerceAtLeast(1) * (live.bands.size - 1)
                    val a = live.bands[x.toInt().coerceIn(0, live.bands.size - 1)]
                    val b = live.bands[(x.toInt() + 1).coerceIn(0, live.bands.size - 1)]
                    (a + (b - a) * (x - x.toInt())).coerceIn(0f, 1f)
                }
                !playing -> .06f
                reduceMotion -> .4f
                else -> (0.25f + 0.75f * (0.5f + 0.5f * sin(t * 6.283f * (1 + i % 3) + i * .7f))).coerceIn(.06f, 1f)
            }
            val x0 = i * (w + g)
            if (segments > 0) {
                val sh = (size.height - g * (segments - 1)) / segments
                for (sIdx in 0 until segments) {
                    val lit = sIdx < (level * segments).toInt() + (if (level > 0f && sIdx == 0) 1 else 0)
                    drawRoundRect(if (lit) color else dim, Offset(x0, size.height - (sIdx + 1) * sh - sIdx * g), Size(w, sh), CornerRadius(2f))
                }
            } else {
                val h = size.height * level.coerceAtLeast(.03f)
                drawRoundRect(dim, Offset(x0, 0f), Size(w, size.height), if (rounded) CornerRadius(w / 2) else CornerRadius.Zero)
                drawRoundRect(color, Offset(x0, size.height - h), Size(w, h), if (rounded) CornerRadius(w / 2) else CornerRadius.Zero)
            }
        }
    }
}

/**
 * A stereo VU pair from the live RMS and peak: two vertical LED ladders with a
 * held peak dot, amber below the knee and red above it.
 */
@Composable
fun StereoMeter(playing: Boolean, color: Color, hot: Color, modifier: Modifier = Modifier, segments: Int = 18) {
    val live = rememberLiveLevels(playing)
    Canvas(modifier) {
        val gap = 3.dp.toPx()
        val colW = (size.width - gap) / 2
        val segH = (size.height - gap * (segments - 1)) / segments
        val levels = floatArrayOf(live?.left ?: 0f, live?.right ?: 0f)
        val peaks = floatArrayOf(live?.peakLeft ?: 0f, live?.peakRight ?: 0f)
        for (ch in 0..1) {
            val x = ch * (colW + gap)
            val lit = (levels[ch] * segments).toInt()
            val peakSeg = (peaks[ch] * segments).toInt().coerceIn(0, segments - 1)
            for (sIdx in 0 until segments) {
                val on = sIdx < lit || (sIdx == peakSeg && peaks[ch] > .02f)
                val base = if (sIdx >= segments - 3) hot else color
                drawRoundRect(if (on) base else base.copy(alpha = .14f), Offset(x, size.height - (sIdx + 1) * segH - sIdx * gap), Size(colW, segH), CornerRadius(2f))
            }
        }
    }
}

/** A thin arc progress ring around whatever it wraps. */
@Composable
fun ProgressRing(fraction: Float, color: Color, track: Color, modifier: Modifier = Modifier, stroke: Dp = 4.dp, content: @Composable BoxScope.() -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            drawArc(track, -90f, 360f, false, Offset(s / 2, s / 2), Size(size.width - s, size.height - s), style = androidx.compose.ui.graphics.drawscope.Stroke(s, cap = StrokeCap.Round))
            drawArc(color, -90f, 360f * fraction, false, Offset(s / 2, s / 2), Size(size.width - s, size.height - s), style = androidx.compose.ui.graphics.drawscope.Stroke(s, cap = StrokeCap.Round))
        }
        Box(Modifier.fillMaxSize().padding(stroke + 6.dp), content = content)
    }
}

/** Rotates its content by [degrees] around its centre. */
fun Modifier.spin(degrees: Float): Modifier = graphicsLayer { rotationZ = degrees }
