package com.music.streamx.skins.wave

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
 * A desktop media player from the dial-up years, redrawn gently: a wide
 * spectrum panel, a monospaced readout and chunky buttons, in a muted
 * phosphor green that stays easy on the eyes.
 */
@Composable
internal fun WaveStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val lcd = c.primary
    val mono = skinFont(FontFamily.Monospace)
    Column(modifier.padding(horizontal = 20.dp)) {
        Panel(Modifier.fillMaxWidth().weight(1f)) {
            // A player window: title strip, the cover as big as a square will fit, then the LCD readout.
            Box(Modifier.fillMaxSize().padding(12.dp)) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) { i -> Box(Modifier.padding(end = 5.dp).size(8.dp).clip(RoundedCornerShape(2.dp)).background(if (i == 0) lcd else c.outlineVariant)) }
                        Text("now_playing.wav", Modifier.weight(1f).padding(start = 6.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = c.onSurfaceVariant, maxLines = 1)
                        Text("${(model.queueIndex + 1).toString().padStart(2, '0')}/${model.queue.size.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = lcd.copy(alpha = .8f))
                    }
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // Square, and as large as fits whichever way the space runs out first.
                        AsyncImage(model.artwork, null, Modifier.aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(c.surfaceVariant)
                            .border(1.dp, c.outlineVariant, RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            Text(model.song.title, style = MaterialTheme.typography.titleMedium.copy(fontFamily = mono), color = lcd, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(model.song.artist, style = MaterialTheme.typography.bodySmall.copy(fontFamily = mono), color = lcd.copy(alpha = .7f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(onClick = actions.openArtist))
                        }
                        Text(clock(model.position.positionMs), Modifier.padding(start = 10.dp), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = mono, fontWeight = FontWeight.Bold), color = lcd)
                    }
                    LevelBars(model.isPlaying, model.reduceMotion, lcd, Modifier.fillMaxWidth().height(if (model.compact) 36.dp else 52.dp).padding(top = 8.dp), bars = 28, gap = 3.dp, rounded = false)
                    Text(listOfNotNull(model.trackFormat.takeIf { it.isNotBlank() }, if (model.shuffleEnabled) "shuffle" else null, if (model.repeatMode != 0) "repeat" else null).joinToString("  ").ifBlank { "stereo" },
                        Modifier.fillMaxWidth().padding(top = 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono), color = lcd.copy(alpha = .7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Panel(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                SeekBar(model.fraction, !model.locked, actions.seekFraction, track = c.surfaceVariant, fill = lcd, thickness = 10.dp, thumbRadius = 0.dp)
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Key(SkinIcons.SkipPrevious, "Previous", actions.previous, Modifier.weight(1f), model.hasPrevious)
                    Key(if (model.isPlaying) SkinIcons.Pause else SkinIcons.PlayArrow, "Play", actions.playPause, Modifier.weight(1.5f), true, accent = true, loading = model.isLoading)
                    Key(SkinIcons.SkipNext, "Next", actions.next, Modifier.weight(1f), model.hasNext)
                    Key(SkinIcons.Shuffle, "Shuffle", actions.toggleShuffle, Modifier.weight(1f), true, lit = model.shuffleEnabled)
                    Key(if (model.repeatMode == 1) SkinIcons.RepeatOne else SkinIcons.Repeat, "Repeat", actions.cycleRepeat, Modifier.weight(1f), true, lit = model.repeatMode != 0)
                }
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Key(if (model.liked) SkinIcons.Favorite else SkinIcons.FavoriteBorder, "Like", actions.toggleLike, Modifier.weight(1f), true, lit = model.liked)
                    Key(SkinIcons.SpeakerGroup, "Output", actions.openOutput, Modifier.weight(1f), true)
                    Key(SkinIcons.MoreHoriz, "Options", actions.openMenu, Modifier.weight(1f), true)
                }
            }
        }
    }
}

@Composable
private fun Panel(modifier: Modifier, content: @Composable () -> Unit) {
    val c = MaterialTheme.colorScheme
    Surface(modifier, shape = RoundedCornerShape(10.dp), color = c.surface, border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant)) { content() }
}

@Composable
private fun Key(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier, enabled: Boolean, accent: Boolean = false, lit: Boolean = false, loading: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.height(44.dp), shape = RoundedCornerShape(6.dp),
        color = if (accent) c.primary else c.surfaceVariant, contentColor = if (accent) c.onPrimary else if (lit) c.primary else c.onSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (lit) c.primary else c.outlineVariant)) {
        Box(Modifier.alpha(if (enabled) 1f else .4f), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = c.onPrimary) else Icon(icon, label, Modifier.size(20.dp))
        }
    }
}
