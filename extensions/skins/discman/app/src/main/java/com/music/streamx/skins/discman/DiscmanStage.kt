package com.music.streamx.skins.discman

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
 * A portable CD player. The disc spins under a clear lid, catching the light
 * in a soft rainbow sweep; the segment display below reads track and time.
 */
@Composable
internal fun DiscmanStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val spin = rememberSpin(model.isPlaying, model.reduceMotion, rpm = 50f)
    Column(modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            val disc = minOf(maxWidth - 24.dp, maxHeight - 12.dp, 360.dp)
            // Lid
            Box(Modifier.size(disc + 24.dp).clip(CircleShape).background(Brush.radialGradient(listOf(c.surfaceVariant, c.surface))).border(1.dp, c.outlineVariant, CircleShape))
            Box(Modifier.size(disc).spin(spin), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2
                    drawCircle(Brush.sweepGradient(listOf(Color(0xFFE9E4F5), Color(0xFFCFE9F7), Color(0xFFDDF2E1), Color(0xFFF9EBD5), Color(0xFFF5D9E3), Color(0xFFE9E4F5))), r)
                    for (i in 0 until 40) drawCircle(Color.White.copy(alpha = .18f), r * (0.46f + i * 0.0135f), style = Stroke(1f))
                    drawCircle(Color(0xFFD8D4E0), r * .2f)
                    drawCircle(Color(0xFF1A1A1E), r * .07f)
                }
                AsyncImage(model.artwork, null, Modifier.fillMaxSize(.6f).clip(CircleShape).background(c.surface), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize(.14f).clip(CircleShape).background(c.background).border(2.dp, c.outlineVariant, CircleShape))
            }
            // Lid glare
            Box(Modifier.size(disc + 24.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color.White.copy(alpha = .22f), Color.Transparent, Color.Transparent))))
        }
        // Segment display
        Surface(Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFF17201B), border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("TRK ${(model.queueIndex + 1).toString().padStart(2, '0')}", color = Color(0xFF9FE1B8), style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(FontFamily.Monospace)))
                    Spacer(Modifier.weight(1f))
                    Text(clock(model.position.positionMs), color = Color(0xFF9FE1B8), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = skinFont(FontFamily.Monospace), fontWeight = FontWeight.Bold))
                }
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(listOfNotNull(if (model.isPlaying) "PLAY" else "PAUSE", if (model.shuffleEnabled) "SHUF" else null, if (model.repeatMode != 0) "RPT" else null, model.trackFormat.takeIf { it.isNotBlank() })
                        .joinToString("  "), color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    LevelBars(model.isPlaying, model.reduceMotion, Color(0xFF9FE1B8), Modifier.width(60.dp).height(14.dp), bars = 10, gap = 2.dp, rounded = false)
                }
                SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.padding(top = 4.dp), track = Color(0xFF2A3A30), fill = Color(0xFF9FE1B8), thickness = 3.dp, thumbRadius = 0.dp)
            }
        }
        TrackTitles(model, actions, Modifier.padding(top = 12.dp).fillMaxWidth(), titleStyle = MaterialTheme.typography.titleLarge, centered = true)
        TransportRow(model, actions, Modifier.padding(top = 4.dp), playSize = 62.dp)
        SecondaryRow(model, actions)
    }
}
