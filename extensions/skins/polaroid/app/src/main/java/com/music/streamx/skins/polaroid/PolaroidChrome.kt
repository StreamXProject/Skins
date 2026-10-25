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

@Composable internal fun PolaroidHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 10.dp, bottom = 12.dp)) {
        // A note pinned with tape; the account photo is a tiny print clipped to it.
        Box(Modifier.fillMaxWidth()) {
            Surface(Modifier.fillMaxWidth().padding(top = 8.dp).rotate(-1f), shape = RoundedCornerShape(3.dp), color = printPaper(), shadowElevation = 6.dp) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(account?.name?.let { "${it.substringBefore(' ')}'s snapshots" } ?: "Snapshots", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), color = Color(0xFF2E2A26), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("what's been on repeat lately", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = skinFont(FontFamily.Cursive)), color = Color(0xFF6B655E))
                    }
                    Column(Modifier.rotate(4f).clickable(onClick = onAccount).shadow(4.dp, RoundedCornerShape(2.dp)).background(printPaper(), RoundedCornerShape(2.dp)).padding(4.dp, 4.dp, 4.dp, 10.dp)) {
                        if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.size(40.dp), contentScale = ContentScale.Crop)
                        else Box(Modifier.size(40.dp).background(c.secondary.copy(alpha = .45f)), contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account", tint = Color(0xFF2E2A26)) }
                    }
                }
            }
            Box(Modifier.align(Alignment.TopCenter).rotate(-5f).size(70.dp, 20.dp).background(c.secondary.copy(alpha = .7f), RoundedCornerShape(2.dp)))
        }
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(Triple("Recent", SkinIcons.History, onHistory), Triple("Device", SkinIcons.FolderOpen, onLocal), Triple("Explore", SkinIcons.Explore, onExplore)).forEachIndexed { i, (label, icon, action) ->
                // Round stickers in alternating pastels.
                Surface(onClick = action, shape = CircleShape, color = listOf(c.primary.copy(alpha = .24f), c.secondary.copy(alpha = .32f), c.surfaceVariant)[i], modifier = Modifier.weight(1f).height(44.dp)) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(16.dp), tint = c.onSurface); Spacer(Modifier.width(6.dp)); Text(label, style = MaterialTheme.typography.labelLarge, color = c.onSurface) }
                }
            }
        }
    }
}

@Composable internal fun PolaroidBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    // A dark contact-sheet strip taped over the page, in the mini player's tone; the current tab is developed into a tiny print.
    Box(Modifier.fillMaxWidth()) {
        SkinDock(Modifier.padding(top = 10.dp), shape = RoundedCornerShape(6.dp), color = c.surfaceVariant, border = BorderStroke(1.dp, c.outline.copy(alpha = .25f)), elevation = 12.dp, horizontal = 18.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    val lift by animateDpAsState(if (selected) (-5).dp else 0.dp, label = "tab lift")
                    Column(Modifier.offset(y = lift).clip(RoundedCornerShape(6.dp)).clickable { onTabSelected(index) }.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.rotate(if (selected) (if (index % 2 == 0) -4f else 4f) else 0f)
                            .then(if (selected) Modifier.shadow(5.dp, RoundedCornerShape(2.dp)).background(printPaper(), RoundedCornerShape(2.dp)).padding(3.dp, 3.dp, 3.dp, 9.dp) else Modifier)) {
                            Box(Modifier.size(34.dp).background(if (selected) c.primary else Color.Transparent), contentAlignment = Alignment.Center) {
                                Icon(tab.icon, tab.label, Modifier.size(20.dp), tint = if (selected) c.onPrimary else c.onSurfaceVariant)
                            }
                        }
                        Text(tab.label, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Cursive)),
                            color = if (selected) c.onSurface else c.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
        // Washi tape holding the strip down, in the secondary colour.
        Box(Modifier.align(Alignment.TopCenter).padding(top = 2.dp).rotate(-3f).size(64.dp, 18.dp).background(c.secondary.copy(alpha = .7f), RoundedCornerShape(2.dp)))
    }
}

@Composable internal fun PolaroidMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 14.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = c.surfaceVariant) {
        Row(Modifier.clickable(onClick = onExpand).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.rotate(-3f).shadow(4.dp, RoundedCornerShape(2.dp)).background(printPaper(), RoundedCornerShape(2.dp)).padding(3.dp, 3.dp, 3.dp, 8.dp)) {
                AsyncImage(artwork, null, Modifier.size(38.dp), contentScale = ContentScale.Crop)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.fillMaxWidth().padding(top = 5.dp).height(5.dp).clip(CircleShape).background(c.surface)) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(c.primary)) }
            }
            Surface(onClick = onPlayPause, shape = CircleShape, color = c.primary, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = c.onPrimary) else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, null, tint = c.onPrimary) }
            }
            IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next") }
        }
    }
}
