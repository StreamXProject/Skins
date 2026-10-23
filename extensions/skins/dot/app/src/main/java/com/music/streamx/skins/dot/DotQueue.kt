package com.music.streamx.skins.dot

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.data.model.Song
import com.music.streamx.ui.skins.skinPlayerSettings
import kotlin.math.roundToInt

private val DotQueueScrollBoundary = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = Offset(0f, available.y)
    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = Velocity(0f, available.y)
}

internal fun dotQueueTarget(from: Int, rows: Int, current: Int, size: Int): Int? {
    if (from <= current || from !in 0 until size) return null
    return (from.toLong() + rows).coerceIn(current + 1L, size - 1L).toInt().takeIf { it != from }
}

private fun queueShift(index: Int, from: Int, to: Int) = when {
    from < to && index > from && index <= to -> -1f
    from > to && index >= to && index < from -> 1f
    else -> 0f
}

@Composable
internal fun DotQueue(
    queue: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onJump: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDragActive: (Boolean) -> Unit,
    onClear: () -> Unit,
    onPlay: () -> Unit,
    locked: Boolean,
    autoplay: Boolean,
    onAutoplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val list = rememberLazyListState()
    val rowPixels = with(LocalDensity.current) { 57.dp.toPx() }

    val keys = remember(queue) {
        val counts = mutableMapOf<String, Int>()
        queue.map { it.queueEntryId ?: "${it.videoId}#${counts.merge(it.videoId, 1, Int::plus)}" }
    }
    val latestKeys by rememberUpdatedState(keys)
    val latestCurrent by rememberUpdatedState(currentIndex)
    val latestMove by rememberUpdatedState(onMove)
    val dragChanged by rememberUpdatedState(onDragActive)

    var dragKey by remember { mutableStateOf<String?>(null) }
    var from by remember { mutableIntStateOf(-1) }
    var offset by remember { mutableFloatStateOf(0f) }

    fun cancelDrag() {
        dragKey = null
        from = -1
        offset = 0f
        dragChanged(false)
    }

    DisposableEffect(Unit) { onDispose { dragChanged(false) } }
    LaunchedEffect(currentIndex, locked) { if (locked || from <= currentIndex) cancelDrag() }
    LaunchedEffect(keys) { if (dragKey != null && dragKey !in keys) cancelDrag() }

    Column(modifier = modifier.fillMaxSize().nestedScroll(DotQueueScrollBoundary)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "// UP NEXT [${queue.size} TRACKS]",
                style = MaterialTheme.typography.labelMedium,
                color = c.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TuiKey(
                label = if (autoplay) "AUTOPLAY:ON" else "AUTOPLAY:OFF",
                accent = autoplay,
                onClick = onAutoplay,
            )
            Spacer(Modifier.width(8.dp))
            TuiKey(
                label = "[ CLEAR ]",
                onClick = onClear,
            )
        }
        HorizontalDivider(color = c.outline)

        LazyColumn(
            state = list,
            overscrollEffect = null,
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(queue, key = { index, _ -> keys[index] }) { index, song ->
                val isCurrent = index == currentIndex
                val isDragging = keys[index] == dragKey
                val shiftTarget = if (dragKey != null && !isDragging) {
                    val target = dotQueueTarget(from, (offset / rowPixels).roundToInt(), latestCurrent, queue.size) ?: from
                    queueShift(index, from, target)
                } else {
                    0f
                }
                val shiftPx by animateFloatAsState(
                    targetValue = shiftTarget * rowPixels,
                    animationSpec = tween(if (reduce) 0 else 160),
                    label = "Queue shift",
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .zIndex(if (isDragging) 2f else 0f)
                        .graphicsLayer { translationY = if (isDragging) offset else shiftPx }
                        .background(if (isCurrent) c.surfaceVariant else c.background)
                        .border(
                            1.dp,
                            if (isDragging) acc else if (isCurrent) acc.copy(alpha = 0.5f) else Color.Transparent,
                            Edge,
                        )
                        .tuiClickable { if (!locked && dragKey == null) { if (isCurrent) onPlay() else onJump(index) } }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isCurrent) ">" else " ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = acc,
                    )
                    Text(
                        text = (index + 1).toString().padStart(3, '0'),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCurrent) acc else c.outlineVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 10.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCurrent) "${song.title}_" else song.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (isCurrent) acc else c.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${song.artist} · ${song.durationText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) c.onSurface else c.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (!isCurrent) {
                        Text(
                            text = "[X]",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.onSurfaceVariant,
                            modifier = Modifier
                                .tuiClickable { if (!locked) onRemove(index) }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                        Text(
                            text = "::",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isDragging) acc else c.onSurfaceVariant,
                            modifier = Modifier
                                .pointerInput(keys[index], locked) {
                                    if (!locked && index > latestCurrent) detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            from = index
                                            dragKey = latestKeys[index]
                                            offset = 0f
                                            dragChanged(true)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offset += dragAmount.y
                                        },
                                        onDragEnd = {
                                            val origin = latestKeys.indexOf(dragKey)
                                            val target = dotQueueTarget(origin, (offset / rowPixels).roundToInt(), latestCurrent, latestKeys.size)
                                            if (target != null) latestMove(origin, target)
                                            cancelDrag()
                                        },
                                        onDragCancel = { cancelDrag() },
                                    )
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }
                }
                HorizontalDivider(color = c.outline)
            }
        }
    }
}
