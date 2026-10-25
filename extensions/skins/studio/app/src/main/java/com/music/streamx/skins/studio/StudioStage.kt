package com.music.streamx.skins.studio

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
 * A channel strip from a mixing desk: amber meters, a long throw fader for the
 * position, latching buttons with status LEDs, and graphite everywhere else.
 */
@Composable
internal fun StudioStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val amber = c.primary
    Column(modifier.padding(horizontal = 20.dp)) {
        Surface(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(18.dp), color = c.surface, border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model.artwork, null, Modifier.size(if (model.compact) 92.dp else 120.dp).clip(RoundedCornerShape(10.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("CH ${(model.queueIndex + 1).toString().padStart(2, '0')}", style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace)), color = amber)
                        TrackTitles(model, actions, titleStyle = MaterialTheme.typography.titleLarge)
                        Text(model.trackFormat.ifBlank { "Stereo" }, style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                    }
                }
                // Meters: left and right off the live signal, a spectrum between them.
                Row(Modifier.fillMaxWidth().weight(1f).padding(top = 14.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF111214)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    StereoMeter(model.isPlaying, amber, Color(0xFFE5533D), Modifier.width(44.dp).fillMaxHeight())
                    LevelBars(model.isPlaying, model.reduceMotion, amber, Modifier.weight(1f).fillMaxHeight(), bars = 14, gap = 4.dp, rounded = false, segments = 16)
                }
                Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("L  R", Modifier.width(44.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, textAlign = TextAlign.Center)
                    Text("50 Hz … 16 kHz", Modifier.weight(1f).padding(start = 14.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant)
                }
            }
        }
        // Fader
        Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(clock(model.position.positionMs), style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace)), color = amber)
                Text(clock(model.durationMs), style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant)
            }
            Fader(model, actions)
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Led("SHUF", model.shuffleEnabled, actions.toggleShuffle, Modifier.weight(1f))
            IconButton(actions.previous, enabled = model.hasPrevious) { Icon(SkinIcons.SkipPrevious, "Previous", Modifier.size(30.dp)) }
            PlayPauseButton(model, actions.playPause, Modifier.size(64.dp), amber, Color(0xFF1A1200))
            IconButton(actions.next, enabled = model.hasNext) { Icon(SkinIcons.SkipNext, "Next", Modifier.size(30.dp)) }
            Led(if (model.repeatMode == 1) "RPT1" else "RPT", model.repeatMode != 0, actions.cycleRepeat, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Led(if (model.liked) "LIKED" else "LIKE", model.liked, actions.toggleLike, Modifier.weight(1f))
            Led("OUT", false, actions.openOutput, Modifier.weight(1f))
            Led("MENU", false, actions.openMenu, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Fader(model: NowPlayingModel, actions: NowPlayingActions) {
    val c = MaterialTheme.colorScheme
    var draft by remember { mutableStateOf<Float?>(null) }
    val f = draft ?: model.fraction
    Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
        SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.fillMaxWidth(), track = Color(0xFF111214), fill = c.primary.copy(alpha = .5f), thickness = 10.dp, thumbRadius = 0.dp, onDraft = { draft = it })
        // Fader cap, following the position; touches go to the bar underneath.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Box(Modifier.offset(x = (maxWidth - 28.dp) * f).size(28.dp, 40.dp).shadow(4.dp, RoundedCornerShape(5.dp)).clip(RoundedCornerShape(5.dp)).background(Color(0xFF3A3D43)).border(1.dp, Color(0xFF55595F), RoundedCornerShape(5.dp))) {
                Box(Modifier.align(Alignment.Center).width(20.dp).height(2.dp).background(Color(0xFFEDEDED)))
            }
        }
    }
}

@Composable
private fun Led(label: String, on: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = modifier.height(40.dp), shape = RoundedCornerShape(8.dp), color = c.surfaceVariant, contentColor = c.onSurface, border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (on) c.primary else c.outlineVariant).then(if (on) Modifier.shadow(6.dp, CircleShape, spotColor = c.primary) else Modifier))
            Spacer(Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace)))
        }
    }
}
