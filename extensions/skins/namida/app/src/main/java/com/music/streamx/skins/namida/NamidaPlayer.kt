package com.music.streamx.skins.namida

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.lyrics.LyricsSource
import com.music.streamx.data.model.LikeStatus
import com.music.streamx.data.model.Song
import com.music.streamx.data.model.artworkAt
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.skins.common.LocalQueueDragActive
import com.music.streamx.skins.common.QueueList
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.haptics.rememberHaptics
import com.music.streamx.ui.player.LocalPlayerDock
import com.music.streamx.ui.player.PlayerPlatform
import com.music.streamx.ui.player.RepeatModes
import com.music.streamx.ui.skins.SkinAudioOutput
import com.music.streamx.ui.skins.SkinAudioPipeline
import com.music.streamx.ui.skins.SkinBackHandler
import com.music.streamx.ui.skins.SkinIcon
import com.music.streamx.ui.skins.SkinLyricsOffset
import com.music.streamx.ui.skins.SkinLyricsProviders
import com.music.streamx.ui.skins.rememberSkinControlsLocked
import com.music.streamx.ui.skins.rememberSkinLyricsTranslation
import com.music.streamx.ui.skins.rememberSkinOutputPicker
import com.music.streamx.ui.skins.rememberSkinRemoteArtwork
import com.music.streamx.ui.skins.skinLyricsPosition
import com.music.streamx.ui.skins.skinLyricsSeek
import com.music.streamx.ui.skins.skinPlayerSettings
import com.music.streamx.ui.theme.StatusBarIcons
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Namida's player. One surface with three resting places: the mini player
 * (where it grows from), the player, and the queue, where the player folds into
 * a header row and the queue rises under it. The host's sheet supplies how far
 * open it is; the skin pins itself to the screen and draws the growth itself,
 * so the panel, cover and play button travel out of the mini player instead of
 * the page sliding up.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NamidaPlayer(
    song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long,
    queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean,
    repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus,
    accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit,
    onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit,
    onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit,
    onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit,
    onCollapse: () -> Unit, onOpenTimer: () -> Unit, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?,
    lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>,
    onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean,
    onDismissLyricsOffset: () -> Unit, modifier: Modifier = Modifier,
) {
    val t = LocalNamidaTones.current
    StatusBarIcons(dark = !t.dark)
    val remoteArt = rememberSkinRemoteArtwork(song)
    PublishArtworkSeed(remoteArt)
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val locked = rememberSkinControlsLocked()
    val guard: (() -> Unit) -> () -> Unit = { action -> { if (locked) onBlocked() else action() } }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // ---- How open: the host's sheet, 0 on the mini player to 1 fully up ----
    val dock = LocalPlayerDock.current
    val open: () -> Float = remember(dock) { { dock?.takeIf { it.sheetTravel > 0f }?.openFraction() ?: 1f } }
    val travel: () -> Float = remember(dock) { { dock?.sheetTravel ?: 0f } }
    DisposableEffect(open) {
        NamidaDock.open = open
        onDispose { if (NamidaDock.open === open) NamidaDock.open = null }
    }

    // ---- The queue stage, 0 on the player to 1 with the queue up ----
    val queueStage = remember { Animatable(0f) }
    fun settleQueue(target: Float) = scope.launch {
        queueStage.animateTo(target, if (reduce) tween(0) else spring(dampingRatio = .88f, stiffness = 420f))
    }
    SkinBackHandler(queueStage.targetValue > 0f) { settleQueue(0f) }

    var lyricsOpen by rememberSaveable { mutableStateOf(false) }
    val lyricsShown by animateFloatAsState(if (lyricsOpen) 1f else 0f, tween(if (reduce) 0 else 320, easing = FastOutSlowInEasing), label = "namidaLyrics")

    var outputOpen by remember { mutableStateOf(false) }
    var pipelineOpen by remember { mutableStateOf(false) }
    var castOpen by remember { mutableStateOf(false) }
    var providersOpen by remember { mutableStateOf(false) }
    val haze = remember { HazeState() }
    val openOutput = rememberSkinOutputPicker { outputOpen = true }
    SkinBackHandler(outputOpen) { outputOpen = false }
    SkinBackHandler(providersOpen) { providersOpen = false }
    SkinBackHandler(pipelineOpen) { pipelineOpen = false }
    SkinBackHandler(castOpen) { castOpen = false }
    SkinBackHandler(lyricsOffsetOpen, onDismissLyricsOffset)

    // Where the cover and the play button sit on the player, once laid out.
    var artFull by remember { mutableStateOf(Rect.Zero) }
    var playFull by remember { mutableStateOf(Rect.Zero) }
    var root by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val swipe = remember { Animatable(0f) }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(modifier.fillMaxSize()
        // Held still on the screen while the sheet travels: the movement on
        // show is the panel growing, not the page sliding.
        .graphicsLayer { translationY = -(1f - open()) * travel() }
        .onGloballyPositioned { root = it }
        .drawBehind {
            val p = open()
            val full = Rect(Offset.Zero, size)
            val mini = NamidaDock.panel ?: Rect(12.dp.toPx(), size.height - 170.dp.toPx(), size.width - 12.dp.toPx(), size.height - 88.dp.toPx())
            val rect = mini.lerpTo(full, p)
            val radius = MINI_RADIUS.dp.toPx() * (1f - p) + 26.dp.toPx() * p * (1f - p) * 2f
            // The mini player's bright wash, settling as it opens into the
            // darker page Namida's player sits on, a hint of the colour on top.
            val top = androidx.compose.ui.graphics.lerp(t.panelTop, t.playerTop, p)
            val bottom = androidx.compose.ui.graphics.lerp(t.panelBottom, t.background, p)
            drawRoundRect(t.background, rect.topLeft, rect.size, CornerRadius(radius))
            drawRoundRect(Brush.verticalGradient(listOf(top, bottom), rect.top, rect.bottom),
                rect.topLeft, rect.size, CornerRadius(radius))
        }) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val queueRange = heightPx * .55f
        val contentShown: () -> Float = { ((open() - .45f) / .55f).coerceIn(0f, 1f) }

        // ---- The player itself; fades in as it opens, away as the queue rises ----
        Column(Modifier.fillMaxSize()
            .graphicsLayer {
                val shown = contentShown()
                alpha = shown * (1f - queueStage.value * 1.6f).coerceIn(0f, 1f)
                translationY = (1f - shown) * 48.dp.toPx() - queueStage.value * 40.dp.toPx()
            }
            // An upward drag anywhere on the player brings the queue up. A
            // downward one is left alone, for the sheet to close the player on.
            .pointerInput(locked) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val start = awaitVerticalTouchSlopOrCancellation(down.id) { change, over ->
                        if (over < 0f) change.consume()
                    } ?: return@awaitEachGesture
                    var velocity = 0f
                    verticalDrag(start.id) { change ->
                        val dy = change.positionChange().y
                        velocity = dy
                        change.consume()
                        scope.launch { queueStage.snapTo((queueStage.value - dy / queueRange).coerceIn(0f, 1f)) }
                    }
                    settleQueue(if (queueStage.value > .25f || velocity < -12f) 1f else 0f)
                }
            }
            .statusBarsPadding().navigationBarsPadding()) {
            PlayerHeader(song, queue.size, queueIndex, onCollapse, onOpenPlaybackSource, onOpenMenu)
            // The cover's slot; the cover itself is drawn above, where it can
            // travel. Lyrics take the slot's whole height when open.
            Box(Modifier.weight(1f).fillMaxWidth()
                .pointerInput(locked) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val start = awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                            ?: return@awaitEachGesture
                        var total = 0f
                        horizontalDrag(start.id) { change ->
                            total += change.positionChange().x
                            change.consume()
                            scope.launch { swipe.snapTo(total) }
                        }
                        val goes = abs(total) > size.width * .2f
                        scope.launch {
                            if (goes && !locked) {
                                val dir = if (total < 0) -1f else 1f
                                swipe.animateTo(dir * size.width, tween(if (reduce) 0 else 180))
                                if (dir < 0) onNext() else onPrevious()
                                swipe.snapTo(-dir * size.width * .6f)
                            } else if (goes) onBlocked()
                            swipe.animateTo(0f, spring(dampingRatio = .82f, stiffness = 420f))
                        }
                    }
                }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { lyricsOpen = !lyricsOpen },
                contentAlignment = Alignment.Center) {
                Box(Modifier.padding(horizontal = 36.dp, vertical = 12.dp).aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .onGloballyPositioned { coords -> root?.takeIf { it.isAttached }?.let { artFull = it.localBoundingBoxOf(coords, false) } })
                if (lyricsShown > 0f) NamidaLyrics(song, remoteArt, lyrics, lyricsSource, lyricsUnavailable, position, reduce,
                    onSeek = { time -> if (locked) onBlocked() else onSeek(skinLyricsSeek(time, skinPlayerSettings().lyricsOffsetMs.value)) },
                    onProviders = { providersOpen = true },
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp).graphicsLayer { alpha = lyricsShown })
            }
            TrackInfo(song, likeStatus, onToggleLike, onOpenArtist)
            NamidaWaveform(song.videoId, position, durationMs, locked, onBlocked, onSeekFraction,
                Modifier.padding(horizontal = 22.dp, vertical = 6.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(clock(position.positionMs), Modifier.weight(1f), color = t.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                PlayerIcon(NamidaIcons.Previous, "Previous", 32, enabled = hasPrevious, onClick = guard(onPrevious))
                Spacer(Modifier.width(18.dp))
                Spacer(Modifier.size(68.dp).onGloballyPositioned { coords -> root?.takeIf { it.isAttached }?.let { playFull = it.localBoundingBoxOf(coords, false) } })
                Spacer(Modifier.width(18.dp))
                PlayerIcon(NamidaIcons.Next, "Next", 32, enabled = hasNext, onClick = guard(onNext))
                Text(clock(durationMs), Modifier.weight(1f), color = t.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            }
            BottomRow(accountName, repeatMode, lyricsOpen, openOutput, guard(onCycleRepeat), { pipelineOpen = true },
                { lyricsOpen = !lyricsOpen }, { providersOpen = true }, { settleQueue(1f) })
        }

        // ---- The mini player's own row, still there at the start of the growth ----
        MiniReplica(song, open)

        // ---- Queue header: the player folded into one row ----
        QueueHeader(song, topInset, shuffleEnabled, { queueStage.value }, guard(onToggleShuffle), onOpenArtist) { settleQueue(0f) }

        // ---- The cover, travelling between mini player, player and queue header ----
        if (artFull != Rect.Zero) {
            val queueArt = with(density) { Rect(16.dp.toPx(), (topInset + 18.dp).toPx(), (16 + 64).dp.toPx(), (topInset + 82.dp).toPx()) }
            Box(Modifier.layout { measurable, constraints ->
                val f = artFull
                val placeable = measurable.measure(Constraints.fixed(f.width.roundToInt().coerceAtLeast(1), f.height.roundToInt().coerceAtLeast(1)))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeWithLayer(f.left.roundToInt(), f.top.roundToInt()) {
                        val p = open()
                        val q = queueStage.value
                        val mini = NamidaDock.art ?: f
                        val target = mini.lerpTo(f.translate(swipe.value, 0f), p).lerpTo(queueArt, q)
                        val s = target.width / f.width
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = s
                        scaleY = target.height / f.height
                        translationX = target.left - f.left
                        translationY = target.top - f.top
                        val radius = 10.dp.toPx() * (1f - p) + 22.dp.toPx() * p
                        shape = RoundedCornerShape(((radius * (1f - q) + 12.dp.toPx() * q) / s).coerceAtLeast(0f))
                        clip = true
                        shadowElevation = 14.dp.toPx() * p * (1f - q * .6f)
                        alpha = maxOf(1f - lyricsShown, q, 1f - p)
                    }
                }
            }.background(t.card)) {
                AsyncImage(remoteArt ?: song.thumbnailUrl.artworkAt(720), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }

        // ---- The play button, the same one from the mini player to the queue ----
        if (playFull != Rect.Zero) {
            val queuePlay = with(density) { Rect(widthPx - (16 + 48).dp.toPx(), (topInset + 26.dp).toPx(), widthPx - 16.dp.toPx(), (topInset + 74.dp).toPx()) }
            Box(Modifier.layout { measurable, constraints ->
                val f = playFull
                val placeable = measurable.measure(Constraints.fixed(f.width.roundToInt().coerceAtLeast(1), f.height.roundToInt().coerceAtLeast(1)))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.placeWithLayer(f.left.roundToInt(), f.top.roundToInt()) {
                        val p = open()
                        val q = queueStage.value
                        val mini = NamidaDock.play ?: f
                        val target = mini.lerpTo(f, p).lerpTo(queuePlay, q)
                        transformOrigin = TransformOrigin(0f, 0f)
                        scaleX = target.width / f.width
                        scaleY = target.height / f.height
                        translationX = target.left - f.left
                        translationY = target.top - f.top
                    }
                }
            }) {
                NamidaPlayButton(isPlaying, isLoading, 68, guard(onPlayPause))
            }
        }

        // ---- The queue, rising under the folded player ----
        if (queueStage.targetValue > 0f || queueStage.value > 0f) {
            val panelTop = with(density) { (topInset + 96.dp).toPx() }
            val grow = remember(queueRange) {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (available.y < 0f && queueStage.value < 1f) {
                            scope.launch { queueStage.snapTo((queueStage.value - available.y / queueRange).coerceIn(0f, 1f)) }
                            return Offset(0f, available.y)
                        }
                        return Offset.Zero
                    }
                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (queueStage.value in .01f..<.99f) { settleQueue(if (queueStage.value > .5f) 1f else 0f); return available }
                        return Velocity.Zero
                    }
                }
            }
            Box(Modifier.fillMaxSize()
                .graphicsLayer { translationY = panelTop + (heightPx - panelTop) * (1f - queueStage.value) }
                .padding(bottom = with(density) { panelTop.toDp() })
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(t.background.copy(alpha = .96f))
                .nestedScroll(grow)) {
                Column(Modifier.fillMaxSize()) {
                    // The handle: drag it, or tap it, to put the queue away.
                    Box(Modifier.fillMaxWidth().height(22.dp)
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val start = awaitVerticalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() } ?: return@awaitEachGesture
                                var velocity = 0f
                                verticalDrag(start.id) { change ->
                                    val dy = change.positionChange().y
                                    velocity = dy
                                    change.consume()
                                    scope.launch { queueStage.snapTo((queueStage.value - dy / queueRange).coerceIn(0f, 1f)) }
                                }
                                settleQueue(if (queueStage.value < .75f || velocity > 12f) 0f else 1f)
                            }
                        }
                        .clickable { settleQueue(0f) }, contentAlignment = Alignment.Center) {
                        Box(Modifier.size(36.dp, 4.dp).clip(CircleShape).background(t.onBackground.copy(alpha = .25f)))
                    }
                    CompositionLocalProvider(LocalQueueDragActive provides onDragActive) {
                        QueueList(queue, queueIndex, locked, guardIndex(locked, onBlocked, onJumpTo), guardIndex(locked, onBlocked, onRemove),
                            { from, to -> if (locked) onBlocked() else onMove(from, to) }, guard(onClear), guard(onPlayPause),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = bottomInset + 16.dp),
                            header = { requestClear ->
                                QueueTitleRow(queue.size - queueIndex.coerceAtLeast(0), autoplayEnabled, guard(onToggleAutoplay), onOpenTimer, requestClear)
                            }) { offset, _, entry, current, actions ->
                            QueueTile(entry, offset, current, isPlaying, actions.jump, actions.remove, actions.playNext)
                        }
                    }
                }
            }
        }
    }

    if (outputOpen) SkinAudioOutput(haze, accountName, { outputOpen = false }, { pipelineOpen = true }, { castOpen = true })
    if (providersOpen) SkinLyricsProviders(haze, lyricsSource, lyricsProviderStates, onSelectLyricsProvider) { providersOpen = false }
    if (pipelineOpen) SkinAudioPipeline(haze, isPlaying) { pipelineOpen = false }
    if (castOpen) PlayerPlatform.host.CastDialog(haze) { castOpen = false }
    if (lyricsOffsetOpen) SkinLyricsOffset(haze, onDismissLyricsOffset)
}

