package com.music.streamx.skins.cassette

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

@Composable internal fun CassetteHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 12.dp)) {
        // A mixtape label: lined paper, a hand-written title, the side marker.
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
            Column {
                Box(Modifier.fillMaxWidth().height(10.dp).background(c.primary))
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("MIXTAPE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 3.sp), color = c.onSurfaceVariant)
                        Text(account?.name?.let { "for ${it.substringBefore(' ')}" } ?: "for you", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        repeat(2) { Box(Modifier.fillMaxWidth().padding(top = 8.dp).height(1.dp).background(c.outlineVariant)) }
                    }
                    Surface(onClick = onAccount, shape = CircleShape, color = c.surfaceVariant, modifier = Modifier.size(42.dp)) {
                        if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Box(contentAlignment = Alignment.Center) { Text("A", style = MaterialTheme.typography.titleLarge, color = c.primary, fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Triple("Rewind", SkinIcons.History, onHistory), Triple("On device", SkinIcons.FolderOpen, onLocal), Triple("Explore", SkinIcons.Explore, onExplore)).forEach { (label, icon, action) ->
                Surface(onClick = action, modifier = Modifier.weight(1f).height(42.dp), shape = RoundedCornerShape(8.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant), shadowElevation = 2.dp) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(16.dp), tint = c.primary); Spacer(Modifier.width(6.dp)); Text(label, style = MaterialTheme.typography.labelLarge) }
                }
            }
        }
    }
}

@Composable internal fun CassetteBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    // Deck keys: the pressed one sits lower and lights its LED.
    SkinDock(shape = RoundedCornerShape(14.dp), color = c.surface, border = BorderStroke(1.dp, c.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Surface(onClick = { onTabSelected(index) }, modifier = Modifier.weight(1f).height(if (selected) 50.dp else 52.dp), shape = RoundedCornerShape(8.dp),
                    color = c.surfaceVariant, contentColor = if (selected) c.primary else c.onSurfaceVariant, shadowElevation = if (selected) 0.dp else 4.dp, border = BorderStroke(1.dp, c.outlineVariant)) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(if (selected) c.primary else c.outlineVariant))
                        Icon(tab.icon, tab.label, Modifier.padding(top = 3.dp).size(20.dp))
                        Text(tab.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable internal fun CassetteMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outlineVariant), shadowElevation = 4.dp) {
        Column {
            Row(Modifier.clickable(onClick = onExpand).padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(artwork, null, Modifier.size(44.dp).clip(RoundedCornerShape(5.dp)).background(c.surface), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Monospace), fontStyle = FontStyle.Italic), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(onClick = onPlayPause, shape = RoundedCornerShape(8.dp), color = c.primary, contentColor = c.onPrimary, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = c.onPrimary)
                        else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, if (playing) "Pause" else "Play")
                    }
                }
                IconButton(onNext) { Icon(SkinIcons.FastForward, "Next", tint = c.onSurface) }
            }
            // The tape counter's strip: how far through the side we are.
            Box(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 8.dp).height(3.dp).clip(CircleShape).background(c.outlineVariant)) {
                Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).fillMaxHeight().background(c.primary))
            }
        }
    }
}
