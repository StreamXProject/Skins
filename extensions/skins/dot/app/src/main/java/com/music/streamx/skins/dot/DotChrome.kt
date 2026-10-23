package com.music.streamx.skins.dot

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.SkinLayout
import com.music.streamx.ui.skins.rememberSkinRemoteArtwork
import com.music.streamx.ui.skins.skinPlayerSettings

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
internal fun DotHomeHeader(
    account: Account?,
    onAccount: () -> Unit,
    onLocal: () -> Unit,
    onHistory: () -> Unit,
    onExplore: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(acc),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "dot / dear music, thanks.",
                style = MaterialTheme.typography.labelMedium,
                color = acc,
                modifier = Modifier.weight(1f),
            )
            TuiKey(label = "[ USER ]", onClick = onAccount)
        }

        CursorTitle(
            text = "READY",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 10.dp),
        )

        HorizontalDivider(color = c.outline)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TuiKey(label = "[ HISTORY ]", modifier = Modifier.weight(1f), onClick = onHistory)
            TuiKey(label = "[ ON DEVICE ]", modifier = Modifier.weight(1f), onClick = onLocal)
            TuiKey(label = "[ EXPLORE ]", modifier = Modifier.weight(1f), onClick = onExplore)
        }
    }
}

@Composable
internal fun DotBrowseHeader(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onExplore: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(acc),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "dot",
            style = MaterialTheme.typography.titleMedium,
            color = c.onBackground,
            modifier = Modifier.widthIn(min = 44.dp),
            maxLines = 1,
            softWrap = false,
        )
        Spacer(Modifier.width(8.dp))
        Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        TuiKey(label = "[ / ]", onClick = onSearch)
        Spacer(Modifier.width(6.dp))
        TuiKey(label = "[ CFG ]", onClick = onSettings)
        Spacer(Modifier.width(6.dp))
        TuiKey(label = "[ EXPLORE ]", onClick = onExplore)
        }
    }
}

