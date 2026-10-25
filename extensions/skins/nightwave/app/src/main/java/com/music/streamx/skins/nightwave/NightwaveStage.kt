package com.music.streamx.skins.nightwave

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
 * Built for the dark. True black behind two slow, soft blooms of the artwork's
 * own colour, large type, generous spacing and nothing bright enough to make
 * you squint at 2am.
 */
@Composable
internal fun NightwaveStage(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val seed = com.music.streamx.ui.skins.rememberSkinArtworkColor(model.artwork) ?: c.primary
    val glow by animateColorAsState(seed.copy(alpha = .35f), tween(if (model.reduceMotion) 0 else 1200), label = "glow")
    val drift by rememberInfiniteTransition(label = "drift").animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse), label = "d")
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val d = if (model.reduceMotion) .5f else drift
            drawCircle(Brush.radialGradient(listOf(glow, Color.Transparent), center = Offset(size.width * (.2f + .3f * d), size.height * .25f), radius = size.width * .7f),
                size.width * .7f, Offset(size.width * (.2f + .3f * d), size.height * .25f))
            drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = .22f), Color.Transparent), center = Offset(size.width * (.9f - .3f * d), size.height * .7f), radius = size.width * .6f),
                size.width * .6f, Offset(size.width * (.9f - .3f * d), size.height * .7f))
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                val scale by animateFloatAsState(if (model.isPlaying) 1f else .92f, tween(if (model.reduceMotion) 0 else 500, easing = FastOutSlowInEasing), label = "cover")
                AsyncImage(model.artwork, null, Modifier.fillMaxWidth(.86f * scale).aspectRatio(1f).shadow(30.dp, RoundedCornerShape(28.dp), ambientColor = seed, spotColor = seed)
                    .clip(RoundedCornerShape(28.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrackTitles(model, actions, Modifier.weight(1f), titleStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold))
                IconButton(actions.toggleLike) { Icon(if (model.liked) SkinIcons.Favorite else SkinIcons.FavoriteBorder, "Like", tint = if (model.liked) seed else c.onSurfaceVariant) }
            }
            SeekBar(model.fraction, !model.locked, actions.seekFraction, Modifier.padding(top = 14.dp), track = c.surfaceVariant, fill = seed, thickness = 6.dp, thumbRadius = 0.dp)
            TimeRow(model)
            TransportRow(model, actions, Modifier.padding(top = 6.dp), accent = seed, onAccent = if (seed.luminance() > .5f) Color.Black else Color.White, playSize = 72.dp)
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(actions.openOutput, Modifier.weight(1f)) {
                    Icon(SkinIcons.SpeakerGroup, null, Modifier.size(16.dp), tint = c.onSurfaceVariant); Spacer(Modifier.width(6.dp))
                    Text(model.trackFormat.ifBlank { "Audio output" }, style = MaterialTheme.typography.labelMedium, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(actions.openMenu) { Icon(SkinIcons.MoreHoriz, "Options", tint = c.onSurfaceVariant) }
            }
        }
    }
}
