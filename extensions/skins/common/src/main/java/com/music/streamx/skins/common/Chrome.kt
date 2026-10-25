package com.music.streamx.skins.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.model.HomeShelf
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.ui.skins.SkinLayout
import com.music.streamx.ui.skins.skinLyricsPosition

/**
 * Keeps a scrolling list's leftover motion inside the player: without this the
 * host's sheet treats the end of a list scroll or fling as a drag on itself
 * and the page appears to stick or collapse.
 */
val PlayerScrollBoundary = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = Offset(0f, available.y)
    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = Velocity(0f, available.y)
}

/**
 * Timed lyrics drawn in the skin's own colours: the active line in the
 * foreground colour, the rest muted, auto-following the playhead until the
 * listener scrolls and for a few seconds after.
 */
@Composable
fun LyricsPage(lines: List<LyricLine>?, unavailable: Boolean, positionMs: Long, offsetMs: Int, reduceMotion: Boolean,
    onSeek: (Long) -> Unit, modifier: Modifier = Modifier, serif: Boolean = false, centered: Boolean = false) {
    val c = MaterialTheme.colorScheme
    if (lines.isNullOrEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (unavailable) "No lyrics for this one." else "Finding the words…", color = c.onSurfaceVariant)
        }
        return
    }
    val state = rememberLazyListState()
    val active = lines.indexOfLast { it.timeMs <= skinLyricsPosition(positionMs, offsetMs) }.coerceAtLeast(0)
    var userScrolledAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state.isScrollInProgress) { if (state.isScrollInProgress) userScrolledAt = System.currentTimeMillis() }
    LaunchedEffect(active) {
        if (System.currentTimeMillis() - userScrolledAt < 4000) return@LaunchedEffect
        if (reduceMotion) state.scrollToItem(active, -200) else state.animateScrollToItem(active, -200)
    }
    val family = if (serif) skinFont(FontFamily.Serif) else FontFamily.Default
    LazyColumn(state = state, modifier = modifier.fillMaxSize().nestedScroll(PlayerScrollBoundary),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        itemsIndexed(lines) { index, line ->
            val color by animateColorAsState(if (index == active) c.onBackground else c.onSurfaceVariant.copy(alpha = if (index < active) .45f else .7f), tween(if (reduceMotion) 0 else 260), label = "line")
            Text(line.text.ifBlank { "♪" }, Modifier.fillMaxWidth().clickable { onSeek(line.timeMs) }.padding(vertical = 4.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = family), color = color,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start)
        }
        item { Spacer(Modifier.height(160.dp)) }
    }
}

/** How a skin lays out the home feed's cards. */
enum class ShelfStyle {
    /** Square cover, rounded, title under. */ CARDS,
    /** Round covers, like records on a shelf. */ DISCS,
    /** Tall 3:4 cards with a soft shadow. */ POSTERS,
    /** Cassette J-cards: the cover folded round a coloured spine. */ TAPES,
    /** White-bordered instant prints at a slight lean. */ PRINTS,
    /** Square, sharp-cornered, hairline-bordered tiles with serif captions. */ TILES,
    /** Dense numbered rows, like a device list. */ LIST,
    /** Covers on frosted cards. */ GLASS,
    /** Covers in a dark bezel with a monospaced readout. */ LCD,
    /** Covers in a graphite channel with a status LED. */ STRIP,
}