private fun guardIndex(locked: Boolean, onBlocked: () -> Unit, action: (Int) -> Unit): (Int) -> Unit = { if (locked) onBlocked() else action(it) }

private fun Rect.translate(dx: Float, dy: Float) = Rect(left + dx, top + dy, right + dx, bottom + dy)

/** Namida's times: always two digits of minutes. */
internal fun clock(ms: Long): String {
    val s = (ms.coerceAtLeast(0) / 1000)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}

@Composable
private fun PlayerHeader(song: Song, size: Int, index: Int, onCollapse: () -> Unit, onSource: () -> Unit, onMenu: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        PlayerIcon(NamidaIcons.ArrowDown, "Close player", 26, onClick = onCollapse)
        Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(onClick = onSource).padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (size > 0) Text("${index + 1}/$size", color = t.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(song.playbackSource ?: song.albumName ?: "Now playing", color = t.onBackground, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(t.card).clickable(onClick = onMenu), contentAlignment = Alignment.Center) {
            SkinIcon(NamidaIcons.More, "More", Modifier.size(20.dp), tint = t.onBackground.copy(alpha = .8f))
        }
    }
}

@Composable
private fun TrackInfo(song: Song, like: LikeStatus, onToggleLike: () -> Unit, onOpenArtist: (String?, String) -> Unit) {
    val t = LocalNamidaTones.current
    val liked = like == LikeStatus.LIKE
    val beat by animateFloatAsState(if (liked) 1f else 0f, spring(dampingRatio = .4f, stiffness = 500f), label = "namidaHeart")
    Row(Modifier.fillMaxWidth().padding(start = 28.dp, end = 16.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(song.artist, Modifier.clickable { onOpenArtist(song.artistId, song.artist) }, color = t.onBackground,
                fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.title, Modifier.padding(top = 2.dp), color = t.secondaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onToggleLike), contentAlignment = Alignment.Center) {
            SkinIcon(if (liked) NamidaIcons.HeartFilled else NamidaIcons.Heart, if (liked) "Remove from favourites" else "Add to favourites",
                Modifier.size(30.dp).graphicsLayer { val s = 1f + beat * .12f - (if (liked) 0f else 0f); scaleX = s; scaleY = s },
                tint = if (liked) t.accent else t.onBackground.copy(alpha = .7f))
        }
    }
}

