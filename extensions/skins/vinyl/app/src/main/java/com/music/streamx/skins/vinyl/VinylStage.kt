package com.music.streamx.skins.vinyl

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
 * A turntable. The record spins at 33⅓ while playing and stops where it was
 * when paused; the tonearm swings onto the groove as the track starts and
 * tracks inward with the position, and lifts off when nothing is playing.
 */
@Composable
internal fun VinylStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val spin = rememberSpin(model.isPlaying, model.reduceMotion, rpm = 33.3f)
    val armAngle by animateFloatAsState(if (model.isPlaying || model.position.positionMs > 0) 20f + 6f * model.fraction else 8f,
        tween(if (model.reduceMotion) 0 else 900, easing = FastOutSlowInEasing), label = "tonearm")
    Column(modifier.padding(horizontal = 20.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            val platter = minOf(maxWidth - 24.dp, maxHeight - 40.dp, 420.dp).coerceAtLeast(1.dp)
            // Plinth
            Box(Modifier.fillMaxWidth().height(platter + 40.dp).clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(listOf(c.surfaceVariant, c.surface))).border(1.dp, c.outlineVariant, RoundedCornerShape(26.dp)))
            // Platter and record
            Box(Modifier.size(platter).align(Alignment.CenterStart).padding(start = 0.dp).offset(x = 8.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().spin(spin)) {
                    val r = size.minDimension / 2
                    drawCircle(Color(0xFF151312), r)
                    for (i in 1..14) drawCircle(Color(0xFF26221F), r * (0.5f + i * 0.034f), style = Stroke(1.2f))
                    // A soft light glint, so the surface reads as vinyl rather than a flat disc.
                    drawArc(Color.White.copy(alpha = .05f), 200f, 70f, true, style = androidx.compose.ui.graphics.drawscope.Fill)
                }
                AsyncImage(model.artwork, null, Modifier.fillMaxSize(.70f).spin(spin).clip(CircleShape).background(c.primary), contentScale = ContentScale.Crop)
                Box(Modifier.size(10.dp).clip(CircleShape).background(c.onSurfaceVariant))
            }
            VinylTonearm(armAngle, Modifier.fillMaxWidth().height(platter + 40.dp))
        }
        TrackTitles(model, actions, Modifier.padding(top = 16.dp), titleStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Serif)))
        SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.padding(top = 10.dp), thickness = 3.dp, thumbRadius = 6.dp)
        TimeRow(model)
        TransportRow(model, actions, Modifier.padding(top = 4.dp), playSize = 64.dp)
        SecondaryRow(model, actions)
    }
}


/** The arm stays anchored to the deck while the record rotates beneath it. */
@Composable
internal fun VinylTonearm(angle: Float, modifier: Modifier, miniature: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Canvas(modifier) {
        val margin = (if (miniature) 7.dp else 26.dp).toPx()
        val pivot = Offset(size.width - margin, margin)
        val length = size.height * (if (miniature) .84f else .82f)
        val radians = angle * PI.toFloat() / 180f
        val elbow = Offset(pivot.x - sin(radians) * length, pivot.y + cos(radians) * length)
        val head = elbow + Offset(-(if (miniature) 2.dp else 8.dp).toPx(), (if (miniature) 2.dp else 5.dp).toPx())
        val shaft = (if (miniature) 1.8.dp else 5.dp).toPx()
        val radius = (if (miniature) 3.5.dp else 12.dp).toPx()
        drawCircle(c.onSurfaceVariant, radius, pivot)
        drawLine(c.onSurface, pivot, elbow, shaft, StrokeCap.Round)
        drawLine(c.onSurface, elbow, head, shaft, StrokeCap.Round)
        drawCircle(c.surface, radius * .4f, pivot)
        val headWidth = (if (miniature) 3.dp else 9.dp).toPx()
        val headHeight = (if (miniature) 6.dp else 18.dp).toPx()
        drawRoundRect(c.primary, head - Offset(headWidth / 2, headHeight / 2),
            Size(headWidth, headHeight), CornerRadius(headWidth / 3))
    }
}
