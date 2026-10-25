package com.music.streamx.skins.namida

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Song
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.skins.SkinIcon
import com.music.streamx.ui.skins.rememberSkinRemoteArtwork
import com.music.streamx.ui.skins.skinPlayerSettings
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Where the mini player's pieces are on screen, for the player to grow out of.
 * In Namida the mini player *is* the player at its smallest; here they're two
 * surfaces in two windows, so each reports its half of the movement and the
 * player draws the whole of it.
 */
internal object NamidaDock {
    var panel by mutableStateOf<Rect?>(null)
    var art by mutableStateOf<Rect?>(null)
    var play by mutableStateOf<Rect?>(null)

    /** How far open the player is, while one is up; the mini player stands aside under it. */
    var open by mutableStateOf<(() -> Float)?>(null)
}

internal const val MINI_HEIGHT = 82
internal const val MINI_RADIUS = 20
internal const val MINI_ART = 58

@Composable
internal fun NamidaMiniPlayer(song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long,
    onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit,
    locked: Boolean, onBlocked: () -> Unit, modifier: Modifier = Modifier) {
    val art = rememberSkinRemoteArtwork(song)
    PublishArtworkSeed(art)
    val t = LocalNamidaTones.current
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // A sideways swipe carries the track info with the finger, then on to the
    // neighbouring track, as Namida's does.
    val swipe = remember { Animatable(0f) }
    val guarded: (() -> Unit) -> () -> Unit = { action -> { if (locked) onBlocked() else action() } }
    Box(modifier.fillMaxWidth().padding(horizontal = 12.dp)
        // Out of sight while the player is growing from it: two covers at once
        // read as a glitch, not a transition.
        .graphicsLayer { alpha = if ((NamidaDock.open?.invoke() ?: 0f) > 0f) 0f else 1f }
        .onGloballyPositioned { NamidaDock.panel = it.boundsInWindow() }
        .shadow(10.dp, RoundedCornerShape(MINI_RADIUS.dp), ambientColor = Color.Black.copy(alpha = .25f), spotColor = Color.Black.copy(alpha = .3f))
        .clip(RoundedCornerShape(MINI_RADIUS.dp))
        .background(Brush.verticalGradient(listOf(t.panelTop, t.panelBottom)))
        .height(MINI_HEIGHT.dp)
        .pointerInput(locked) {
            val width = size.width.toFloat()
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var vertical = 0f
                var horizontal = 0f
                val start = awaitTouchSlopOrCancellation(down.id) { change, over ->
                    if (abs(over.x) > abs(over.y)) horizontal = over.x else vertical = over.y
                    change.consume()
                } ?: return@awaitEachGesture
                if (horizontal != 0f) {
                    drag(start.id) { change ->
                        horizontal += change.position.x - change.previousPosition.x
                        change.consume()
                        scope.launch { swipe.snapTo(horizontal / width) }
                    }
                    val goes = abs(horizontal) > width * .22f
                    scope.launch {
                        if (goes && !locked) {
                            val dir = if (horizontal < 0) -1f else 1f
                            swipe.animateTo(dir, tween(if (reduce) 0 else 160))
                            if (dir < 0) onNext() else onPrevious()
                            swipe.snapTo(-dir)
                            swipe.animateTo(0f, spring(dampingRatio = .85f, stiffness = 500f))
                        } else {
                            if (goes) onBlocked()
                            swipe.animateTo(0f, spring(dampingRatio = .8f, stiffness = 600f))
                        }
                    }
                } else {
                    drag(start.id) { change ->
                        vertical += change.position.y - change.previousPosition.y
                        change.consume()
                    }
                    if (vertical < -24.dp.toPx()) onExpand()
                }
            }
        }
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onExpand)) {
        Row(Modifier.fillMaxWidth().fillMaxHeight().padding(start = 12.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(MINI_ART.dp)
                .onGloballyPositioned { NamidaDock.art = it.boundsInWindow() }
                .graphicsLayer { translationX = swipe.value * 40.dp.toPx(); alpha = 1f - abs(swipe.value) * .6f }
                .shadow(6.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(t.card)) {
                AsyncImage(art, null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSizeCompat())
            }
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 6.dp)
                .graphicsLayer { translationX = swipe.value * size.width * .6f; alpha = 1f - abs(swipe.value) }) {
                Text(song.artist, color = t.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.title, color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            }
            MiniIcon(NamidaIcons.Previous, "Previous", t.onBackground, guarded(onPrevious))
            NamidaPlayButton(playing, loading, 44, guarded(onPlayPause),
                Modifier.onGloballyPositioned { NamidaDock.play = it.boundsInWindow() })
            MiniIcon(NamidaIcons.Next, "Next", t.onBackground, guarded(onNext))
        }
        // The waveform folded down to a line along the panel's foot.
        val progress = if (durationMs > 0) (position.positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        Spacer(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).padding(horizontal = 18.dp)
            .drawBehind {
                drawLine(t.onBackground.copy(alpha = .12f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height, StrokeCap.Round)
                drawLine(t.onBackground.copy(alpha = .55f), Offset(0f, size.height / 2), Offset(size.width * progress, size.height / 2), size.height, StrokeCap.Round)
            })
    }
}

@Composable
private fun MiniIcon(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        SkinIcon(icon, label, Modifier.size(21.dp), tint = tint.copy(alpha = .85f))
    }
}

/**
 * Namida's play button: a circle in the track colour, shaded to grey toward its
 * lower corner, glowing a little of the same colour underneath.
 */
@Composable
internal fun NamidaPlayButton(playing: Boolean, loading: Boolean, sizeDp: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = LocalNamidaTones.current
    val color = t.main.copy(alpha = 1f)
    val shaded = color.copy(alpha = .78f).compositeOver(Color.Gray)
    Box(modifier.size(sizeDp.dp)
        .drawBehind {
            val r = size.minDimension / 2
            drawCircle(color.copy(alpha = .35f), r + 3.dp.toPx(), center + Offset(0f, 2.dp.toPx()))
            drawCircle(Brush.linearGradient(0f to color, .7f to shaded, start = Offset.Zero, end = Offset(size.width, size.height)), r)
        }
        .clip(CircleShape)
        .clickable(onClick = onClick),
        contentAlignment = Alignment.Center) {
        val icon = sizeDp * .5f
        if (loading) CircularProgressIndicator(Modifier.size((icon * 1.1f).dp), color = Color.White.copy(alpha = .6f), strokeWidth = 2.dp)
        else SkinIcon(if (playing) NamidaIcons.Pause else NamidaIcons.Play, if (playing) "Pause" else "Play",
            Modifier.size(icon.dp), tint = Color.White.copy(alpha = .78f))
    }
}

/** Fills a Box without reaching for BoxScope, so it can sit after other modifiers. */
internal fun Modifier.matchParentSizeCompat(): Modifier = this.fillMaxWidth().fillMaxHeight()

internal fun Rect.lerpTo(other: Rect, f: Float): Rect = Rect(
    left + (other.left - left) * f, top + (other.top - top) * f,
    right + (other.right - right) * f, bottom + (other.bottom - bottom) * f)

internal fun roundRect(rect: Rect, radius: Float) = androidx.compose.ui.geometry.RoundRect(rect, CornerRadius(radius))