@Composable
private fun PlayerIcon(icon: ImageVector, label: String, sizeDp: Int, enabled: Boolean = true, tint: Color? = null, onClick: () -> Unit) {
    val t = LocalNamidaTones.current
    Box(Modifier.size((sizeDp + 18).dp).clip(CircleShape).clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        SkinIcon(icon, label, Modifier.size(sizeDp.dp), tint = (tint ?: t.onBackground).copy(alpha = if (enabled) .85f else .3f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomRow(account: String?, repeatMode: Int, lyricsOpen: Boolean, onOutput: () -> Unit, onRepeat: () -> Unit,
    onEqualizer: () -> Unit, onLyrics: () -> Unit, onProviders: () -> Unit, onQueue: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.clip(CircleShape).background(t.card).clickable(onClick = onOutput).padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(t.cardRaised), contentAlignment = Alignment.Center) {
                SkinIcon(NamidaIcons.Headphones, null, Modifier.size(18.dp), tint = t.onBackground.copy(alpha = .8f))
            }
            Text(account ?: "Audio", Modifier.padding(start = 10.dp).widthInMax(140.dp), color = t.onBackground, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.weight(1f))
        PlayerIcon(if (repeatMode == RepeatModes.ONE) NamidaIcons.RepeatOne else NamidaIcons.Repeat,
            when (repeatMode) { RepeatModes.ONE -> "Repeat one"; RepeatModes.ALL -> "Repeat all"; else -> "Repeat off" }, 22,
            tint = if (repeatMode != RepeatModes.OFF) t.accent else null, onClick = onRepeat)
        PlayerIcon(NamidaIcons.Equalizer, "Equalizer", 22, onClick = onEqualizer)
        Box(Modifier.size(40.dp).clip(CircleShape).combinedClickable(onClick = onLyrics, onLongClick = onProviders), contentAlignment = Alignment.Center) {
            SkinIcon(NamidaIcons.Lyrics, "Lyrics", Modifier.size(22.dp), tint = if (lyricsOpen) t.accent else t.onBackground.copy(alpha = .85f))
        }
        PlayerIcon(NamidaIcons.Queue, "Queue", 22, onClick = onQueue)
    }
}

private fun Modifier.widthInMax(max: Dp) = this.then(Modifier.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(maxWidth = minOf(constraints.maxWidth, max.roundToPx())))
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
})

