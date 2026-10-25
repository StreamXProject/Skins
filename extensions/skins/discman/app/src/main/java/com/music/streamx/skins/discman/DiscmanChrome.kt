package com.music.streamx.skins.discman

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

@Composable internal fun DiscmanHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 12.dp)) {
        // The top LCD: who is in, what disc is loaded.
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = Color(0xFF17201B), border = BorderStroke(1.dp, c.outlineVariant)) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DISC  ●  ${account?.name?.substringBefore(' ')?.uppercase() ?: "GUEST"}", color = Color(0xFF9FE1B8), style = MaterialTheme.typography.titleMedium.copy(fontFamily = mono), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("ESP  ·  ANTI-SKIP  ·  READY", color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelSmall.copy(fontFamily = mono))
                }
                Surface(onClick = onAccount, shape = CircleShape, color = Color(0xFF243029), modifier = Modifier.size(38.dp)) {
                    if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account", Modifier.size(18.dp), tint = Color(0xFF9FE1B8)) }
                }
            }
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("HISTORY" to onHistory, "DEVICE" to onLocal, "EXPLORE" to onExplore).forEach { (label, action) ->
                Surface(onClick = action, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(18.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                    Box(contentAlignment = Alignment.Center) { Text(label, style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono, letterSpacing = 1.sp), color = c.onSurface) }
                }
            }
        }
    }
}

@Composable internal fun DiscmanBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    // A remote-control strip, pill shaped like the player's own buttons.
    SkinDock(shape = RoundedCornerShape(32.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    Surface(onClick = { onTabSelected(index) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp),
                        color = if (selected) Color(0xFF17201B) else c.surfaceVariant, contentColor = if (selected) Color(0xFF9FE1B8) else c.onSurfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.icon, tab.label, Modifier.size(18.dp))
                            if (selected) { Spacer(Modifier.width(6.dp)); Text(tab.label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1) }
                        }
                    }
                }
            }
        }
    }
}

@Composable internal fun DiscmanMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val spin = rememberSpin(playing, false, rpm = 50f)
    Surface(modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.clickable(onClick = onExpand).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).spin(spin), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) { drawCircle(Brush.sweepGradient(listOf(Color(0xFFE9E4F5), Color(0xFFCFE9F7), Color(0xFFDDF2E1), Color(0xFFF9EBD5), Color(0xFFE9E4F5))), size.minDimension / 2) }
                AsyncImage(artwork, null, Modifier.fillMaxSize(.62f).clip(CircleShape), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize(.16f).clip(CircleShape).background(c.surfaceVariant))
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF17201B)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(song.title, color = Color(0xFF9FE1B8), style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(song.artist, color = Color(0xFF5E9A74), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    LevelBars(playing, false, Color(0xFF9FE1B8), Modifier.width(30.dp).height(10.dp), bars = 6, gap = 2.dp, rounded = false)
                }
                Box(Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp).background(Color(0xFF2A3A30))) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Color(0xFF9FE1B8))) }
            }
            IconButton(onPlayPause) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, null, tint = c.primary) }
            IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next") }
        }
    }
}
