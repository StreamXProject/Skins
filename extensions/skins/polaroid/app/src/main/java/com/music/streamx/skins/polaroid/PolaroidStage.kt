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

/**
 * An instant photo pinned to a pastel wall. The cover is the picture, the
 * caption is the title in a hand-written line, and the print straightens up
 * while a track plays and settles back to a lean when it pauses.
 */
@Composable
internal fun PolaroidStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val lean by animateFloatAsState(if (model.isPlaying) -1.5f else -4f, tween(if (model.reduceMotion) 0 else 700, easing = FastOutSlowInEasing), label = "lean")
    val paper = printPaper()
    Column(modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxWidth(.82f).rotate(lean).shadow(16.dp, RoundedCornerShape(4.dp)).background(paper, RoundedCornerShape(4.dp)).padding(14.dp, 14.dp, 14.dp, 20.dp)) {
                AsyncImage(model.artwork, null, Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFE9E4DB)), contentScale = ContentScale.Crop)
                Text(model.song.title, Modifier.fillMaxWidth().padding(top = 16.dp), style = MaterialTheme.typography.titleLarge.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), color = Color(0xFF2E2A26),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                Text(model.song.artist, Modifier.fillMaxWidth().clickable(onClick = actions.openArtist), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = skinFont(FontFamily.Cursive)), color = Color(0xFF6B655E),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            }
            // A bit of washi tape holding the print up.
            Box(Modifier.align(Alignment.TopCenter).padding(top = 6.dp).rotate(-6f).size(74.dp, 22.dp).background(c.secondary.copy(alpha = .7f), RoundedCornerShape(2.dp)))
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(clock(model.position.positionMs), style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant)
            SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.weight(1f).padding(horizontal = 12.dp), track = c.surfaceVariant, fill = c.primary, thickness = 8.dp, thumbRadius = 0.dp)
            Text(clock(model.durationMs), style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill(SkinIcons.Shuffle, "Shuffle", actions.toggleShuffle, model.shuffleEnabled)
            Pill(SkinIcons.SkipPrevious, "Previous", actions.previous, false, enabled = model.hasPrevious)
            PlayPauseButton(model, actions.playPause, Modifier.weight(1f).height(56.dp))
            Pill(SkinIcons.SkipNext, "Next", actions.next, false, enabled = model.hasNext)
            Pill(if (model.repeatMode == 1) SkinIcons.RepeatOne else SkinIcons.Repeat, "Repeat", actions.cycleRepeat, model.repeatMode != 0)
        }
        SecondaryRow(model, actions, Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun Pill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, active: Boolean, enabled: Boolean = true) {
    val c = MaterialTheme.colorScheme
    Surface(onClick = onClick, enabled = enabled, shape = CircleShape, color = if (active) c.primary.copy(alpha = .2f) else c.surfaceVariant,
        contentColor = if (active) c.primary else c.onSurfaceVariant, modifier = Modifier.size(50.dp).alpha(if (enabled) 1f else .4f)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, label, Modifier.size(22.dp)) }
    }
}
