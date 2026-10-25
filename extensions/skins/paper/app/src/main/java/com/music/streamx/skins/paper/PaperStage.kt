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

/**
 * Ink on paper. Warm off-white, a serif, artwork printed in halftone grey and
 * controls set as type with hairline rules — the e-reader of music players.
 */
@Composable
internal fun PaperStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val serif = skinFont(FontFamily.Serif)
    val mono = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    Column(modifier.padding(horizontal = 28.dp)) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth(.78f).aspectRatio(1f).border(1.dp, c.onSurface, RoundedCornerShape(2.dp)).padding(8.dp)) {
                    AsyncImage(model.artwork, null, Modifier.fillMaxSize().background(c.surfaceVariant), contentScale = ContentScale.Crop, colorFilter = mono)
                }
                Text("№ ${model.queueIndex + 1} of ${model.queue.size}", Modifier.padding(top = 14.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif, letterSpacing = 2.sp), color = c.onSurfaceVariant)
            }
        }
        Text(model.song.title, style = MaterialTheme.typography.headlineMedium.copy(fontFamily = serif), maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(model.song.artist, Modifier.fillMaxWidth().padding(top = 4.dp).clickable(onClick = actions.openArtist), style = MaterialTheme.typography.bodyLarge.copy(fontFamily = serif, fontStyle = FontStyle.Italic),
            color = c.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        HorizontalDivider(Modifier.padding(top = 18.dp), color = c.onSurface, thickness = 1.dp)
        SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.padding(top = 2.dp), track = c.surfaceVariant, fill = c.onSurface, thickness = 2.dp, thumbRadius = 5.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(clock(model.position.positionMs), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif), color = c.onSurfaceVariant)
            Text(model.trackFormat.ifBlank { "—" }, style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif), color = c.onSurfaceVariant, maxLines = 1)
            Text(clock(model.durationMs), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif), color = c.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            InkButton(if (model.shuffleEnabled) "Shuffled" else "Shuffle", actions.toggleShuffle, model.shuffleEnabled)
            IconButton(actions.previous, enabled = model.hasPrevious) { Icon(SkinIcons.SkipPrevious, "Previous", Modifier.size(30.dp), tint = c.onSurface) }
            Surface(onClick = actions.playPause, shape = CircleShape, color = c.onSurface, contentColor = c.surface, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (model.isLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = c.surface)
                    else Icon(if (model.isPlaying) SkinIcons.Pause else SkinIcons.PlayArrow, null, Modifier.size(32.dp))
                }
            }
            IconButton(actions.next, enabled = model.hasNext) { Icon(SkinIcons.SkipNext, "Next", Modifier.size(30.dp), tint = c.onSurface) }
            InkButton(when (model.repeatMode) { 1 -> "Once"; 2 -> "Repeat"; else -> "Repeat" }, actions.cycleRepeat, model.repeatMode != 0)
        }
        HorizontalDivider(Modifier.padding(top = 10.dp), color = c.outlineVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InkButton(if (model.liked) "♥ Kept" else "♡ Keep", actions.toggleLike, model.liked)
            InkButton("Output", actions.openOutput, false)
            InkButton("More…", actions.openMenu, false)
        }
    }
}

@Composable
private fun InkButton(label: String, onClick: () -> Unit, active: Boolean) {
    val c = MaterialTheme.colorScheme
    TextButton(onClick, contentPadding = PaddingValues(horizontal = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Serif), textDecoration = if (active) androidx.compose.ui.text.style.TextDecoration.Underline else null),
            color = if (active) c.onSurface else c.onSurfaceVariant)
    }
}
