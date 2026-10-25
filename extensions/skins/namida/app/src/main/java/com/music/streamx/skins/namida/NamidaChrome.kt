package com.music.streamx.skins.namida

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Account
import com.music.streamx.data.model.DetailPage
import com.music.streamx.data.model.HomeShelf
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.data.model.Song
import com.music.streamx.data.model.artworkAt
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.SkinIcon
import com.music.streamx.ui.skins.SkinLayout

// ---------------------------------------------------------------- navigation

/** The host's tab glyph, swapped for Namida's line icon of the same meaning. */
internal fun tabIcon(tab: BottomTab): ImageVector {
    val name = tab.icon.name.lowercase()
    return when {
        "home" in name -> NamidaIcons.Home
        "explore" in name -> NamidaIcons.Explore
        "library" in name -> NamidaIcons.Library
        "search" in name -> NamidaIcons.Search
        "sensor" in name || "radio" in name -> NamidaIcons.Radio
        else -> namidaIcon(tab.icon)
    }
}

/**
 * Namida's bar: flat, every tab an icon, the chosen one sat on a pill with its
 * name underneath. The pill and the name grow in rather than snapping.
 */
@Composable
internal fun NamidaBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().background(t.card).navigationBarsPadding().height(72.dp).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            val pill by animateDpAsState(if (selected) 64.dp else 40.dp, spring(dampingRatio = .7f, stiffness = 500f), label = "namidaTabPill")
            val fill by animateColorAsState(if (selected) t.indicator else Color.Transparent, tween(220), label = "namidaTabFill")
            Column(Modifier.weight(1f).fillMaxHeight()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(pill, 34.dp).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
                    SkinIcon(tabIcon(tab), tab.label, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = if (selected) .9f else .7f))
                }
                AnimatedVisibility(selected, enter = fadeIn(tween(200)) + expandVertically(), exit = fadeOut(tween(120)) + shrinkVertically()) {
                    Text(tab.label, Modifier.padding(top = 4.dp), color = t.onBackground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- headers

@Composable
internal fun NamidaBrowseHeader(account: Account?, onSearch: () -> Unit, onSettings: () -> Unit, onExplore: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        HeaderIcon(NamidaIcons.Menu, "Explore", onExplore)
        Spacer(Modifier.weight(1f))
        HeaderIcon(NamidaIcons.Search, "Search", onSearch)
        HeaderIcon(NamidaIcons.Settings, "Settings", onSettings)
        if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, account.name, contentScale = ContentScale.Crop,
            modifier = Modifier.padding(start = 6.dp).size(30.dp).clip(CircleShape).background(t.card).clickable(onClick = onSettings))
    }
}

/** Namida's home title: the app's name, large, with the page's two shortcuts beside it. */
@Composable
internal fun NamidaHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("StreamX", Modifier.weight(1f), color = t.onBackground.copy(alpha = .82f), fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        HeaderIcon(NamidaIcons.Folder, "On device", onLocal)
        HeaderIcon(NamidaIcons.History, "History", onHistory)
        Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onAccount), contentAlignment = Alignment.Center) {
            if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", contentScale = ContentScale.Crop,
                modifier = Modifier.size(28.dp).clip(CircleShape))
            else SkinIcon(NamidaIcons.Account, "Account", Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .8f))
        }
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    val t = LocalNamidaTones.current
    Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        SkinIcon(icon, label, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .8f))
    }
}

// ---------------------------------------------------------------- home feed

