package com.music.streamx.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Song
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.skins.SkinIcon as Icon
import com.music.streamx.ui.skins.*

@Composable
fun SpotuiMiniPlayer(song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long,
    saved: Boolean, onSave: () -> Unit, onPlayPause: () -> Unit, onExpand: () -> Unit,
    onNext: () -> Unit, onPrevious: () -> Unit, locked: Boolean, onBlocked: () -> Unit, modifier: Modifier = Modifier) {
    val art = rememberSkinRemoteArtwork(song)
    val palette = rememberArtworkColors(art)
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val fill by animateColorAsState(lerp(com.music.streamx.ui.skins.spotuiArtworkTint(palette.colors), Color.Black, .12f),
        tween(if (reduce) 0 else 500), label = "spotuiMiniArtworkColor")
    Column(modifier.padding(horizontal = 8.dp).clip(RoundedCornerShape(8.dp)).background(fill)
        .pointerInput(locked) {
            var total = 0f
            detectHorizontalDragGestures(onDragStart = { total = 0f }, onDragEnd = {
                if (kotlin.math.abs(total) > 120f) { if (locked) onBlocked() else if (total < 0) onNext() else onPrevious() }
            }) { change, amount -> change.consume(); total += amount }
        }.clickable(onClick = onExpand)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(art, null, contentScale = ContentScale.Crop, modifier = Modifier.size(42.dp).clip(RoundedCornerShape(6.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(song.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onSave, modifier = Modifier.size(44.dp)) { Icon(if (saved) SpotuiIcons.Check else SpotuiIcons.Add,
                if (saved) "Remove from favourites" else "Add to favourites", Modifier.size(22.dp), tint = if (saved) Color(0xFF1ED760) else Color.White) }
            IconButton(onClick = onPlayPause, modifier = Modifier.size(44.dp)) {
                if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                else Icon(if (playing) SpotuiIcons.Pause else SpotuiIcons.Play, if (playing) "Pause" else "Play", Modifier.size(26.dp), tint = Color.White)
            }
        }
        SpotuiMiniProgress(position, durationMs)
    }
}

@Composable
private fun SpotuiMiniProgress(position: PlaybackPosition, duration: Long) {
    val progress = if (duration > 0) (position.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    Box(Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = .2f))) {
        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color.White.copy(alpha = .8f)))
    }
}
