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

@Composable internal fun NightwaveHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; in 17..21 -> "Good evening"; else -> "Still up?" }
    Box(Modifier.fillMaxWidth()) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Brush.radialGradient(listOf(c.primary.copy(alpha = .28f), Color.Transparent), center = Offset(size.width * .15f, 0f), radius = size.width * .6f), size.width * .6f, Offset(size.width * .15f, 0f))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 14.dp, bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
                    Text(account?.name ?: "Lights low, volume up.", style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(onClick = onAccount, shape = CircleShape, color = c.surfaceVariant, modifier = Modifier.size(46.dp), border = BorderStroke(1.dp, c.primary.copy(alpha = .4f))) {
                    if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account", tint = c.primary) }
                }
            }
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Triple("Recent", SkinIcons.History, onHistory), Triple("Device", SkinIcons.FolderOpen, onLocal), Triple("Explore", SkinIcons.Explore, onExplore)).forEach { (label, icon, action) ->
                    Surface(onClick = action, shape = RoundedCornerShape(22.dp), color = c.surfaceVariant.copy(alpha = .7f), border = BorderStroke(1.dp, c.surfaceVariant)) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(16.dp), tint = c.primary); Spacer(Modifier.width(8.dp)); Text(label, style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }
        }
    }
}

@Composable internal fun NightwaveBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    // A floating island, icons only, a glowing dot under the current tab.
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 40.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(32.dp), color = c.surfaceVariant.copy(alpha = .92f), border = BorderStroke(1.dp, c.surfaceVariant), shadowElevation = 12.dp) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    Column(Modifier.clip(CircleShape).clickable { onTabSelected(index) }.size(52.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(tab.icon, tab.label, Modifier.size(22.dp), tint = if (selected) c.primary else c.onSurfaceVariant)
                        Box(Modifier.padding(top = 4.dp).size(5.dp).clip(CircleShape).background(if (selected) c.primary else Color.Transparent).then(if (selected) Modifier.shadow(8.dp, CircleShape, spotColor = c.primary) else Modifier))
                    }
                }
            }
        }
    }
}

@Composable internal fun NightwaveMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val seed = com.music.streamx.ui.skins.rememberSkinArtworkColor(artwork) ?: c.primary
    Surface(modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = c.surfaceVariant.copy(alpha = .92f), border = BorderStroke(1.dp, seed.copy(alpha = .4f)), shadowElevation = 10.dp) {
        Column {
            Row(Modifier.clickable(onClick = onExpand).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(artwork, null, Modifier.size(48.dp).shadow(10.dp, CircleShape, spotColor = seed).clip(CircleShape), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onPlayPause) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = seed) else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, null, tint = seed) }
                IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next") }
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 0.dp).padding(bottom = 6.dp).height(3.dp).clip(CircleShape).background(c.surface)) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(seed)) }
        }
    }
}
