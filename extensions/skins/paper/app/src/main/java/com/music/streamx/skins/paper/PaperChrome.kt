package com.music.streamx.skins.paper

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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

@Composable internal fun PaperHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val serif = skinFont(FontFamily.Serif)
    val date = remember { java.text.SimpleDateFormat("EEEE, d MMMM", java.util.Locale.getDefault()).format(java.util.Date()) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 10.dp, bottom = 10.dp)) {
        // A masthead: name, a double rule, the dateline.
        Text("THE DAILY LISTEN", Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = serif, letterSpacing = 4.sp), textAlign = TextAlign.Center, maxLines = 1)
        Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(2.dp).background(c.onSurface)); Box(Modifier.fillMaxWidth().padding(top = 2.dp).height(1.dp).background(c.onSurface))
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(date, style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif), color = c.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text(account?.name ?: "Sign in", Modifier.clickable(onClick = onAccount), style = MaterialTheme.typography.labelMedium.copy(fontFamily = serif, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline), color = c.onSurface, maxLines = 1)
        }
        Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(1.dp).background(c.outlineVariant))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Recently read" to onHistory, "On this device" to onLocal, "Browse the archive" to onExplore).forEach { (label, action) ->
                Text(label, Modifier.clickable(onClick = action).padding(vertical = 4.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = serif, fontStyle = FontStyle.Italic), color = c.onSurfaceVariant)
            }
        }
    }
}

@Composable internal fun PaperBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val c = MaterialTheme.colorScheme
    // Running heads along a hairline: no icons, the current section underlined.
    // A slip of paper laid on the page: hairline border, a whisper of shadow.
    SkinDock(shape = RoundedCornerShape(2.dp), color = c.background, border = BorderStroke(1.dp, c.onSurface), elevation = 4.dp) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    Text(tab.label.uppercase(), Modifier.clickable { onTabSelected(index) }.padding(horizontal = 8.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Serif), letterSpacing = 2.sp,
                            textDecoration = if (selected) androidx.compose.ui.text.style.TextDecoration.Underline else null),
                        color = if (selected) c.onSurface else c.onSurfaceVariant, maxLines = 1)
                }
            }
        }
    }
}

@Composable internal fun PaperMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val serif = skinFont(FontFamily.Serif)
    Column(modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().background(c.surface).border(1.dp, c.onSurface, RoundedCornerShape(2.dp))) {
        Row(Modifier.clickable(onClick = onExpand).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(artwork, null, Modifier.size(44.dp).border(1.dp, c.onSurface).padding(3.dp), contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = serif), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, style = MaterialTheme.typography.bodySmall.copy(fontFamily = serif, fontStyle = FontStyle.Italic), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TextButton(onPlayPause) { if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = c.onSurface) else Text(if (playing) "Pause" else "Play", style = MaterialTheme.typography.labelLarge.copy(fontFamily = serif), color = c.onSurface) }
            TextButton(onNext) { Text("Next", style = MaterialTheme.typography.labelLarge.copy(fontFamily = serif), color = c.onSurfaceVariant) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceVariant)) { Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(c.onSurface)) }
    }
}