/** The mini player's text and side buttons, drawn where the mini player is, fading as the player takes over. */
@Composable
private fun MiniReplica(song: Song, open: () -> Float) {
    val t = LocalNamidaTones.current
    Box(Modifier.layout { measurable, constraints ->
        val r = NamidaDock.panel
        if (r == null) return@layout layout(0, 0) {}
        val placeable = measurable.measure(Constraints.fixed(r.width.roundToInt().coerceAtLeast(1), r.height.roundToInt().coerceAtLeast(1)))
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeWithLayer(r.left.roundToInt(), r.top.roundToInt()) { alpha = (1f - open() * 3.5f).coerceIn(0f, 1f) }
        }
    }) {
        Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.size(MINI_ART.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp, end = 6.dp)) {
                Text(song.artist, color = t.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.title, Modifier.padding(top = 2.dp), color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { SkinIcon(NamidaIcons.Previous, null, Modifier.size(21.dp), tint = t.onBackground.copy(alpha = .85f)) }
            Spacer(Modifier.size(44.dp))
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { SkinIcon(NamidaIcons.Next, null, Modifier.size(21.dp), tint = t.onBackground.copy(alpha = .85f)) }
        }
    }
}

@Composable
private fun QueueHeader(song: Song, topInset: Dp, shuffle: Boolean, stage: () -> Float, onShuffle: () -> Unit,
    onOpenArtist: (String?, String) -> Unit, onClose: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().padding(top = topInset + 18.dp).height(64.dp)
        .graphicsLayer { val q = stage(); alpha = ((q - .4f) / .6f).coerceIn(0f, 1f); translationY = (1f - q) * 24.dp.toPx() }
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width((16 + 64 + 14).dp))
        Column(Modifier.weight(1f)) {
            Text(song.artist, Modifier.clickable { onOpenArtist(song.artistId, song.artist) }, color = t.onBackground, fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.title, color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        PlayerIcon(NamidaIcons.Shuffle, if (shuffle) "Shuffle on" else "Shuffle off", 22, tint = if (shuffle) t.accent else null, onClick = onShuffle)
        Spacer(Modifier.width((48 + 16 + 4).dp))
    }
}

