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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
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
 * A real compact cassette in a deck. The shell is drawn to scale (100 × 64 mm):
 * smoked plastic, five screws, the paper label with its side letter, the
 * window with both tape packs, the amber viewing strip and the head opening.
 *
 * Motion, all of it tied to the transport:
 *  - the reels turn at constant *tape* speed, so the small pack spins faster
 *    than the big one, and the packs trade size as the track plays;
 *  - play and pause spin the reels up and down with a little inertia instead
 *    of starting and stopping dead;
 *  - a seek fast-winds the reels in the direction of the jump;
 *  - a new track ejects the tape and slots the next one in;
 *  - the tape strip in the window drifts past, and the packs carry a faint wobble.
 */
@Composable
internal fun CassetteStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    val motion = rememberCassetteMotion(model)
    // Eject the old tape and slot the new one in when the track changes.
    val insert = remember { Animatable(0f) }
    LaunchedEffect(model.song.videoId) {
        if (model.reduceMotion) return@LaunchedEffect
        insert.snapTo(1f)
        insert.animateTo(0f, spring(dampingRatio = .62f, stiffness = 260f))
    }
    Column(modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.surface).border(1.5.dp, c.outlineVariant, RoundedCornerShape(18.dp)).padding(12.dp)) {
                // The J-card peeking out of the case: cover, title, side.
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.surfaceVariant).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model.artwork, null, Modifier.size(44.dp).clip(RoundedCornerShape(5.dp)).background(c.surface), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(model.song.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = mono, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(model.song.artist, Modifier.clickable(onClick = actions.openArtist), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text("A", style = MaterialTheme.typography.headlineSmall, color = c.primary, fontWeight = FontWeight.Black)
                }
                // The deck's well: the tape sits in it and drops out of it on eject.
                Box(Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0D0B0A)).padding(6.dp)) {
                    CassetteShell(motion, Modifier.fillMaxWidth().aspectRatio(1.5625f)
                        .graphicsLayer {
                            translationY = insert.value * size.height * 1.05f
                            rotationX = insert.value * 18f
                            alpha = 1f - insert.value * .6f
                        })
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp, start = 2.dp, end = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(model.trackFormat.ifBlank { "Stereo" }.uppercase(), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${clock(model.position.positionMs)} / ${if (model.durationMs > 0) clock(model.durationMs) else "–:––"}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = mono), color = c.onSurfaceVariant)
                }
            }
        }
        SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.padding(top = 14.dp), thickness = 6.dp, thumbRadius = 0.dp)
        // Deck keys: wide mechanical buttons.
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DeckKey(SkinIcons.FastRewind, "Previous", actions.previous, Modifier.weight(1f), model.hasPrevious)
            DeckKey(if (model.isPlaying) SkinIcons.Pause else SkinIcons.PlayArrow, "Play", actions.playPause, Modifier.weight(1.6f), true, accent = true, loading = model.isLoading)
            DeckKey(SkinIcons.FastForward, "Next", actions.next, Modifier.weight(1f), model.hasNext)
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DeckKey(SkinIcons.Shuffle, "Shuffle", actions.toggleShuffle, Modifier.weight(1f), true, lit = model.shuffleEnabled)
            DeckKey(if (model.liked) SkinIcons.Favorite else SkinIcons.FavoriteBorder, "Like", actions.toggleLike, Modifier.weight(1f), true, lit = model.liked)
            DeckKey(if (model.repeatMode == 1) SkinIcons.RepeatOne else SkinIcons.Repeat, "Repeat", actions.cycleRepeat, Modifier.weight(1f), true, lit = model.repeatMode != 0)
            DeckKey(SkinIcons.SpeakerGroup, "Output", actions.openOutput, Modifier.weight(1f), true)
            DeckKey(SkinIcons.MoreHoriz, "Options", actions.openMenu, Modifier.weight(1f), true)
        }
    }
}