/** Namida's home sections: a heading with its icon, then a row of cards; the first row large, as Mixes are. */
internal fun LazyListScope.namidaFeedShelves(shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
    onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) {
    shelves.forEachIndexed { index, shelf ->
        item(key = "namida-shelf:$index:${shelf.title}") {
            val t = LocalNamidaTones.current
            val more = shelfBrowseId(shelf)?.let { id -> { onClick(ShelfItem(shelf.title, shelf.subtitle, null, null, id), shelf.title) } }
            Column(Modifier.padding(top = if (index == 0) 8.dp else 22.dp)) {
                Row(Modifier.fillMaxWidth().then(if (more != null) Modifier.clickable(onClick = more) else Modifier)
                    .padding(start = 20.dp, end = 12.dp, top = 6.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    SkinIcon(sectionIcon(index, shelf.title), null, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .75f))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(shelf.title, color = t.onBackground.copy(alpha = .85f), fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (shelf.subtitle.isNotBlank()) Text(shelf.subtitle, color = t.tertiaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (more != null) SkinIcon(NamidaIcons.ChevronRight, "Show all", Modifier.size(22.dp), tint = t.secondaryText)
                }
                // Namida's three kinds of row: the wide Mixes, the tiled Recent
                // Listens, and the bare covers Top Recents runs.
                val name = shelf.title.lowercase()
                val style = when {
                    index == 0 -> 0
                    "recent" in name || "again" in name || "history" in name || "played" in name -> 1
                    else -> 2
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(if (style == 0) 12.dp else 10.dp)) {
                    items(shelf.items) { entry ->
                        val open = { onClick(entry, shelf.title) }
                        val hold = onLongPress?.let { { it(entry) } }
                        when (style) {
                            0 -> MixCard(entry, open, hold)
                            1 -> NamidaCard(entry, layout.shelfWidthDp, open, hold)
                            else -> CoverCard(entry, layout.shelfWidthDp, open, hold)
                        }
                    }
                }
            }
        }
    }
}

private fun sectionIcon(index: Int, title: String): ImageVector {
    val name = title.lowercase()
    return when {
        "recent" in name || "again" in name || "history" in name -> NamidaIcons.History
        "mix" in name -> NamidaIcons.Playlists
        "artist" in name -> NamidaIcons.Artists
        "album" in name || "release" in name -> NamidaIcons.Albums
        "video" in name -> NamidaIcons.Video
        "radio" in name || "station" in name -> NamidaIcons.Radio
        else -> listOf(NamidaIcons.Tracks, NamidaIcons.Albums, NamidaIcons.Playlists, NamidaIcons.Artists)[index % 4]
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MixCard(item: ShelfItem, onClick: () -> Unit, onLongPress: (() -> Unit)?) {
    val t = LocalNamidaTones.current
    Column(Modifier.width(300.dp).combinedClickable(onClick = onClick, onLongClick = onLongPress)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.65f).shadow(8.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).background(t.card)) {
            AsyncImage(item.thumbnailUrl.artworkAt(720), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Text(item.title, Modifier.padding(start = 4.dp, top = 8.dp), color = t.onBackground.copy(alpha = .85f), fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (item.subtitle.isNotBlank()) Text(item.subtitle, Modifier.padding(start = 4.dp), color = t.tertiaryText, fontSize = 12.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Namida's small card: the cover on top, the words below it, all on one tinted tile. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NamidaCard(item: ShelfItem, widthDp: Int, onClick: () -> Unit, onLongPress: (() -> Unit)?) {
    val t = LocalNamidaTones.current
    Column(Modifier.width(widthDp.dp).clip(RoundedCornerShape(12.dp)).background(t.card)
        .border(1.dp, t.onBackground.copy(alpha = .06f), RoundedCornerShape(12.dp))
        .combinedClickable(onClick = onClick, onLongClick = onLongPress)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(t.cardRaised)) {
            AsyncImage(item.thumbnailUrl.artworkAt(480), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Column(Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
            Text(item.title, color = t.onBackground.copy(alpha = .82f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle.ifBlank { " " }, color = t.tertiaryText, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** A cover on its own with the words under it, as Namida's Top Recents row. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CoverCard(item: ShelfItem, widthDp: Int, onClick: () -> Unit, onLongPress: (() -> Unit)?) {
    val t = LocalNamidaTones.current
    Column(Modifier.width(widthDp.dp).combinedClickable(onClick = onClick, onLongClick = onLongPress)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).shadow(6.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(t.card)) {
            AsyncImage(item.thumbnailUrl.artworkAt(480), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Text(item.title, Modifier.padding(start = 2.dp, top = 6.dp), color = t.onBackground.copy(alpha = .82f), fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (item.subtitle.isNotBlank()) Text(item.subtitle, Modifier.padding(start = 2.dp), color = t.tertiaryText, fontSize = 11.sp,
            fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------------------------------------------------------- library rows

/** Namida's tile: square cover with a soft shadow, three lines of text, the menu dots at the end. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NamidaLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().padding(vertical = 1.dp).background(t.card.copy(alpha = .6f))
        .combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(58.dp).shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp)).background(t.cardRaised)) {
            AsyncImage(item.thumbnailUrl.artworkAt(240), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(item.title, color = t.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onLongPress), contentAlignment = Alignment.Center) {
            SkinIcon(NamidaIcons.MoreVertical, "More", Modifier.size(20.dp), tint = t.secondaryText)
        }
    }
}

// ---------------------------------------------------------------- album / playlist header

/**
 * Namida's subpage header: a strip of the page's covers across the top fading
 * into the page, the page's own cover overlapping it with the title beside, and
 * the round-cornered action buttons underneath.
 */
@Composable
internal fun NamidaReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit, onSearch: () -> Unit,
    onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: androidx.compose.ui.unit.Dp) {
    val t = LocalNamidaTones.current
    val strip = songs.mapNotNull { it.thumbnailUrl }.distinct().take(5).ifEmpty { listOfNotNull(page.thumbnailUrl) }
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(topPadding + 200.dp)) {
            Row(Modifier.fillMaxWidth().height(topPadding + 150.dp)) {
                strip.forEach { url ->
                    AsyncImage(url.artworkAt(480), null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }
            Box(Modifier.fillMaxWidth().height(topPadding + 150.dp)
                .background(Brush.verticalGradient(0f to t.background.copy(alpha = .15f), .55f to t.background.copy(alpha = .45f), 1f to t.background)))
            Row(Modifier.align(Alignment.BottomStart).padding(start = 22.dp, end = 16.dp), verticalAlignment = Alignment.Bottom) {
                Box(Modifier.size(150.dp).shadow(16.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(t.card)) {
                    if (page.thumbnailUrl != null || strip.size < 4)
                        AsyncImage((page.thumbnailUrl ?: strip.firstOrNull()).artworkAt(720), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Column(Modifier.fillMaxSize()) {
                        strip.take(4).chunked(2).forEach { pair ->
                            Row(Modifier.weight(1f)) { pair.forEach { AsyncImage(it.artworkAt(240), null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight()) } }
                        }
                    }
                }
                Column(Modifier.weight(1f).padding(start = 16.dp, bottom = 34.dp)) {
                    Text(page.title, color = t.onBackground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    if (page.subtitle.isNotBlank()) Text(page.subtitle, Modifier.padding(top = 2.dp).clickable {
                        songs.firstOrNull()?.let { s -> s.artistId?.let { onArtist(it, s.artist) } }
                    }, color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(if (songs.size == 1) "1 Track" else "${songs.size} Tracks", Modifier.padding(top = 2.dp), color = t.secondaryText,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
            page.library?.let { library -> onToggleLibrary?.let { toggle ->
                ActionButton(if (library.saved) NamidaIcons.HeartFilled else NamidaIcons.Heart, if (library.saved) "Remove from library" else "Save to library", toggle)
            } }
            ActionButton(NamidaIcons.Search, "Search", onSearch)
            ActionButton(NamidaIcons.Shuffle, "Shuffle", onShuffle, enabled = songs.isNotEmpty())
            ActionButton(NamidaIcons.Play, "Play", onPlay, enabled = songs.isNotEmpty())
            onMore?.let { more -> ActionButton(NamidaIcons.More, "More", { more(songs) }) }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    val t = LocalNamidaTones.current
    Box(Modifier.size(64.dp, 46.dp).clip(RoundedCornerShape(16.dp)).background(t.card).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center) {
        SkinIcon(icon, label, Modifier.size(22.dp), tint = t.onBackground.copy(alpha = if (enabled) .8f else .3f))
    }
}

// ---------------------------------------------------------------- odds and ends

@Composable
internal fun NamidaBottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) {
    Box(modifier.fillMaxWidth().height(if (withMiniPlayer) 170.dp else 96.dp)
        .background(Brush.verticalGradient(0f to Color.Transparent, .6f to pageColor.copy(alpha = .7f), 1f to pageColor)))
}

@Composable
internal fun NamidaLibraryStats(onOpen: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(12.dp)).background(t.card).clickable(onClick = onOpen)
        .padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        SkinIcon(NamidaIcons.Equalizer, null, Modifier.size(20.dp), tint = t.onBackground.copy(alpha = .8f))
        Text("Statistics", Modifier.padding(start = 10.dp), color = t.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Namida's sleep timer: a card of chips, picked then started. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NamidaSleepTimer(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>, onStart: (Int) -> Unit,
    onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) {
    val t = LocalNamidaTones.current
    var picked by remember { mutableIntStateOf(minutes ?: presets.firstOrNull() ?: 30) }
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(t.background).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkinIcon(NamidaIcons.Timer, null, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .8f))
                Text("Sleep timer", Modifier.padding(start = 12.dp), color = t.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            if (running) Text(if (afterTrack) "Stops after this track" else "Stops in ${minutes ?: picked} min", Modifier.padding(top = 10.dp),
                color = t.secondaryText, fontSize = 13.sp)
            FlowRow(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { value -> Chip("$value min", value == picked && !afterTrack) { picked = value } }
                Chip("After this track", afterTrack) { onAfterTrack() }
            }
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                if (running) DialogButton("Stop", false) { onCancel(); onDismiss() }
                DialogButton("Cancel", false, onDismiss)
                DialogButton("Start", true) { onStart(picked); onDismiss() }
            }
        }
    }
}

@Composable
private fun DialogButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val t = LocalNamidaTones.current
    Text(label, Modifier.clip(RoundedCornerShape(14.dp)).background(if (primary) t.indicator else t.card).clickable(onClick = onClick)
        .padding(horizontal = 18.dp, vertical = 10.dp), color = t.onBackground, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
}