@Composable
private fun QueueTitleRow(upcoming: Int, autoplay: Boolean, onAutoplay: () -> Unit, onTimer: () -> Unit, onClear: () -> Unit) {
    val t = LocalNamidaTones.current
    Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        SkinIcon(NamidaIcons.Queue, null, Modifier.size(20.dp), tint = t.onBackground.copy(alpha = .8f))
        Text("Queue • $upcoming", Modifier.padding(start = 10.dp).weight(1f), color = t.onBackground, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Chip(if (autoplay) "Autoplay" else "Autoplay off", autoplay, onAutoplay)
        PlayerIcon(NamidaIcons.Timer, "Sleep timer", 20, onClick = onTimer)
        PlayerIcon(NamidaIcons.Delete, "Clear queue", 20, onClick = onClear)
    }
}

@Composable
internal fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    val t = LocalNamidaTones.current
    Text(label, Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) t.indicator else t.card).clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 6.dp), color = t.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

/** A row of the queue: Namida's track tile, the playing one washed in the track colour. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QueueTile(song: Song, offset: Int, current: Boolean, playing: Boolean, onClick: () -> Unit, onRemove: () -> Unit, onPlayNext: () -> Unit) {
    val t = LocalNamidaTones.current
    var menu by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(14.dp))
        .background(if (current) t.indicator.copy(alpha = .55f) else Color.Transparent)
        .combinedClickable(onClick = onClick, onLongClick = { menu = true })
        .padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)).background(t.card), contentAlignment = Alignment.Center) {
            AsyncImage(song.thumbnailUrl.artworkAt(240), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (current) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .35f)), contentAlignment = Alignment.Center) {
                PlayingBars(playing, Color.White)
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(song.title, color = t.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            song.albumName?.let { Text(it, color = t.tertiaryText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
        Column(horizontalAlignment = Alignment.End) {
            song.durationText?.let { Text(it, color = t.secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
            if (offset > 0) SkinIcon(NamidaIcons.DragHandle, "Hold to move", Modifier.padding(top = 4.dp).size(20.dp), tint = t.tertiaryText)
        }
        androidx.compose.material3.DropdownMenu(menu, { menu = false }) {
            if (offset > 1) androidx.compose.material3.DropdownMenuItem(text = { Text("Play next") }, onClick = { menu = false; onPlayNext() })
            if (offset > 0) androidx.compose.material3.DropdownMenuItem(text = { Text("Remove from queue") }, onClick = { menu = false; onRemove() })
        }
    }
}

/** Three bars bouncing while the track plays, still while it's paused. */
@Composable
internal fun PlayingBars(playing: Boolean, color: Color, modifier: Modifier = Modifier) {
    val phase = remember { Animatable(0f) }
    LaunchedEffect(playing) {
        if (playing) while (true) phase.animateTo(phase.value + 1f, tween(900, easing = androidx.compose.animation.core.LinearEasing))
    }
    Canvas(modifier.size(18.dp)) {
        val w = size.width / 5
        for (i in 0..2) {
            val h = size.height * (.35f + .65f * ((sin((phase.value + i * .33f) * 6.283f) + 1f) / 2f).let { if (playing) it else .4f })
            drawLine(color, Offset(w * (i * 2 + .5f), size.height), Offset(w * (i * 2 + .5f), size.height - h), w * .9f, StrokeCap.Round)
        }
    }
}

