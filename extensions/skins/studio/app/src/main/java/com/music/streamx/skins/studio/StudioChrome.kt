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

@Composable internal fun StudioHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val mono = skinFont(FontFamily.Monospace)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 10.dp)) {
        // A rack unit: screws in the corners, a label strip, LED buttons.
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
            Box {
                listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd).forEach { Box(Modifier.align(it).padding(6.dp).size(7.dp).clip(CircleShape).background(c.outlineVariant)) }
                Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("SESSION", style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono, letterSpacing = 3.sp), color = c.primary)
                            Text(account?.name ?: "Guest engineer", style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        StereoMeter(true, c.primary, Color(0xFFE5533D), Modifier.size(28.dp, 40.dp), segments = 10)
                        Surface(onClick = onAccount, shape = RoundedCornerShape(6.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant), modifier = Modifier.padding(start = 12.dp).size(40.dp)) {
                            if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account", Modifier.size(18.dp), tint = c.primary) }
                        }
                    }
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HIST" to onHistory, "LOCAL" to onLocal, "XPLR" to onExplore).forEach { (label, action) ->
                            Surface(onClick = action, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(6.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(c.primary)); Spacer(Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable internal fun StudioBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    SkinDock(shape = RoundedCornerShape(12.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Surface(onClick = { onTabSelected(index) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(6.dp), color = c.surfaceVariant,
                    contentColor = if (selected) c.onSurface else c.onSurfaceVariant, border = BorderStroke(1.dp, c.outlineVariant)) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(tab.icon, tab.label, Modifier.size(18.dp))
                        Text(tab.label.uppercase(), Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1)
                        Box(Modifier.padding(top = 3.dp).width(18.dp).height(3.dp).clip(CircleShape).background(if (selected) c.primary else c.outlineVariant).then(if (selected) Modifier.shadow(6.dp, CircleShape, spotColor = c.primary) else Modifier))
                    }
                }
            }
        }
    }
}

@Composable internal fun StudioMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Column {
            Row(Modifier.clickable(onClick = onExpand).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StereoMeter(playing, c.primary, Color(0xFFE5533D), Modifier.size(22.dp, 44.dp), segments = 12)
                AsyncImage(artwork, null, Modifier.padding(start = 10.dp).size(44.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onPlayPause) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, null, tint = c.primary) }
                IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next") }
            }
            Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFF111214))) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(c.primary.copy(alpha = .7f))) }
        }
    }
}