/** Reel angles, pack sizes and the tape drift, advanced once per frame. */
@Stable
internal class CassetteMotion {
    var left by mutableFloatStateOf(0f)
    var right by mutableFloatStateOf(0f)
    var drift by mutableFloatStateOf(0f)
    var time by mutableFloatStateOf(0f)
    /** 0 = full left pack (start of the track), 1 = full right pack. */
    var fraction by mutableFloatStateOf(0f)
}

private const val HUB = .088f      // hub radius, as a share of the shell width
private const val PACK_MIN = .098f
private const val PACK_MAX = .168f
internal fun packRadius(share: Float) = PACK_MIN + (PACK_MAX - PACK_MIN) * share

@Composable
internal fun rememberCassetteMotion(model: NowPlayingModel): CassetteMotion {
    val motion = remember { CassetteMotion() }
    // Spin up and down with inertia rather than snapping.
    val speed by animateFloatAsState(if (model.isPlaying && !model.isLoading) 1f else 0f,
        tween(if (model.reduceMotion) 0 else 650, easing = FastOutSlowInEasing), label = "capstan")
    val currentSpeed by rememberUpdatedState(speed)
    val fraction by rememberUpdatedState(model.fraction)
    val reduce by rememberUpdatedState(model.reduceMotion)
    // A seek fast-winds the reels: remember where we were and how far we jumped.
    var lastFraction by remember { mutableFloatStateOf(model.fraction) }
    var wind by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(model.fraction) {
        val jump = model.fraction - lastFraction
        if (abs(jump) > .02f && !model.reduceMotion) wind = (jump * 40f).coerceIn(-14f, 14f)
        lastFraction = model.fraction
    }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceAtMost(.05f)
            last = now
            // The packs ease toward the real position so a seek visibly winds rather than jumps.
            motion.fraction += (fraction - motion.fraction) * (if (abs(wind) > .1f) .08f else .35f)
            if (reduce) { motion.fraction = fraction; continue }
            motion.time += dt
            val drive = currentSpeed + wind
            wind *= .9f
            if (abs(wind) < .05f) wind = 0f
            // Constant tape speed: ω = v / r, so the emptier reel turns faster.
            val base = 150f * dt * drive
            motion.left = (motion.left + base * HUB / packRadius(1f - motion.fraction)) % 360f
            motion.right = (motion.right + base * HUB / packRadius(motion.fraction)) % 360f
            motion.drift = (motion.drift + dt * 22f * drive) % 1000f
        }
    }
    return motion
}