/**
 * Namida's waveform seek bar. The host doesn't decode a waveform, so the bars
 * are shaped from the track id instead — the same track always gets the same
 * outline — but they behave like Namida's: tap or drag anywhere to seek.
 */
@Composable
internal fun NamidaWaveform(trackId: String, position: PlaybackPosition, durationMs: Long, locked: Boolean, onBlocked: () -> Unit,
    onSeekFraction: (Float) -> Unit, modifier: Modifier = Modifier) {
    val t = LocalNamidaTones.current
    val bars = remember(trackId) { waveformFor(trackId, 96) }
    var drag by remember(trackId) { mutableStateOf<Float?>(null) }
    val fraction = drag ?: if (durationMs > 0) (position.positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val grow by animateFloatAsState(if (drag != null) 1.12f else 1f, spring(stiffness = 600f), label = "namidaWaveGrow")
    Canvas(modifier.fillMaxWidth().height(64.dp)
        .pointerInput(locked, durationMs) {
            awaitEachGesture {
                val down = awaitFirstDown()
                if (locked || durationMs <= 0) { onBlocked(); return@awaitEachGesture }
                drag = (down.position.x / size.width).coerceIn(0f, 1f)
                var pointer = down
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == pointer.id } ?: break
                    if (!change.pressed) break
                    change.consume()
                    drag = (change.position.x / size.width).coerceIn(0f, 1f)
                    pointer = change
                }
                drag?.let(onSeekFraction)
                drag = null
            }
        }) {
        val step = size.width / bars.size
        val bar = step * .5f
        val mid = size.height / 2
        bars.forEachIndexed { i, amp ->
            val x = step * i + step / 2
            val h = (size.height * .9f * amp * grow).coerceAtLeast(bar)
            val played = (i + .5f) / bars.size <= fraction
            drawLine(if (played) t.onBackground.copy(alpha = .78f) else t.onBackground.copy(alpha = .22f),
                Offset(x, mid - h / 2), Offset(x, mid + h / 2), bar, StrokeCap.Round)
        }
    }
}