/** Shelf headings and cards for [BaseSkinRenderer.FeedShelves], one look per [ShelfStyle]. */
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.styledShelves(style: SkinStyle, shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
    onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) {
    shelves.forEachIndexed { section, shelf ->
        item(key = "heading-$section") {
            val c = MaterialTheme.colorScheme
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    when (style.shelf) {
                        ShelfStyle.LCD, ShelfStyle.STRIP -> Text("${(section + 1).toString().padStart(2, '0')} · ${shelf.title.uppercase()}", style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        ShelfStyle.TILES -> Text(shelf.title, style = MaterialTheme.typography.titleLarge.copy(fontFamily = skinFont(FontFamily.Serif)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        ShelfStyle.PRINTS -> Text(shelf.title, style = MaterialTheme.typography.titleLarge.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        else -> Text(shelf.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (shelf.subtitle.isNotBlank()) Text(shelf.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1)
                }
                shelfBrowseId(shelf)?.let { id -> TextButton({ onClick(ShelfItem(shelf.title, "", null, null, id), shelf.title) }) { Text("More") } }
            }
        }
        if (style.shelf == ShelfStyle.LIST) {
            itemsIndexed(shelf.items.take(6), key = { i, it -> "row-$section-$i-${it.browseId ?: it.videoId ?: it.title}" }) { i, item ->
                val c = MaterialTheme.colorScheme
                Row(Modifier.fillMaxWidth().combinedClickable(onClick = { onClick(item, shelf.title) }, onLongClick = { onLongPress?.invoke(item) }).padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text((i + 1).toString().padStart(2, '0'), Modifier.width(28.dp), style = MaterialTheme.typography.labelMedium.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.primary)
                    AsyncImage(item.thumbnailUrl, null, Modifier.size(44.dp).clip(RoundedCornerShape(layout.artworkRadiusDp.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            item(key = "gap-$section") { Spacer(Modifier.height(14.dp)) }
        } else item(key = "shelf-$section") {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(if (style.shelf == ShelfStyle.TILES) 8.dp else 12.dp), modifier = Modifier.padding(bottom = 22.dp)) {
                itemsIndexed(shelf.items) { i, item ->
                    ShelfCard(style, item, i, layout, Modifier.combinedClickable(onClick = { onClick(item, shelf.title) }, onLongClick = { onLongPress?.invoke(item) }))
                }
            }
        }
    }
}

/**
 * One feed card in the skin's [ShelfStyle]. [fill] makes it take the width it
 * is given (a grid cell on a browse page) instead of the shelf width.
 */
@Composable
internal fun ShelfCard(style: SkinStyle, item: ShelfItem, index: Int, layout: SkinLayout, modifier: Modifier, fill: Boolean = false) {
    val c = MaterialTheme.colorScheme
    val w = layout.shelfWidthDp.dp
    // Every card below sizes itself with `.width(...)`; in a grid it fills the cell instead.
    fun Modifier.cardWidth(width: androidx.compose.ui.unit.Dp): Modifier = if (fill) fillMaxWidth() else widthIn(min = width, max = width)
    val r = RoundedCornerShape(layout.artworkRadiusDp.dp)
    @Composable fun caption(color: Color = c.onBackground, family: FontFamily = FontFamily.Default, center: Boolean = false) {
        Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = family), color = color, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), textAlign = if (center) TextAlign.Center else TextAlign.Start)
        if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall.copy(fontFamily = family), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(), textAlign = if (center) TextAlign.Center else TextAlign.Start)
    }
    when (style.shelf) {
        ShelfStyle.CARDS -> Column(modifier.cardWidth(w)) {
            AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxWidth().aspectRatio(1f).clip(r).background(c.surfaceVariant), contentScale = ContentScale.Crop); caption()
        }
        ShelfStyle.DISCS -> Column(modifier.cardWidth(w), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF151312)))
                AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxSize(.72f).clip(CircleShape).background(c.primary), contentScale = ContentScale.Crop)
                Box(Modifier.size(6.dp).clip(CircleShape).background(c.surfaceVariant))
            }
            caption(center = true)
        }
        ShelfStyle.POSTERS -> Column(modifier.cardWidth(w)) {
            Box(Modifier.fillMaxWidth().aspectRatio(.78f).shadow(14.dp, r, spotColor = c.primary).clip(r).background(c.surfaceVariant)) {
                AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .7f)), startY = 200f)))
                Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        ShelfStyle.TAPES -> Column(modifier.cardWidth(w)) {
            // A J-card insert: a coloured spine with the side and length, the cover folded beside it.
            Row(Modifier.fillMaxWidth().aspectRatio(1.12f).shadow(6.dp, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant)) {
                Column(Modifier.fillMaxHeight().width(20.dp).background(c.primary).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween) {
                    Text(if (index % 2 == 0) "A" else "B", style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onPrimary)
                    Text("60", style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onPrimary.copy(alpha = .8f))
                }
                Box(Modifier.fillMaxHeight().width(1.dp).background(Color.Black.copy(alpha = .35f)))
                AsyncImage(item.thumbnailUrl, null, Modifier.weight(1f).fillMaxHeight().background(c.surface), contentScale = ContentScale.Crop)
            }
            Text(item.title, Modifier.padding(top = 8.dp).fillMaxWidth(), style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ShelfStyle.PRINTS -> Column(modifier.cardWidth(w).rotate(if (index % 2 == 0) -2f else 2f).shadow(8.dp, RoundedCornerShape(3.dp)).background(printPaper(), RoundedCornerShape(3.dp)).padding(8.dp, 8.dp, 8.dp, 12.dp)) {
            AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFE9E4DB)), contentScale = ContentScale.Crop)
            Text(item.title, Modifier.padding(top = 8.dp).fillMaxWidth(), style = MaterialTheme.typography.titleSmall.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic), color = Color(0xFF2E2A26), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
        ShelfStyle.TILES -> Column(modifier.cardWidth(w)) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).border(1.dp, c.onSurface, RoundedCornerShape(2.dp)).padding(5.dp)) {
                AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxSize().background(c.surfaceVariant), contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(.15f) }))
            }
            caption(family = skinFont(FontFamily.Serif))
        }
        ShelfStyle.GLASS -> Column(modifier.cardWidth(w).clip(RoundedCornerShape(layout.artworkRadiusDp.dp + 6.dp)).background(c.surface.copy(alpha = .6f)).border(1.dp, Color.White.copy(alpha = .35f), RoundedCornerShape(layout.artworkRadiusDp.dp + 6.dp)).padding(8.dp)) {
            AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxWidth().aspectRatio(1f).clip(r).background(c.surfaceVariant), contentScale = ContentScale.Crop); caption()
        }
        ShelfStyle.LCD -> Column(modifier.cardWidth(w).clip(RoundedCornerShape(8.dp)).background(c.surface).border(1.dp, c.outlineVariant, RoundedCornerShape(8.dp)).padding(6.dp)) {
            AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            Text(item.title, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ShelfStyle.STRIP -> Row(modifier.cardWidth(w + 60.dp).clip(RoundedCornerShape(8.dp)).background(c.surface).border(1.dp, c.outlineVariant, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(if (index == 0) c.primary else c.outlineVariant))
            AsyncImage(item.thumbnailUrl, null, Modifier.padding(start = 8.dp).size(56.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text("CH ${(index + 1).toString().padStart(2, '0')}", style = MaterialTheme.typography.labelSmall.copy(fontFamily = skinFont(FontFamily.Monospace)), color = c.primary)
                Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        ShelfStyle.LIST -> Unit
    }
}
