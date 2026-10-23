package com.music.streamx.ui.skins

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*

/** Compact shelves and recent shortcuts, adapted from Spotui's Home feed. */
fun LazyListScope.spotuiFeedShelves(
    shelves: List<HomeShelf>,
    layout: SkinLayout,
    onClick: (ShelfItem, String) -> Unit,
    onLongPress: ((ShelfItem) -> Unit)?,
    shelfBrowseId: (HomeShelf) -> String?,
) {
    val shortcuts = shelves.firstOrNull()?.items.orEmpty().take(8)
    items(shortcuts.chunked(2)) { row ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { entry ->
                Row(Modifier.weight(1f).clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(onClick = { onClick(entry, shelves.first().title) }, onLongClick = onLongPress?.let { { it(entry) } }),
                    verticalAlignment = Alignment.CenterVertically) {
                    SkinArtwork(entry, Modifier.size(52.dp))
                    Text(entry.title, Modifier.padding(8.dp).weight(1f), style = MaterialTheme.typography.titleSmall,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
    shelves.forEachIndexed { index, shelf ->
        item(key = "skin-shelf:$index") {
            Column(Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(shelf.title, Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    shelfBrowseId(shelf)?.let { id ->
                        androidx.compose.material3.TextButton(onClick = {
                            onClick(ShelfItem(shelf.title, shelf.subtitle.orEmpty(), null, null, id), shelf.title)
                        }) { Text("Show all") }
                    }
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(shelf.items) { entry ->
                        Column(Modifier.width(layout.shelfWidthDp.dp).combinedClickable(
                            onClick = { onClick(entry, shelf.title) }, onLongClick = onLongPress?.let { { it(entry) } })) {
                            SkinArtwork(entry, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(layout.artworkRadiusDp.dp)))
                            Text(entry.title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpotuiLibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
    Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SkinArtwork(item, Modifier.size(56.dp).clip(RoundedCornerShape(LocalSkinLayout.current.artworkRadiusDp.dp)))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SkinArtwork(item: ShelfItem, modifier: Modifier) {
    AsyncImage(model = item.thumbnailUrl.artworkAt(CARD_ART_PX), contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
}
