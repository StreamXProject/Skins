package com.music.streamx.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.lyrics.LyricsSource
import com.music.streamx.data.model.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.theme.StatusBarIcons
import com.music.streamx.ui.haptics.rememberHaptics
import com.music.streamx.ui.skins.SkinIcon as Icon
import com.music.streamx.ui.skins.*
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/** Spotui's complete player composition, backed exclusively by the host's playback/lyrics state. */
@Composable
internal fun SpotuiPlayer(
    song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long,
    queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean,
    repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus,
    accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit,
    onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit,
    onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit,
    onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit,
    onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?,
    lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>,
    onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean,
    onDismissLyricsOffset: () -> Unit, modifier: Modifier = Modifier,
) {
    StatusBarIcons(dark = false)
    val reduceMotion by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val locked = rememberSkinControlsLocked()
    var page by rememberSaveable { mutableStateOf("player") }
    var queueOpen by remember { mutableStateOf(false) }
    var outputOpen by remember { mutableStateOf(false) }
    var pipelineOpen by remember { mutableStateOf(false) }
    var castOpen by remember { mutableStateOf(false) }
    var providersOpen by remember { mutableStateOf(false) }
    val haze = remember { HazeState() }
    val openOutput = rememberSkinOutputPicker { outputOpen = true }
    SkinBackHandler(page != "player") { page = "player" }
    SkinBackHandler(outputOpen) { outputOpen = false }
    SkinBackHandler(providersOpen) { providersOpen = false }
    SkinBackHandler(pipelineOpen) { pipelineOpen = false }
    SkinBackHandler(castOpen) { castOpen = false }
    SkinBackHandler(lyricsOffsetOpen, onDismissLyricsOffset)
    val remoteArt = rememberSkinRemoteArtwork(song)
    val palette = rememberArtworkColors(remoteArt)
    val accent by animateColorAsState(com.music.streamx.ui.skins.spotuiArtworkTint(palette.colors),
        tween(if (reduceMotion) 0 else 500), label = "spotuiArtworkColor")
    val canvas = rememberSkinCanvas(song)
    var canvasRendered by remember(canvas?.url) { mutableStateOf(false) }
    val translation = rememberSkinLyricsTranslation(song.videoId, lyrics, lyricsSource, lyricsUnavailable, "Loading lyrics…", rememberHaptics())
    val seekLine: (Long) -> Unit = { time -> if (locked) onBlocked() else onSeek(skinLyricsSeek(time, skinPlayerSettings().lyricsOffsetMs.value)) }
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier.fillMaxSize().background(Color.Black)) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(accent, Color.Black))))
            if (page == "player" && canvas != null) {
                CanvasArtworkPlayer(canvas, isPlaying, Modifier.fillMaxSize(), onRenderedChanged = { canvasRendered = it })
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .5f), Color.Black.copy(alpha = .12f), Color.Black.copy(alpha = .8f)))))
            }
            AnimatedContent(page, transitionSpec = {
                (slideInVertically(tween(if (reduceMotion) 0 else 280)) { it / 8 } + fadeIn(tween(if (reduceMotion) 0 else 220))) togetherWith
                    (slideOutVertically(tween(if (reduceMotion) 0 else 220)) { -it / 12 } + fadeOut(tween(if (reduceMotion) 0 else 160)))
            }, label = "spotuiPlayerPages") { target ->
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                    SpotuiPlayerHeader(if (target == "player") song.playbackSource ?: "Now playing" else if (target == "queue") "Queue" else song.title,
                        if (target == "lyrics") song.artist else if (target == "player") "PLAYING FROM" else "",
                        onClose = { if (target == "player") onCollapse() else page = "player" },
                        onMenu = if (target == "lyrics") ({ providersOpen = true }) else onOpenMenu,
                        onOrigin = if (target == "player") onOpenPlaybackSource else null)
                    when (target) {
                        "lyrics" -> {
                            SpotuiLyrics(lyrics, translation.subLines, lyricsUnavailable, song.videoId, position, reduceMotion,
                                seekLine, Modifier.weight(1f))
                            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = translation.toggleTranslation, enabled = !lyrics.isNullOrEmpty()) {
                                    Icon(SpotuiIcons.Translate, "Translate", Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                                    Text(if (translation.showingTranslation) "Original" else "Translate", color = Color.White)
                                }
                                TextButton(onClick = translation.toggleRomanization, enabled = !lyrics.isNullOrEmpty()) { Text("Romanize", color = Color.White) }
                            }
                            SpotuiSeekBar(position, durationMs, song.videoId, onSeekFraction, locked)
                            SpotuiPlayButton(isPlaying, isLoading, onPlayPause, Modifier.align(Alignment.CenterHorizontally).padding(bottom = 12.dp))
                        }
                        else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            item {
                                SpotuiArtworkPager(song, queue, queueIndex, remoteArt, canvasRendered, locked, reduceMotion, onJumpTo)
                            }
                            item {
                                Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = 20.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(song.title, style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(song.artist, Modifier.clickable { onOpenArtist(song.artistId, song.artist) }.padding(top = 4.dp),
                                                color = Color.White.copy(alpha = .7f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        IconButton(onClick = onToggleLike) { Icon(if (likeStatus == LikeStatus.LIKE) SpotuiIcons.Check else SpotuiIcons.Add,
                                            if (likeStatus == LikeStatus.LIKE) "Remove from favourites" else "Add to favourites", tint = if (likeStatus == LikeStatus.LIKE) Color(0xFF1ED760) else Color.White, modifier = Modifier.size(26.dp)) }
                                    }
                                    SpotuiSeekBar(position, durationMs, song.videoId, onSeekFraction, locked)
                                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        SpotuiAction(SpotuiIcons.Shuffle, "Shuffle", { if (locked) onBlocked() else onToggleShuffle() }, active = shuffleEnabled)
                                        SpotuiAction(SpotuiIcons.Previous, "Previous", { if (locked) onBlocked() else onPrevious() }, enabled = hasPrevious && !locked)
                                        SpotuiPlayButton(isPlaying, isLoading, onPlayPause)
                                        SpotuiAction(SpotuiIcons.Next, "Next", { if (locked) onBlocked() else onNext() }, enabled = hasNext && !locked)
                                        SpotuiAction(SpotuiIcons.Repeat, if (repeatMode == RepeatModes.ONE) "Repeat one" else "Repeat", { if (locked) onBlocked() else onCycleRepeat() }, active = repeatMode != RepeatModes.OFF)
                                    }
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = openOutput) { Icon(SpotuiIcons.Devices, "Audio output", Modifier.size(22.dp), tint = Color(0xFF1ED760)) }
                                        Text(accountName ?: "This device", Modifier.weight(1f).clickable(onClick = openOutput), fontSize = 12.sp, color = Color(0xFF1ED760), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        IconButton(onClick = onListenTogether) { Icon(Icons.Outlined.Group, "Listen together", Modifier.size(22.dp)) }
                                        IconButton(onClick = { queueOpen = true }) { Icon(Icons.Outlined.QueueMusic, "Queue", Modifier.size(24.dp)) }
                                    }
                                }
                            }
                            item {
                                SpotuiLyricsPreview(song.videoId, lyrics, lyricsUnavailable, position, accent, reduceMotion, { page = "lyrics" })
                            }
                        }
                    }
                }
            }
        }
        if (queueOpen) SpotuiQueueSheet(song, queue, queueIndex, autoplayEnabled, shuffleEnabled, isPlaying, isLoading,
            locked, reduceMotion, windowHeight, onJumpTo, onRemove, onMove, onDragActive, onClear, onToggleAutoplay,
            onToggleShuffle, onOpenTimer, onPlayPause, { queueOpen = false })
        if (outputOpen) SkinAudioOutput(haze, accountName, { outputOpen = false }, { pipelineOpen = true }, { castOpen = true })
        if (providersOpen) SkinLyricsProviders(haze, lyricsSource, lyricsProviderStates, onSelectLyricsProvider, { providersOpen = false })
        if (pipelineOpen) SkinAudioPipeline(haze, isPlaying, { pipelineOpen = false })
        if (castOpen) PlayerPlatform.host.CastDialog(haze) { castOpen = false }
        if (lyricsOffsetOpen) SkinLyricsOffset(haze, onDismissLyricsOffset)
    }
}