/** The shell itself, drawn to scale. Shared with the mini player and the queue. */
@Composable
internal fun CassetteShell(motion: CassetteMotion, modifier: Modifier, label: Color = Color(0xFFE6D8BA)) {
    val accent = MaterialTheme.colorScheme.primary
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) { drawCassette(motion, label, accent) }
        // The side letter, in the label's corner.
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Text("A", Modifier.padding(start = maxWidth * .095f, top = maxHeight * .118f),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = (maxWidth.value * .075f).sp, fontWeight = FontWeight.Black),
                color = Color(0xFF2B241D))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCassette(m: CassetteMotion, label: Color, accent: Color) {
    val w = size.width; val h = size.height
    fun x(f: Float) = w * f
    fun y(f: Float) = h * f
    val shellTop = Color(0xFF2C2724); val shellBottom = Color(0xFF181513)
    val rim = Color(0xFF443C36)
    // Shell
    drawRoundRect(Brush.verticalGradient(listOf(shellTop, shellBottom)), cornerRadius = CornerRadius(x(.035f)))
    drawRoundRect(rim, cornerRadius = CornerRadius(x(.035f)), style = Stroke(x(.006f)))
    // Moulding: a raised inner frame and faint ribbing along the top.
    drawRoundRect(Color(0xFF221E1B), Offset(x(.045f), y(.07f)), Size(x(.91f), y(.70f)), CornerRadius(x(.02f)))
    for (i in 0..5) drawLine(Color.White.copy(alpha = .025f), Offset(x(.12f), y(.035f + i * .006f)), Offset(x(.88f), y(.035f + i * .006f)), 1f)
    // Top tabs
    drawRoundRect(rim, Offset(x(.08f), y(.015f)), Size(x(.06f), y(.035f)), CornerRadius(3f))
    drawRoundRect(rim, Offset(x(.86f), y(.015f)), Size(x(.06f), y(.035f)), CornerRadius(3f))
    // Paper label with its ruled lines
    val labelTop = y(.105f); val labelH = y(.16f)
    drawRoundRect(Brush.verticalGradient(listOf(label, label.copy(red = label.red * .93f, green = label.green * .92f, blue = label.blue * .88f)), startY = labelTop, endY = labelTop + labelH),
        Offset(x(.07f), labelTop), Size(x(.86f), labelH), CornerRadius(x(.012f)))
    val ink = Color(0xFF8C7F6A)
    drawLine(ink, Offset(x(.17f), labelTop + labelH * .35f), Offset(x(.9f), labelTop + labelH * .35f), 1.2f)
    drawLine(ink, Offset(x(.17f), labelTop + labelH * .7f), Offset(x(.9f), labelTop + labelH * .7f), 1.2f)
    // Window panel
    val winTop = y(.30f); val winBottom = y(.73f)
    drawRoundRect(Color(0xFF100E0D), Offset(x(.07f), winTop), Size(x(.86f), winBottom - winTop), CornerRadius(x(.02f)))
    drawRoundRect(Color(0xFF3A332D), Offset(x(.07f), winTop), Size(x(.86f), winBottom - winTop), CornerRadius(x(.02f)), style = Stroke(1.5f))
    val l = Offset(x(.31f), y(.515f)); val r = Offset(x(.69f), y(.515f))
    val wobble = sin(m.time * 5.3f) * x(.0012f)
    val packL = x(packRadius(1f - m.fraction)) + wobble
    val packR = x(packRadius(m.fraction)) - wobble
    clipRect(x(.07f), winTop, x(.93f), winBottom) {
        // Tape packs, with a sheen ring so they read as wound tape.
        for ((centre, pack) in listOf(l to packL, r to packR)) {
            drawCircle(Color(0xFF2A1F19), pack, centre)
            drawCircle(Brush.radialGradient(listOf(Color(0x00FFFFFF), Color(0x18FFFFFF), Color(0x00FFFFFF)), centre, pack), pack, centre)
            drawCircle(Color(0xFF1B1410), pack, centre, style = Stroke(1.5f))
        }
        // The tape running across the bottom of the window to the head
        drawLine(Color(0xFF3B2A20), Offset(l.x - packL * .2f, l.y + packL * .98f), Offset(x(.2f), winBottom), x(.006f))
        drawLine(Color(0xFF3B2A20), Offset(r.x + packR * .2f, r.y + packR * .98f), Offset(x(.8f), winBottom), x(.006f))
    }
    // Amber viewing strip between the reels, the tape drifting past inside it.
    val stripL = x(.405f); val stripR = x(.595f); val stripT = y(.43f); val stripB = y(.60f)
    drawRoundRect(Color(0xFF6B4220).copy(alpha = .9f), Offset(stripL, stripT), Size(stripR - stripL, stripB - stripT), CornerRadius(x(.008f)))
    clipRect(stripL, stripT, stripR, stripB) {
        val step = x(.012f)
        val shift = (m.drift * x(.01f)) % step
        var px = stripL - step + shift
        var i = 0
        while (px < stripR) {
            val tall = ((i * 37 + 11) % 5) / 5f
            drawLine(Color(0xFFE3B47A).copy(alpha = .35f + tall * .3f), Offset(px, stripT + (stripB - stripT) * (.3f - tall * .15f)),
                Offset(px, stripB - (stripB - stripT) * (.3f - tall * .15f)), 1.3f)
            px += step; i++
        }
        drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .45f), Color.Transparent, Color.Black.copy(alpha = .45f)), stripL, stripR), Offset(stripL, stripT), Size(stripR - stripL, stripB - stripT))
    }
    // Reel hubs: cream rings, gold rim, six drive teeth that turn.
    for ((centre, angle) in listOf(l to m.left, r to m.right)) {
        val hub = x(HUB)
        drawCircle(Color(0xFFB98A4E), hub * 1.06f, centre)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFF2E6CF), Color(0xFFD8C4A0)), centre, hub), hub, centre)
        drawCircle(Color(0xFF151210), hub * .62f, centre)
        rotate(angle, centre) {
            for (i in 0 until 6) rotate(i * 60f, centre) {
                drawRoundRect(Color(0xFFEDE0C6), Offset(centre.x - hub * .09f, centre.y - hub * .62f), Size(hub * .18f, hub * .2f), CornerRadius(2f))
            }
            // A tick on the hub ring so the turning is easy to see.
            drawCircle(Color(0xFF9B7A4C), hub * .07f, centre + Offset(0f, -hub * .82f))
        }
    }
    // Head opening: the trapezoid along the bottom edge
    val trap = androidx.compose.ui.graphics.Path().apply {
        moveTo(x(.16f), h); lineTo(x(.23f), y(.79f)); lineTo(x(.77f), y(.79f)); lineTo(x(.84f), h); close()
    }
    drawPath(trap, Brush.verticalGradient(listOf(Color(0xFF2E2925), Color(0xFF1E1A17)), y(.79f), h))
    drawPath(trap, rim, style = Stroke(1.5f))
    for (fx in listOf(.245f, .755f)) drawCircle(Color(0xFF080707), x(.022f), Offset(x(fx), y(.905f)))
    for (fx in listOf(.345f, .655f)) drawRoundRect(Color(0xFF080707), Offset(x(fx) - x(.017f), y(.875f) - x(.017f)), Size(x(.034f), x(.034f)), CornerRadius(3f))
    // Screws: four corners and one in the middle of the head opening
    for (p in listOf(Offset(x(.035f), y(.055f)), Offset(x(.965f), y(.055f)), Offset(x(.035f), y(.945f)), Offset(x(.965f), y(.945f)), Offset(x(.5f), y(.835f)))) {
        val sr = x(.019f)
        drawCircle(Brush.radialGradient(listOf(Color(0xFF8E8478), Color(0xFF4A433C)), p, sr), sr, p)
        drawLine(Color(0xFF26211D), p + Offset(-sr * .6f, 0f), p + Offset(sr * .6f, 0f), 1.6f)
        drawLine(Color(0xFF26211D), p + Offset(0f, -sr * .6f), p + Offset(0f, sr * .6f), 1.6f)
    }
    // A soft highlight across the plastic
    drawRoundRect(Brush.linearGradient(listOf(Color.White.copy(alpha = .06f), Color.Transparent), Offset.Zero, Offset(w * .6f, h)), cornerRadius = CornerRadius(x(.035f)))
}

@Composable
private fun DeckKey(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier, enabled: Boolean,
    accent: Boolean = false, lit: Boolean = false, loading: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.height(50.dp), shape = RoundedCornerShape(10.dp),
        color = if (accent) c.primary else c.surfaceVariant, contentColor = if (accent) c.onPrimary else if (lit) c.primary else c.onSurfaceVariant,
        shadowElevation = 3.dp, border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant.copy(alpha = if (accent) 0f else 1f))) {
        Box(Modifier.alpha(if (enabled) 1f else .4f), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = c.onPrimary) else Icon(icon, label, Modifier.size(22.dp))
            if (lit) Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(5.dp).clip(CircleShape).background(c.primary))
        }
    }
}
