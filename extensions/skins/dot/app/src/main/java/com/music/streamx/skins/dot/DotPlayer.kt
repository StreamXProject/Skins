package com.music.streamx.skins.dot

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.lyrics.LyricsSource
import com.music.streamx.data.model.LikeStatus
import com.music.streamx.data.model.Song
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.player.PlayerPlatform
import com.music.streamx.ui.skins.*
import dev.chrisbanes.haze.HazeState
import com.music.streamx.ui.theme.StatusBarIcons

private fun durationFromText(text: String?): Long {
    val parts = text?.split(':')?.map { it.toLongOrNull() ?: return 0L } ?: return 0L
    return if (parts.size in 2..3 && parts.all { it >= 0 }) parts.fold(0L) { n, v -> n * 60 + v } * 1000L else 0L
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
internal fun DotPlayer(
    song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long,
    queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean,
    repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus,
    accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit,
    onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit,
    onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit,
    onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit,
    onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit,
    lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean,
    lyricsProviderStates: Map<LyricsSource, LyricsProviderState>,
    onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean,
    onDismissLyricsOffset: () -> Unit, modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color
    StatusBarIcons(false)
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val locked = rememberSkinControlsLocked()
    val art = rememberSkinRemoteArtwork(song)
    val total = durationMs.takeIf { it > 0 } ?: durationFromText(song.durationText)

    var page by rememberSaveable { mutableIntStateOf(0) }
    var output by remember { mutableStateOf(false) }
    var pipeline by remember { mutableStateOf(false) }
    var cast by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf(false) }
    var providers by remember { mutableStateOf(false) }
    var timing by remember { mutableStateOf(false) }
    val haze = remember { HazeState() }

    val openOutput = rememberSkinOutputPicker { output = true }
    val trackFormat = rememberSkinTrackFormat()
    val lyricsOffset by skinPlayerSettings().lyricsOffsetMs.collectAsStateWithLifecycle()

    fun action(block: () -> Unit) { if (locked) onBlocked() else block() }
    SkinBackHandler(page != 0 || options || output || pipeline || cast || providers || timing || lyricsOffsetOpen) {
        when {
            options -> options = false
            providers -> providers = false
            timing || lyricsOffsetOpen -> { timing = false; onDismissLyricsOffset() }
            pipeline -> pipeline = false
            cast -> cast = false
            output -> output = false
            else -> page = 0
        }
    }

    BoxWithConstraints(modifier.fillMaxSize().background(c.background)) {
        val compact = maxHeight < 720.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TuiKey(
                    label = if (page == 0) "[ V ]" else "[ < ]",
                    onClick = { if (page == 0) onCollapse() else page = 0 },
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = when (page) {
                            1 -> "// THE WORDS"
                            2 -> "// UP NEXT"
                            else -> "// NOW PLAYING"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = c.onSurfaceVariant,
                    )
                    Text(
                        text = "TRACK ${(queueIndex + 1).toString().padStart(2, '0')}/${queue.size.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = acc,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box {
                    TuiKey(label = "[ ... ]", onClick = { options = true })
                    DropdownMenu(
                        expanded = options,
                        onDismissRequest = { options = false },
                        modifier = Modifier.background(c.surface).border(1.dp, c.outline, Edge),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Track options", style = MaterialTheme.typography.bodyMedium) },
                            onClick = { options = false; onOpenMenu() },
                        )
                        DropdownMenuItem(
                            text = { Text("Audio output", style = MaterialTheme.typography.bodyMedium) },
                            onClick = { options = false; openOutput() },
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep timer", style = MaterialTheme.typography.bodyMedium) },
                            onClick = { options = false; onOpenTimer() },
                        )
                        DropdownMenuItem(
                            text = { Text("Listen together", style = MaterialTheme.typography.bodyMedium) },
                            onClick = { options = false; action(onListenTogether) },
                        )
                        if (page == 1) {
                            DropdownMenuItem(
                                text = { Text("Lyrics provider", style = MaterialTheme.typography.bodyMedium) },
                                onClick = { options = false; providers = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Lyrics timing", style = MaterialTheme.typography.bodyMedium) },
                                onClick = { options = false; timing = true },
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = c.outline)

            // Content Crossfade
            Crossfade(
                targetState = page,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                animationSpec = tween(if (reduce) 0 else 240),
                label = "Player page",
            ) { current ->
                when (current) {
                    1 -> DotLyrics(
                        lyrics = lyrics,
                        lyricsUnavailable = lyricsUnavailable,
                        positionMs = position.positionMs,
                        lyricsOffsetMs = lyricsOffset,
                        reduceAnimation = reduce,
                        onSeek = { action { onSeek(skinLyricsSeek(it, lyricsOffset)) } },
                    )
                    2 -> DotQueue(
                        queue = queue,
                        currentIndex = queueIndex,
                        isPlaying = isPlaying,
                        onJump = onJumpTo,
                        onRemove = onRemove,
                        onMove = onMove,
                        onDragActive = onDragActive,
                        onClear = { action(onClear) },
                        onPlay = { action(onPlayPause) },
                        locked = locked,
                        autoplay = autoplayEnabled,
                        onAutoplay = { action(onToggleAutoplay) },
                    )
                    else -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = if (compact) 8.dp else 14.dp),
                    ) {
                        // Artwork panel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            TuiPanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                val coverArt = art ?: song.thumbnailUrl
                                if (DotOptions.asciiArt) {
                                    val ascii = rememberAsciiCoverBitmap(
                                        artworkSource = coverArt,
                                        cols = DotOptions.cols,
                                        seed = song.videoId.hashCode().toLong(),
                                    )
                                    AsciiCover(
                                        cover = ascii,
                                        playing = isPlaying,
                                        wave = DotOptions.waveSweep,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    Crossfade(
                                        targetState = coverArt,
                                        animationSpec = tween(if (reduce) 0 else 240),
                                        label = "cover",
                                    ) { cover ->
                                        AsyncImage(
                                            model = cover,
                                            contentDescription = "Album artwork",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                                    com.music.streamx.ui.skins.SkinArtworkStats(Modifier.align(Alignment.BottomCenter).padding(8.dp))
                                }
                            }
                        }

                        // Meta and Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = if (compact) 6.dp else 10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    CursorTitle(
                                        text = song.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 2.dp),
                                    )
                                    Text(
                                        text = listOf(song.artist, song.albumName).filterNotNull().filter { it.isNotBlank() }
                                            .joinToString(" · ")
                                            .lowercase(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.clickable { onOpenArtist(song.artistId, song.artist) },
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                TuiKey(
                                    label = if (likeStatus == LikeStatus.LIKE) "[ FAV * ]" else "[ FAV ]",
                                    accent = likeStatus == LikeStatus.LIKE,
                                    bright = likeStatus == LikeStatus.LIKE,
                                    onClick = { action(onToggleLike) },
                                )
                            }

                            // Format spec chips
                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                TuiChip("src:${song.playbackSource ?: "streamx"}".lowercase())
                                if (trackFormat.isNotBlank()) {
                                    TuiChip("audio:${trackFormat}".lowercase())
                                }
                            }

                            // Slider row
                            var scrub by remember { mutableStateOf<Float?>(null) }
                            val playFraction = if (total > 0) (position.positionMs.toFloat() / total).coerceIn(0f, 1f) else 0f
                            val shownMs = scrub?.let { (it * total).toLong() } ?: position.positionMs

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                            ) {
                                Text(
                                    text = formatTime(shownMs),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (scrub != null) acc else c.onSurfaceVariant,
                                )
                                ThinSlider(
                                    fraction = scrub ?: playFraction,
                                    onScrub = { scrub = it },
                                    onSeek = {
                                        scrub = null
                                        action { onSeekFraction(it) }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                )
                                Text(
                                    text = formatTime(total),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.onSurfaceVariant,
                                )
                            }

                            // Transport keys
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                            ) {
                                TuiKey(
                                    label = if (shuffleEnabled) "[SHUF*]" else "[SHUF]",
                                    accent = shuffleEnabled,
                                    fill = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { action(onToggleShuffle) },
                                )
                                TuiKey(
                                    label = "|<<",
                                    enabled = hasPrevious,
                                    big = true,
                                    modifier = Modifier.weight(1f),
                                    onHold = { action { onSeek(maxOf(0L, position.positionMs - 5000L)) } },
                                    onClick = { action(onPrevious) },
                                )
                                TuiKey(
                                    label = if (isPlaying) "  ||  " else "  |>  ",
                                    bright = true,
                                    big = true,
                                    modifier = Modifier.weight(1.3f),
                                    onClick = { action(onPlayPause) },
                                )
                                TuiKey(
                                    label = ">>|",
                                    enabled = hasNext,
                                    big = true,
                                    modifier = Modifier.weight(1f),
                                    onHold = { action { onSeek(minOf(total, position.positionMs + 5000L)) } },
                                    onClick = { action(onNext) },
                                )
                                TuiKey(
                                    label = when (repeatMode) { 2 -> "[LOOP*]"; 1 -> "[LOOP 1]"; else -> "[LOOP]" },
                                    accent = repeatMode != 0,
                                    fill = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { action(onCycleRepeat) },
                                )
                            }

                            // Action keys & Queue row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                            ) {
                                TuiKey(
                                    label = if (lyrics != null) "[ WORDS ]" else "[ NO WORDS ]",
                                    accent = lyrics != null,
                                    fill = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { page = 1 },
                                )
                                TuiKey(
                                    label = "[ OUTPUT ]",
                                    fill = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = openOutput,
                                )
                                TuiKey(
                                    label = "[ QUEUE: ${(queueIndex + 1).toString().padStart(2, '0')}/${queue.size.toString().padStart(2, '0')} ]",
                                    fill = true,
                                    modifier = Modifier.weight(1.2f),
                                    onClick = { page = 2 },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (output) {
        DotAudioOutput(
            onDismiss = { output = false },
            onPipeline = { output = false; pipeline = true },
            onCast = { output = false; cast = true },
        )
    }
    if (pipeline) SkinAudioPipeline(haze, isPlaying) { pipeline = false }
    if (cast) PlayerPlatform.host.CastDialog(haze) { cast = false }
    if (providers) SkinLyricsProviders(
        haze, lyricsSource, lyricsProviderStates,
        onSelect = { source -> onSelectLyricsProvider(source); providers = false },
        onDismiss = { providers = false },
    )
    if (timing || lyricsOffsetOpen) SkinLyricsOffset(haze) {
        timing = false
        onDismissLyricsOffset()
    }

}
