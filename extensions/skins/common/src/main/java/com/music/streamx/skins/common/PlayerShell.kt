package com.music.streamx.skins.common

import com.music.streamx.skins.common.icons.*
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
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
import com.music.streamx.ui.theme.StatusBarIcons
import dev.chrisbanes.haze.HazeState

/**
 * The frame every skin's player shares: a top bar, three pages (now playing,
 * lyrics, queue), and the host sheets for output, lyrics providers and timing.
 * The now-playing page is the skin's own — see [BaseSkinRenderer.NowPlaying].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerShell(
    renderer: BaseSkinRenderer,
    song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long,
    queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean,
    repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus,
    accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit,
    onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit,
    onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onClear: () -> Unit,
    onOpenMenu: () -> Unit, onOpenArtist: (String?, String) -> Unit, onListenTogether: () -> Unit,
    onCollapse: () -> Unit, onOpenTimer: () -> Unit, onBlocked: () -> Unit,
    lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean,
    lyricsProviderStates: Map<LyricsSource, LyricsProviderState>, onSelectLyricsProvider: (LyricsSource) -> Unit,
    lyricsOffsetOpen: Boolean, onDismissLyricsOffset: () -> Unit, modifier: Modifier,
) {
    val c = MaterialTheme.colorScheme
    StatusBarIcons(c.background.luminance() > .5f)
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val lyricsOffset by skinPlayerSettings().lyricsOffsetMs.collectAsStateWithLifecycle()
    val locked = rememberSkinControlsLocked()
    val art = rememberSkinRemoteArtwork(song)
    val total = durationMs.takeIf { it > 0 } ?: durationFromText(song.durationText)
    val trackFormat = rememberSkinTrackFormat()
    var page by rememberSaveable { mutableIntStateOf(0) }
    var output by remember { mutableStateOf(false) }
    var pipeline by remember { mutableStateOf(false) }
    var cast by remember { mutableStateOf(false) }
    var providers by remember { mutableStateOf(false) }
    var timing by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf(false) }
    val haze = remember { HazeState() }
    val openOutput = rememberSkinOutputPicker { output = true }
    fun guarded(block: () -> Unit): () -> Unit = { if (locked) onBlocked() else block() }
    SkinBackHandler(page != 0) { page = 0 }

    BoxWithConstraints(modifier.fillMaxSize().background(c.background)) {
        val compact = maxHeight < 740.dp
        val model = NowPlayingModel(song, art, isPlaying, isLoading, position, total, queue, queueIndex, hasPrevious, hasNext,
            repeatMode, shuffleEnabled, likeStatus == LikeStatus.LIKE, locked, trackFormat, reduce, compact)
        val actions = NowPlayingActions(
            playPause = guarded(onPlayPause), next = guarded(onNext), previous = guarded(onPrevious),
            seekFraction = { f -> if (locked) onBlocked() else onSeekFraction(f) },
            toggleLike = guarded(onToggleLike), toggleShuffle = guarded(onToggleShuffle), cycleRepeat = guarded(onCycleRepeat),
            openMenu = onOpenMenu, openOutput = openOutput, openArtist = { onOpenArtist(song.artistId, song.artist) },
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ if (page == 0) onCollapse() else page = 0 }) {
                    Icon(if (page == 0) SkinIcons.KeyboardArrowDown else SkinIcons.ArrowBack, if (page == 0) "Collapse player" else "Back to player")
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(when (page) { 1 -> "Lyrics"; 2 -> "Up next"; else -> renderer.style.brand }.uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = c.primary)
                    Text(song.playbackSource ?: "Now playing", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box {
                    IconButton({ options = true }) { Icon(SkinIcons.MoreVert, "Player options") }
                    DropdownMenu(options, { options = false }) {
                        DropdownMenuItem(text = { Text("Track options") }, onClick = { options = false; onOpenMenu() })
                        DropdownMenuItem(text = { Text("Audio output") }, onClick = { options = false; openOutput() })
                        DropdownMenuItem(text = { Text("Sleep timer") }, onClick = { options = false; onOpenTimer() })
                        DropdownMenuItem(text = { Text("Listen together") }, onClick = { options = false; guarded(onListenTogether)() })
                        if (page == 1) {
                            DropdownMenuItem(text = { Text("Lyrics provider") }, onClick = { options = false; providers = true })
                            DropdownMenuItem(text = { Text("Lyrics timing") }, onClick = { options = false; timing = true })
                        }
                    }
                }
            }
            Crossfade(page, Modifier.weight(1f).fillMaxWidth(), animationSpec = tween(if (reduce) 0 else 260), label = "Player page") { current ->
                when (current) {
                    1 -> renderer.Lyrics(model, lyrics, lyricsUnavailable, lyricsOffset,
                        onSeek = { t -> if (locked) onBlocked() else onSeek(skinLyricsSeek(t, lyricsOffset)) }, modifier = Modifier.fillMaxSize())
                    2 -> renderer.Queue(model, autoplayEnabled, onJumpTo, onRemove, onMove, onClear, guarded(onPlayPause), guarded(onToggleAutoplay), Modifier.fillMaxSize())
                    else -> renderer.NowPlaying(model, actions, Modifier.fillMaxSize())
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PageTab("Playing", SkinIcons.MusicNote, page == 0, { page = 0 }, Modifier.weight(1f), renderer.style)
                PageTab("Lyrics", SkinIcons.Lyrics, page == 1, { page = 1 }, Modifier.weight(1f), renderer.style)
                PageTab("Queue", SkinIcons.QueueMusic, page == 2, { page = 2 }, Modifier.weight(1f), renderer.style)
            }
        }
    }
    if (output) SkinAudioOutput(haze, accountName, { output = false }, { output = false; pipeline = true }, { output = false; cast = true })
    if (pipeline) SkinAudioPipeline(haze, isPlaying) { pipeline = false }
    if (cast) PlayerPlatform.host.CastDialog(haze) { cast = false }
    if (providers) SkinLyricsProviders(haze, lyricsSource, lyricsProviderStates, { onSelectLyricsProvider(it); providers = false }) { providers = false }
    if (timing || lyricsOffsetOpen) SkinLyricsOffset(haze) { timing = false; onDismissLyricsOffset() }
}

@Composable
private fun PageTab(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier, style: SkinStyle) {
    val c = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = modifier.height(40.dp), shape = RoundedCornerShape(style.radius),
        color = if (selected) c.primary else c.surfaceVariant, contentColor = if (selected) c.onPrimary else c.onSurfaceVariant) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