@Composable
private fun SpotuiPlayerHeader(title: String, subtitle: String, onClose: () -> Unit, onMenu: () -> Unit, onOrigin: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { Icon(SpotuiIcons.Down, "Close player", Modifier.size(26.dp)) }
        Column(Modifier.weight(1f).then(if (onOrigin != null) Modifier.clickable(onClick = onOrigin) else Modifier), horizontalAlignment = Alignment.CenterHorizontally) {
            if (onOrigin != null && subtitle.isNotBlank()) Text(subtitle, color = Color.White.copy(alpha = .7f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (onOrigin == null && subtitle.isNotBlank()) Text(subtitle, color = Color.White.copy(alpha = .7f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onMenu) { Icon(Icons.Outlined.MoreVert, "More", Modifier.size(24.dp)) }
    }
}

@Composable
private fun SpotuiArtworkPager(song: Song, queue: List<Song>, index: Int, remoteArt: String?, canvasVisible: Boolean, locked: Boolean, reduce: Boolean, onJump: (Int) -> Unit) {
    val count = queue.size.coerceAtLeast(1)
    val pager = rememberPagerState(initialPage = index.coerceIn(0, count - 1), pageCount = { count })
    val currentIndex by rememberUpdatedState(index)
    val currentCount by rememberUpdatedState(queue.size)
    val currentJump by rememberUpdatedState(onJump)
    var programmatic by remember { mutableStateOf(false) }
    LaunchedEffect(index, count) {
        val target = index.coerceIn(0, count - 1)
        if (pager.currentPage != target) {
            programmatic = true
            try { if (reduce) pager.scrollToPage(target) else pager.animateScrollToPage(target, animationSpec = tween(320)) }
            finally { programmatic = false }
        }
    }
    LaunchedEffect(pager, locked) {
        snapshotFlow { pager.settledPage }.drop(1).collect { target ->
            if (!locked && !programmatic && target != currentIndex && target in 0 until currentCount) currentJump(target)
        }
    }
    HorizontalPager(pager, userScrollEnabled = !locked && count > 1, modifier = Modifier.widthIn(max = 425.dp).fillMaxWidth().aspectRatio(1f)) { page ->
        val entry = if (page == index) song else queue.getOrNull(page) ?: song
        Box(Modifier.fillMaxSize().padding(20.dp).clip(RoundedCornerShape(10.dp))) {
            AsyncImage(if (page == index) remoteArt else entry.thumbnailUrl.artworkAt(HEADER_ART_PX), null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    .graphicsLayer { alpha = if (canvasVisible && page == index) 0f else 1f })
            if (page == index) com.music.streamx.ui.skins.SkinArtworkStats(
                Modifier.align(Alignment.BottomCenter).padding(10.dp))
        }
    }
}

@Composable
private fun SpotuiAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, active: Boolean = false, enabled: Boolean = true) {
    IconButton(onClick = onClick, enabled = enabled) { Icon(icon, label, Modifier.size(26.dp), tint = if (!enabled) Color.White.copy(alpha = .3f) else if (active) Color(0xFF1ED760) else Color.White) }
}

@Composable
internal fun SpotuiPlayButton(playing: Boolean, loading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(64.dp).background(Color.White, CircleShape)) {
        if (loading) CircularProgressIndicator(Modifier.size(30.dp), color = Color.Black, strokeWidth = 2.dp)
        else Icon(if (playing) SpotuiIcons.Pause else SpotuiIcons.Play, if (playing) "Pause" else "Play", Modifier.size(30.dp), tint = Color.Black)
    }
}

@Composable
private fun SpotuiSeekBar(position: PlaybackPosition, duration: Long, trackId: String, onSeek: (Float) -> Unit, locked: Boolean) {
    var drag by remember(trackId) { mutableStateOf<Float?>(null) }
    val fraction = drag ?: if (duration > 0) (position.positionMs.toFloat() / duration).coerceIn(0f, 1f) else 0f
    Column(Modifier.fillMaxWidth()) {
        ThinSlider(value = fraction, onValueChange = { if (!locked && duration > 0) drag = it },
            onValueChangeFinished = { if (!locked && duration > 0) drag?.let(onSeek); drag = null },
            idleHeight = 3.dp, activeHeight = 5.dp, activeColor = Color.White, inactiveColor = Color.White.copy(alpha = .25f),
            modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(spotuiTime((fraction * duration).toLong()), color = Color.White.copy(alpha = .7f), fontSize = 12.sp)
            Text(spotuiTime(duration), color = Color.White.copy(alpha = .7f), fontSize = 12.sp)
        }
    }
}

fun spotuiTime(value: Long): String = (value.coerceAtLeast(0) / 1000).let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" }
fun spotuiActiveLyric(lines: List<LyricLine>, time: Long): Int =
    if (lines.none { it.timeMs > 0 }) 0 else lines.indexOfLast { it.timeMs >= 0 && it.timeMs <= time }.coerceAtLeast(0)

@Composable
private fun SpotuiLyricsPreview(track: String, lines: List<LyricLine>?, unavailable: Boolean, position: PlaybackPosition, accent: Color, reduce: Boolean, onExpand: () -> Unit) {
    val offset by skinPlayerSettings().lyricsOffsetMs.collectAsStateWithLifecycle()
    val active = spotuiActiveLyric(lines.orEmpty(), skinLyricsPosition(position.positionMs, offset))
    val start = active.coerceAtMost((lines.orEmpty().size - 5).coerceAtLeast(0))
    Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp))
        .background(Brush.verticalGradient(listOf(accent.copy(alpha = .9f), accent.copy(alpha = .4f))))
        .clickable(onClick = onExpand).padding(20.dp)) {
        Text("Lyrics preview", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (lines.isNullOrEmpty()) Text(if (unavailable) "No lyrics found for this track" else "Loading lyrics…", color = Color.White.copy(alpha = .7f))
        else {
            lines.drop(start).take(5).forEachIndexed { i, line ->
                val alpha by animateFloatAsState(if (start + i == active) 1f else .55f, tween(if (reduce) 0 else 220), label = "spotuiPreviewLine")
                Text(line.text, Modifier.padding(vertical = 6.dp), color = Color.White.copy(alpha = alpha), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            FilledTonalButton(onClick = onExpand, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("Show lyrics") }
        }
    }
}

@Composable
private fun SpotuiLyrics(lines: List<LyricLine>?, translations: List<LyricLine>?, unavailable: Boolean, track: String,
    position: PlaybackPosition, reduce: Boolean, onSeek: (Long) -> Unit, modifier: Modifier) {
    val list = rememberLazyListState()
    val offset by skinPlayerSettings().lyricsOffsetMs.collectAsStateWithLifecycle()
    val active by remember(lines, offset) { derivedStateOf { spotuiActiveLyric(lines.orEmpty(), skinLyricsPosition(position.positionMs, offset)) } }
    val margin = with(LocalDensity.current) { 90.dp.roundToPx() }
    LaunchedEffect(track, active) {
        if (!lines.isNullOrEmpty() && !list.isScrollInProgress) {
            if (reduce) list.scrollToItem(active, -margin) else {
                val visible = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == active }
                if (visible != null) list.animateScrollBy((visible.offset - margin).toFloat(), tween(800, easing = FastOutSlowInEasing))
                else list.animateScrollToItem(active, -margin)
            }
        }
    }
    if (lines.isNullOrEmpty()) Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (unavailable) Text("Couldn't find lyrics for this track", color = Color.White.copy(alpha = .7f)) else CircularProgressIndicator(color = Color.White)
    } else LazyColumn(modifier.fillMaxWidth()
        .nestedScroll(SpotuiLyricsScrollBoundary)
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val edge = minOf(48.dp.toPx() / size.height, .25f)
            drawRect(Brush.verticalGradient(0f to Color.Transparent, edge to Color.Black,
                (1f - edge) to Color.Black, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
        }, state = list, overscrollEffect = null, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemsIndexed(lines) { index, line ->
            val opacity by animateFloatAsState(if (lines.none { it.timeMs > 0 } || index == active) 1f else .42f, tween(if (reduce) 0 else 650, easing = FastOutSlowInEasing), label = "spotuiLyricHighlight")
            val scale by animateFloatAsState(if (index == active) 1.06f else 1f, tween(if (reduce) 0 else 700, easing = FastOutSlowInEasing), label = "spotuiLyricScale")
            Column(Modifier.fillMaxWidth().padding(end = 24.dp).graphicsLayer { alpha = opacity; scaleX = scale; scaleY = scale; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, .5f) }
                .clickable(enabled = line.timeMs >= 0) { onSeek(line.timeMs) }.padding(vertical = 4.dp)) {
                Text(line.text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                translations?.getOrNull(index)?.text?.let { Text(it, color = Color.White.copy(alpha = .7f), fontSize = 14.sp) }
            }
        }
    }
}

private val SpotuiLyricsScrollBoundary = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = Offset(0f, available.y)
    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = Velocity(0f, available.y)
}
