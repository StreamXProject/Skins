package com.music.streamx.skins.vinyl

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable internal fun VinylHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val spin = rememberSpin(true, false, rpm = 8f)
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("THE CRATE", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp), color = c.primary)
                Text(account?.name?.let { "${it.substringBefore(' ')}'s records" } ?: "Your records", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = skinFont(FontFamily.Serif)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            // A little record for the account: the sleeve is the avatar.
            Box(Modifier.size(56.dp).clickable(onClick = onAccount), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().spin(spin).clip(CircleShape).background(Color(0xFF151312)))
                if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                else Box(Modifier.size(24.dp).clip(CircleShape).background(c.primary), contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account", Modifier.size(14.dp), tint = c.onPrimary) }
            }
        }
        // Sleeve tabs, like dividers in a crate.
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Recently spun" to onHistory, "On device" to onLocal, "Dig deeper" to onExplore).forEach { (label, action) ->
                Surface(onClick = action, shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 3.dp, bottomEnd = 3.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                    Text(label, Modifier.padding(horizontal = 12.dp, vertical = 9.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Serif)))
                }
            }
        }
    }
}

@Composable internal fun VinylBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    val reduce by com.music.streamx.ui.skins.skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    // The same flat, hairline-edged surface as the crate's chips and the deck above it.
    // The current section gets a small record that spins beside its name; the rest stay quiet.
    SkinDock(shape = RoundedCornerShape(22.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant), elevation = 10.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val spin = rememberSpin(selected, reduce, rpm = 12f)
                val tint by animateColorAsState(if (selected) c.primary else c.onSurfaceVariant, tween(if (reduce) 0 else 220), label = "tab tint")
                Row(Modifier.weight(if (selected) 1.5f else 1f).height(48.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (selected) c.primary.copy(alpha = .14f) else Color.Transparent)
                    .clickable { onTabSelected(index) }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    if (selected) {
                        Box(Modifier.size(26.dp).graphicsLayer { rotationZ = spin }, contentAlignment = Alignment.Center) {
                            GroovedDisc(Modifier.fillMaxSize())
                            Box(Modifier.size(11.dp).clip(CircleShape).background(c.primary))
                            Box(Modifier.size(2.dp).clip(CircleShape).background(Color(0xFF111010)))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(tab.label, style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Serif)), color = tint, maxLines = 1)
                    } else Icon(tab.icon, tab.label, Modifier.size(22.dp), tint = tint)
                }
            }
        }
    }
}

/** A black disc with grooves and a sheen; the label and spin are the caller's. */
@Composable internal fun GroovedDisc(modifier: Modifier) {
    Canvas(modifier) {
        val r = size.minDimension / 2
        drawCircle(Color(0xFF111010), r)
        var g = r * .38f
        while (g < r * .97f) { drawCircle(Color.White.copy(alpha = .05f), g, style = Stroke(.8f)); g += r * .055f }
        drawCircle(Brush.sweepGradient(listOf(Color.Transparent, Color.White.copy(alpha = .14f), Color.Transparent, Color.White.copy(alpha = .10f), Color.Transparent)), r)
        drawCircle(Color(0xFF2B2724), r, style = Stroke(1.2f))
    }
}

/**
 * A little turntable: the record spins with the cover as its label and the
 * tonearm travels in as the side plays. Same surface as the dock below it.
 */
@Composable internal fun VinylMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val reduce by com.music.streamx.ui.skins.skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val spin = rememberSpin(playing, reduce, rpm = 33.3f)
    val arm by animateFloatAsState(if (playing) 22f + 10f * fraction.coerceIn(0f, 1f) else 6f, tween(if (reduce) 0 else 600, easing = FastOutSlowInEasing), label = "tonearm")
    Surface(modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = c.surface,
        border = BorderStroke(1.dp, c.outlineVariant), shadowElevation = 8.dp) {
        Box {
            Row(Modifier.fillMaxWidth().clickable(onClick = onExpand).padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp)) {
                    Box(Modifier.fillMaxSize().padding(2.dp).graphicsLayer { rotationZ = spin }) {
                        GroovedDisc(Modifier.fillMaxSize())
                        AsyncImage(artwork, null, Modifier.align(Alignment.Center).fillMaxSize(.5f).clip(CircleShape).background(c.primary), contentScale = ContentScale.Crop)
                        Box(Modifier.align(Alignment.Center).size(3.dp).clip(CircleShape).background(Color(0xFF111010)))
                    }
                    VinylTonearm(arm, Modifier.fillMaxSize(), miniature = true)
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Serif)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(onClick = onPlayPause, shape = CircleShape, color = Color.Transparent, contentColor = c.primary, border = BorderStroke(1.5.dp, c.primary), modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = c.primary)
                        else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, if (playing) "Pause" else "Play", Modifier.size(22.dp))
                    }
                }
                IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next", tint = c.onSurface) }
            }
            // How far into the side: a hairline along the foot of the deck.
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 4.dp).height(2.dp).clip(CircleShape).background(c.outlineVariant)) {
                Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(c.primary))
            }
        }
    }
}