private fun waveformFor(id: String, count: Int): List<Float> {
    var seed = id.hashCode().toLong() xor 0x5DEECE66DL
    fun next(): Float { seed = (seed * 6364136223846793005L + 1442695040888963407L); return ((seed ushr 33) and 0xFFFF).toFloat() / 0xFFFF }
    val raw = List(count) { next() }
    // Smoothed and given a loudness curve over the track, like music rather than noise.
    return List(count) { i ->
        val near = (-2..2).map { raw[(i + it).coerceIn(0, count - 1)] }.average().toFloat()
        val section = .55f + .45f * ((sin(i / count.toFloat() * 9.4f + raw[0] * 6f) + 1f) / 2f)
        (.18f + .82f * near * section).coerceIn(.12f, 1f)
    }
}

/**
 * Lyrics where the cover was, over the cover blurred: the line being sung on a
 * pill of the track colour, the rest centred around it and fading at the edges.
 */
@Composable
private fun NamidaLyrics(song: Song, art: String?, lines: List<LyricLine>?, source: LyricsSource?, unavailable: Boolean,
    position: PlaybackPosition, reduce: Boolean, onSeek: (Long) -> Unit, onProviders: () -> Unit, modifier: Modifier) {
    val t = LocalNamidaTones.current
    val translation = rememberSkinLyricsTranslation(song.videoId, lines, source, unavailable, "Loading lyrics…", rememberHaptics())
    val offset by skinPlayerSettings().lyricsOffsetMs.collectAsStateWithLifecycle()
    Box(modifier.clip(RoundedCornerShape(24.dp))) {
        AsyncImage(art, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().blur(36.dp).graphicsLayer { alpha = .55f })
        Box(Modifier.fillMaxSize().background(t.background.copy(alpha = .35f)))
        if (lines.isNullOrEmpty()) {
            Box(Modifier.fillMaxSize().clickable(onClick = onProviders), contentAlignment = Alignment.Center) {
                if (unavailable) Text("No lyrics found", color = t.secondaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                else CircularProgressIndicator(Modifier.size(28.dp), color = t.onBackground.copy(alpha = .6f), strokeWidth = 2.dp)
            }
            return@Box
        }
        val list = rememberLazyListState()
        val timed = remember(lines) { lines.any { it.timeMs > 0 } }
        val active by remember(lines, offset) {
            derivedStateOf { if (!timed) -1 else lines.indexOfLast { it.timeMs >= 0 && it.timeMs <= skinLyricsPosition(position.positionMs, offset) } }
        }
        var touchedAt by remember { mutableLongStateOf(0L) }
        LaunchedEffect(list.isScrollInProgress) { if (list.isScrollInProgress) touchedAt = System.currentTimeMillis() }
        LaunchedEffect(song.videoId, active) {
            if (active < 0 || System.currentTimeMillis() - touchedAt < 3500) return@LaunchedEffect
            val center = list.layoutInfo.viewportEndOffset / 2
            val visible = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == active }
            if (visible != null && !reduce) list.animateScrollBy((visible.offset + visible.size / 2 - center).toFloat(), tween(650, easing = FastOutSlowInEasing))
            else list.scrollToItem(active, -center + 40)
        }
        LazyColumn(Modifier.fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(Brush.verticalGradient(0f to Color.Transparent, .18f to Color.Black, .82f to Color.Black, 1f to Color.Transparent),
                    blendMode = BlendMode.DstIn)
            }, state = list, horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 120.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(lines) { index, line ->
                val now = index == active
                val pill by animateFloatAsState(if (now) 1f else 0f, tween(if (reduce) 0 else 300), label = "namidaLyricPill")
                val alpha = when { !timed -> .9f; now -> 1f; index < active -> .5f; else -> .78f }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(t.main.copy(alpha = .75f * pill))
                    .clickable(enabled = line.timeMs >= 0) { onSeek(line.timeMs) }
                    .padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(line.text.ifBlank { "•••" }, color = t.onBackground.copy(alpha = alpha), fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    translation.subLines?.getOrNull(index)?.text?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = t.secondaryText.copy(alpha = alpha * .9f), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Row(Modifier.align(Alignment.TopEnd).padding(10.dp)) {
            Chip(if (translation.showingTranslation) "Original" else "Translate", translation.showingTranslation, translation.toggleTranslation)
        }
    }
}