@Composable
internal fun DotBottomNavigation(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val c = MaterialTheme.colorScheme
    Surface(color = c.background, tonalElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            HorizontalDivider(color = c.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tabs.forEachIndexed { index, tab ->
                    val active = index == selectedIndex
                    TuiKey(
                        label = "[ ${tab.label.uppercase()} ]",
                        accent = active,
                        fill = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(index) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun DotMiniPlayer(
    song: Song,
    playing: Boolean,
    loading: Boolean,
    position: PlaybackPosition,
    durationMs: Long,
    saved: Boolean,
    onSave: () -> Unit,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    locked: Boolean,
    onBlocked: () -> Unit,
    modifier: Modifier,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color
    val fraction = if (durationMs > 0) (position.positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val art = rememberSkinRemoteArtwork(song)

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .border(1.dp, c.outline, Edge),
        color = c.surface,
        contentColor = c.onSurface,
        shape = Edge,
    ) {
        Column {
            Hairline(fraction = fraction, color = acc)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DotArtworkThumbnail(
                    model = art ?: song.thumbnailUrl,
                    cols = 36,
                    playing = playing,
                    wave = DotOptions.waveSweep,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, c.outline, Edge)
                        .background(c.surfaceVariant)
                        .tuiClickable(onExpand),
                )
                Spacer(Modifier.width(10.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .tuiClickable(onExpand)
                        .padding(end = 6.dp),
                ) {
                    CursorTitle(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "${song.artist} · ${formatTime(position.positionMs)}/${formatTime(durationMs)}".lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                TuiKey(
                    label = if (saved) "[*]" else "[+]",
                    accent = saved,
                    onClick = { if (locked) onBlocked() else onSave() },
                )
                Spacer(Modifier.width(6.dp))
                TuiKey(
                    label = if (playing) "||" else "|>",
                    onClick = { if (locked) onBlocked() else onPlayPause() },
                )
                Spacer(Modifier.width(6.dp))
                TuiKey(
                    label = ">>|",
                    onClick = { if (locked) onBlocked() else onNext() },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.dotFeedShelves(
    shelves: List<HomeShelf>,
    layout: SkinLayout,
    onClick: (ShelfItem, String) -> Unit,
    onLongPress: ((ShelfItem) -> Unit)?,
    shelfBrowseId: (HomeShelf) -> String?,
) {
    shelves.forEachIndexed { section, shelf ->
        item(key = "dot-heading-$section") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "// ${(section + 1).toString().padStart(2, '0')} / ${shelf.title.uppercase()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                shelfBrowseId(shelf)?.let {
                    TuiKey(label = "[ > ]", onClick = { onClick(ShelfItem(shelf.title, "", null, null, it), shelf.title) })
                }
            }
        }
        item(key = "dot-shelf-$section") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 14.dp),
            ) {
                itemsIndexed(shelf.items) { index, item ->
                    val c = MaterialTheme.colorScheme
                    Column(
                        modifier = Modifier
                            .width(148.dp)
                            .border(1.dp, c.outline, Edge)
                            .background(c.surface)
                            .combinedClickable(
                                onClick = { onClick(item, shelf.title) },
                                onLongClick = { onLongPress?.invoke(item) },
                            )
                            .padding(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .border(1.dp, c.outlineVariant, Edge),
                        ) {
                            DotArtworkThumbnail(
                                model = item.thumbnailUrl,
                                cols = 44,
                                playing = false,
                                wave = false,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Text(
                                text = (index + 1).toString().padStart(2, '0'),
                                style = MaterialTheme.typography.labelSmall,
                                color = DotOptions.accent.color,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(c.background)
                                    .border(1.dp, c.outline, Edge)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            text = item.subtitle.lowercase().ifBlank { " " },
                            style = MaterialTheme.typography.labelSmall,
                            color = c.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DotReleaseHeader(
    page: DetailPage,
    songs: List<Song>,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onSearch: () -> Unit,
    onMore: ((List<Song>) -> Unit)?,
    onArtist: (String, String) -> Unit,
    onToggleLibrary: (() -> Unit)?,
    topPadding: Dp,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .border(1.dp, c.outline, Edge),
            ) {
                DotArtworkThumbnail(
                    model = page.thumbnailUrl,
                    cols = 56,
                    playing = false,
                    wave = false,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "// ${songs.size} TRACKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = acc,
                )
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = page.subtitle.lowercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TuiKey(label = "[ PLAY ]", bright = true, modifier = Modifier.weight(1f), onClick = onPlay)
            TuiKey(label = "[ SHUFFLE ]", modifier = Modifier.weight(1f), onClick = onShuffle)
            onToggleLibrary?.let { TuiKey(label = "[ SAVE ]", onClick = it) }
            onMore?.let { TuiKey(label = "[ ... ]", onClick = { it(songs) }) }
        }
        HorizontalDivider(color = c.outline)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DotLibraryRow(
    item: ShelfItem,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!item.thumbnailUrl.isNullOrBlank()) {
            DotArtworkThumbnail(
                model = item.thumbnailUrl,
                cols = 36,
                playing = false,
                wave = false,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, c.outline, Edge)
                    .background(c.surfaceVariant),
            )
            Spacer(Modifier.width(12.dp))
        } else {
            Text(
                text = ">",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = acc,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subtitle.lowercase().ifBlank { " " },
                style = MaterialTheme.typography.labelSmall,
                color = c.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "[ > ]",
            style = MaterialTheme.typography.labelSmall,
            color = c.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = c.outline)
}

@Composable
internal fun DotLibrary(
    shelves: List<HomeShelf>,
    links: List<ShelfItem>,
    deviceItems: List<ShelfItem>,
    contentPadding: PaddingValues,
    onItem: (ShelfItem, String) -> Unit,
    onLongPress: ((ShelfItem) -> Unit)?,
    onShowAll: (HomeShelf) -> Unit,
    onNewPlaylist: (() -> Unit)?,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    LazyColumn(contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "// YOUR COLLECTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = acc,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CursorTitle(
                        text = "LIBRARY",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f),
                    )
                    onNewPlaylist?.let {
                        TuiKey(label = "[ + PLAYLIST ]", onClick = it)
                    }
                }
                HorizontalDivider(Modifier.padding(top = 10.dp), color = c.outline)
            }
        }
        items(links) { item ->
            DotLibraryRow(item, { onItem(item, "Library") }, { onLongPress?.invoke(item) })
        }
        shelves.forEach { shelf ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "// ${shelf.title.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    TuiKey(label = "[ ALL ]", onClick = { onShowAll(shelf) })
                }
            }
            items(shelf.items.take(6)) { item ->
                DotLibraryRow(item, { onItem(item, shelf.title) }, { onLongPress?.invoke(item) })
            }
        }
        if (deviceItems.isNotEmpty()) {
            item {
                Text(
                    text = "// ON THIS DEVICE",
                    style = MaterialTheme.typography.labelLarge,
                    color = acc,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
            items(deviceItems) { item ->
                DotLibraryRow(item, { onItem(item, "On device") }, { onLongPress?.invoke(item) })
            }
        }
    }
}

@Composable
internal fun DotBottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (withMiniPlayer) 76.dp else 28.dp)
            .background(Brush.verticalGradient(listOf(Color.Transparent, pageColor))),
    )
}

@Composable
internal fun DotLibraryStats(onOpen: () -> Unit) {
    TuiKey(
        label = "[ VIEW STATS & HISTORY ]",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onOpen,
    )
}
