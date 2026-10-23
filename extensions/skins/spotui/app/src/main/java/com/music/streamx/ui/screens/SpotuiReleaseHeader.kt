package com.music.streamx.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.ui.skins.SkinIcon as Icon
import com.music.streamx.ui.skins.SpotuiIcons

@Composable
internal fun SpotuiReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit,
    onSearch: () -> Unit, onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: androidx.compose.ui.unit.Dp) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 16.dp)) {
        AsyncImage(page.thumbnailUrl.artworkAt(HEADER_ART_PX), null, contentScale = ContentScale.Crop,
            modifier = Modifier.align(Alignment.CenterHorizontally).size(240.dp).clip(RoundedCornerShape(6.dp)))
        Text(page.title, Modifier.padding(top = 24.dp), style = MaterialTheme.typography.headlineLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Text(page.subtitle, Modifier.padding(top = 8.dp).clickable {
            songs.firstOrNull()?.let { song -> song.artistId?.let { onArtist(it, song.artist) } }
        }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text("${songs.size} songs", Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (page.library != null && onToggleLibrary != null) IconButton(onClick = onToggleLibrary) {
                Icon(if (page.library!!.saved) SpotuiIcons.Check else SpotuiIcons.Add, "Save to library", Modifier.size(26.dp))
            }
            IconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, "Search playlist", Modifier.size(24.dp)) }
            if (onMore != null) IconButton(onClick = { onMore(songs) }) { Icon(SpotuiIcons.More, "More", Modifier.size(24.dp)) }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onShuffle, enabled = songs.isNotEmpty()) { Icon(SpotuiIcons.Shuffle, "Shuffle", Modifier.size(24.dp)) }
            FilledIconButton(onClick = onPlay, enabled = songs.isNotEmpty(), modifier = Modifier.size(56.dp), shape = CircleShape) {
                Icon(SpotuiIcons.Play, "Play", Modifier.size(26.dp))
            }
        }
    }
}
