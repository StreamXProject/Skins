package com.music.streamx.skins.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.roundToInt
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.model.Song
import com.music.streamx.ui.skins.skinLyricsPosition

/** What a queue row may do; the shell has already routed locked controls to the host's notice. */
class QueueRowActions(val jump: () -> Unit, val playNext: () -> Unit, val remove: () -> Unit, val playPause: () -> Unit)

/**
 * The scrolling, auto-following part of a lyrics page, so a skin only draws
 * one line. Follows the playhead until the listener scrolls, and resumes four
 * seconds after they let go. [active] is the current line's index, or -1 before the first.
 */
@Composable
fun LyricsScroller(lines: List<LyricLine>, positionMs: Long, offsetMs: Int, reduceMotion: Boolean, modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 24.dp), spacing: androidx.compose.ui.unit.Dp = 16.dp,
    state: LazyListState = rememberLazyListState(), centerOffsetPx: Int = -220,
    line: @Composable (index: Int, line: LyricLine, state: LineState) -> Unit) {
    val active = lines.indexOfLast { it.timeMs <= skinLyricsPosition(positionMs, offsetMs) }
    var userScrolledAt by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state.isScrollInProgress) { if (state.isScrollInProgress) userScrolledAt = System.currentTimeMillis() }
    LaunchedEffect(active) {
        if (active < 0 || System.currentTimeMillis() - userScrolledAt < 4000) return@LaunchedEffect
        if (reduceMotion) state.scrollToItem(active, centerOffsetPx) else state.animateScrollToItem(active, centerOffsetPx)
    }
    LazyColumn(state = state, modifier = modifier.fillMaxSize().nestedScroll(PlayerScrollBoundary), contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(spacing)) {
        itemsIndexed(lines) { index, l -> line(index, l, when { index == active -> LineState.ACTIVE; index < active -> LineState.SUNG; else -> LineState.UPCOMING }) }
        item { Spacer(Modifier.height(200.dp)) }
    }
}

enum class LineState { SUNG, ACTIVE, UPCOMING }

/** The empty and loading states every lyrics page shares; returns true when there is nothing else to draw. */
@Composable
fun LyricsEmpty(lines: List<LyricLine>?, unavailable: Boolean, modifier: Modifier = Modifier, message: String = "No lyrics for this one.", searching: String = "Finding the words…"): Boolean {
    if (!lines.isNullOrEmpty()) return false
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (unavailable) message else searching, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    return true
}

/** Set by the player shell: tells the host sheet to stay put while a queue row is being dragged. */
val LocalQueueDragActive = staticCompositionLocalOf<(Boolean) -> Unit> { {} }

/**
 * The list half of a queue page: the current track first, then what follows,
 * each row drawn by the skin, with the clear-queue confirmation handled here.
 * [header] draws above the rows and receives a `clear` request it may wire to a button.
 *
 * Hold any upcoming track to pick it up, drag it, and let go to drop it there.
 * The hold is watched ahead of the row's own taps, so the skin's rows need no
 * handle; a quick swipe still scrolls and a tap still jumps.
 */
@Composable
fun QueueList(queue: List<Song>, currentIndex: Int, locked: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onClear: () -> Unit, onPlayPause: () -> Unit, modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    header: @Composable (requestClear: () -> Unit) -> Unit = {},
    row: @Composable (offset: Int, index: Int, song: Song, current: Boolean, actions: QueueRowActions) -> Unit) {
    var clear by remember { mutableStateOf(false) }
    val start = currentIndex.coerceAtLeast(0)
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val drag = remember(listState) { QueueListDrag(listState) }
    drag.onMove = onMove
    drag.onActiveChange = LocalQueueDragActive.current
    drag.tick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    drag.update(remember(queue, start) { queueRows(queue, start) }, start)
    with(LocalDensity.current) { drag.edgeZone = 72.dp.toPx(); drag.edgeSpeed = 900.dp.toPx() }
    LaunchedEffect(locked) { if (locked) drag.cancel() }
    DisposableEffect(drag) { onDispose { drag.cancel() } }
    // A move the player never echoes back shouldn't leave the list in an order it doesn't have.
    LaunchedEffect(drag, drag.waiting) { if (drag.waiting) { delay(1500); drag.giveUp() } }
    // Held near either edge, the list scrolls under the row; the finger stays
    // put, so each frame is settled as if the rows had moved past it.
    val direction = drag.scrollDir
    LaunchedEffect(drag, direction) {
        if (direction == 0) return@LaunchedEffect
        var previous = withFrameNanos { it }
        var ended = false
        while (!ended) {
            // Pinning the list's place on a swap cancels the scroll it lands in;
            // the loop picks straight back up rather than leaving the row stuck.
            try {
                listState.scroll {
                    while (true) {
                        val now = withFrameNanos { it }
                        val seconds = ((now - previous) / 1_000_000_000f).coerceAtMost(1f / 30f)
                        previous = now
                        val moved = scrollBy(drag.scrollSpeed * seconds)
                        drag.settle(0f)
                        if (moved == 0f && !(if (direction < 0) listState.canScrollBackward else listState.canScrollForward)) break
                    }
                }
                ended = true
            } catch (e: CancellationException) {
                ensureActive()
            }
        }
    }
    val layoutDirection = LocalLayoutDirection.current
    Column(modifier.fillMaxSize()) {
        header { if (!locked) clear = true }
        Box(Modifier.weight(1f).fillMaxWidth()
            // Once a row is picked up the list carries the gesture, not the row:
            // the row's own slot can scroll out of view, which ended the drag.
            .pointerInput(drag) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    try {
                        while (true) {
                            val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
                            if (drag.held == null) { if (change.pressed) continue else break }
                            val dy = change.positionChange().y
                            change.consume()
                            if (!change.pressed) break
                            drag.settle(dy)
                        }
                    } finally { drag.drop() }
                }
            }) {
            LazyColumn(Modifier.fillMaxSize().nestedScroll(PlayerScrollBoundary), state = listState, contentPadding = contentPadding) {
                itemsIndexed(drag.rows, key = { _, r -> r.key }) { offset, r ->
                    val index = start + offset
                    Box(Modifier
                        // The held row stays in the list as an empty slot that trades
                        // places with its neighbours; the copy that's seen follows the finger.
                        .then(if (drag.held?.key == r.key) Modifier.alpha(0f) else Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null))
                        .pointerInput(locked, offset == 0, r.key) {
                            if (locked || offset == 0) return@pointerInput
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                // Wait out the long-press; any lift or real movement first means "not a drag".
                                val ended = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                    var gone = false
                                    while (!gone) {
                                        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }
                                        gone = change == null || !change.pressed || (change.position - down.position).getDistance() > viewConfiguration.touchSlop
                                    }
                                    true
                                }
                                if (ended == null && drag.begin(r.key)) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }) {
                        row(offset, index, r.song, offset == 0, QueueRowActions(
                            jump = { if (!locked) { if (offset == 0) onPlayPause() else onJump(index) } },
                            playNext = { if (!locked && offset > 1) onMove(index, start + 1) },
                            remove = { if (!locked) onRemove(index) },
                            playPause = { if (!locked) onPlayPause() }))
                    }
                }
            }
            drag.held?.let { held ->
                val offset = drag.rows.indexOfFirst { it.key == held.key }
                Box(Modifier.matchParentSize().padding(start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection))) {
                    Box(Modifier
                        .offset { IntOffset(0, (drag.heldTop - listState.layoutInfo.viewportStartOffset).roundToInt()) }
                        .graphicsLayer {
                            scaleX = 1.03f; scaleY = 1.03f
                            shadowElevation = 16.dp.toPx()
                            shape = RoundedCornerShape(12.dp); clip = false
                        }
                        // Rows are drawn on the skin's own panel; lifted off it, the copy
                        // needs a backing or its text sits on top of the rows underneath.
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))) {
                        row(offset, start + offset, held.song, false, QueueRowActions({}, {}, {}, {}))
                    }
                }
            }
        }
    }
    if (clear) AlertDialog(onDismissRequest = { clear = false }, title = { Text("Clear the queue?") },
        text = { Text("Upcoming tracks will be removed. The current track keeps playing.") },
        confirmButton = { TextButton({ clear = false; onClear() }) { Text("Clear") } },
        dismissButton = { TextButton({ clear = false }) { Text("Keep") } })
}

/** One row of [QueueList], keyed by its queue entry so it keeps its identity through a reorder. */
private class QueueListRow(val key: String, val song: Song)

private fun queueRows(queue: List<Song>, start: Int): List<QueueListRow> {
    // Entries without an id fall back to the song and how many times it has come
    // up so far — never the position, which changes on every move.
    val seen = HashMap<String, Int>()
    val used = HashSet<String>()
    return queue.drop(start).map { song ->
        val occurrence = seen.getOrDefault(song.videoId, 0).also { seen[song.videoId] = it + 1 }
        val base = song.queueEntryId ?: "${song.videoId}#$occurrence"
        var key = base
        var repeat = 1
        while (!used.add(key)) key = base + "~" + repeat++
        QueueListRow(key, song)
    }
}

/**
 * The one drag a [QueueList] can have at a time. The finger alone moves the
 * held row's centre in the viewport; swaps are read off the live layout
 * against it and made in [rows] at once, and the player hears of the whole
 * move once, on drop. Until it plays the move back, [rows] keeps the new order
 * rather than snapping the row back to where it came from.
 */
private class QueueListDrag(private val listState: LazyListState) {
    var rows by mutableStateOf(emptyList<QueueListRow>())
        private set
    private var live = emptyList<QueueListRow>()
    private var start = 0
    var held by mutableStateOf<QueueListRow?>(null)
        private set
    /** Where the copy under the finger is drawn: its top in the viewport, kept on screen. */
    var heldTop by mutableFloatStateOf(0f)
        private set
    var waiting by mutableStateOf(false)
        private set
    /** -1, 0 or 1 — state, because it starts and stops the scroll loop. */
    var scrollDir by mutableIntStateOf(0)
        private set
    /** Signed, px a second; only the loop reads it. */
    var scrollSpeed = 0f
        private set
    var onMove: (Int, Int) -> Unit = { _, _ -> }
    var onActiveChange: (Boolean) -> Unit = {}
    var tick: () -> Unit = {}
    var edgeZone = 0f
    var edgeSpeed = 0f
    private var heldCentre = 0f
    private var heldSize = 0
    /** Where the last swap will put the held row, until the list lays it out there. */
    private var awaiting: Int? = null

    fun update(next: List<QueueListRow>, start: Int) {
        if (next === live && start == this.start) return
        live = next
        this.start = start
        if (held != null) {
            // The queue changed under the finger (a track ended, one was added):
            // the positions a drop would send are no longer the ones it picked up.
            if (next.map { it.key }.toSet() != rows.map { it.key }.toSet()) cancel()
            return
        }
        if (waiting) {
            val keys = next.map { it.key }
            if (keys != rows.map { it.key } && keys.toSet() == rows.map { it.key }.toSet()) return
        }
        waiting = false
        rows = next
    }

    fun begin(key: String): Boolean {
        if (held != null || waiting) return false
        val row = rows.firstOrNull { it.key == key } ?: return false
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return false
        heldSize = item.size
        heldCentre = item.offset + item.size / 2f
        heldTop = item.offset.toFloat()
        awaiting = null
        held = row
        onActiveChange(true)
        return true
    }

    fun settle(dy: Float) {
        val key = held?.key ?: return
        val info = listState.layoutInfo
        val items = info.visibleItemsInfo
        val half = heldSize / 2f
        // Kept between the first upcoming row and the last, where those are on
        // screen: nothing goes above the playing track or past the end.
        var centre = heldCentre + dy
        rows.getOrNull(1)?.let { first ->
            items.firstOrNull { it.key == first.key }?.let { centre = centre.coerceAtLeast(it.offset + half) }
        }
        rows.lastOrNull()?.let { last ->
            items.firstOrNull { it.key == last.key }?.let { centre = centre.coerceAtMost(it.offset + it.size - half) }
        }
        heldCentre = centre
        val top = centre - half
        val minTop = info.viewportStartOffset.toFloat()
        heldTop = top.coerceIn(minTop, (info.viewportEndOffset - heldSize).toFloat().coerceAtLeast(minTop))
        aim(top, key)

        val me = items.firstOrNull { it.key == key } ?: return
        // A swap already made but not laid out yet: deciding another off the old
        // layout would trade the row straight back.
        awaiting?.let {
            if (me.index != it) return
            awaiting = null
        }
        val target = items
            .filter { it.key != key && it.index >= 1 && it.index < rows.size }
            .minByOrNull { abs(it.offset + it.size / 2f - centre) } ?: return
        // Half a row of overlap per swap, so a row isn't traded back and forth over a pixel.
        if (abs(centre - (target.offset + target.size / 2f)) > target.size / 2f) return
        // LazyColumn keeps its place by its first visible row's key; moving that
        // row would drag the whole list along with it, so the position is pinned.
        val first = listState.firstVisibleItemIndex
        val firstOffset = listState.firstVisibleItemScrollOffset
        rows = rows.toMutableList().apply { add(target.index, removeAt(me.index)) }
        if (me.index == first || target.index == first) listState.requestScrollToItem(first, firstOffset)
        awaiting = target.index
        tick()
    }

    /** Only while there's a row that way to trade with, and list left to scroll. */
    private fun aim(top: Float, key: String) {
        val info = listState.layoutInfo
        val startEdge = info.viewportStartOffset + edgeZone
        val endEdge = info.viewportEndOffset - edgeZone
        val speed = when {
            top < startEdge -> -edgeSpeed * ((startEdge - top) / edgeZone).coerceIn(.2f, 1f)
            top + heldSize > endEdge -> edgeSpeed * ((top + heldSize - endEdge) / edgeZone).coerceIn(.2f, 1f)
            else -> 0f
        }
        val blocked = when {
            speed < 0f -> rows.getOrNull(1)?.key == key || !listState.canScrollBackward
            speed > 0f -> rows.lastOrNull()?.key == key || !listState.canScrollForward
            else -> true
        }
        setScroll(if (blocked) 0f else speed)
    }

    private fun setScroll(speed: Float) {
        scrollSpeed = speed
        val direction = if (speed > 0f) 1 else if (speed < 0f) -1 else 0
        if (scrollDir != direction) scrollDir = direction
    }

    /** Lets go: the whole move goes to the player as one. */
    fun drop() {
        val row = held ?: return
        held = null
        awaiting = null
        setScroll(0f)
        onActiveChange(false)
        val from = live.indexOfFirst { it.key == row.key }
        val to = rows.indexOfFirst { it.key == row.key }
        if (from < 1 || to < 1 || from == to) {
            rows = live
            return
        }
        waiting = true
        onMove(start + from, start + to)
    }

    /** Puts the row back where it was, sending nothing. */
    fun cancel() {
        if (held == null) return
        held = null
        awaiting = null
        setScroll(0f)
        onActiveChange(false)
        waiting = false
        rows = live
    }

    fun giveUp() {
        if (held != null || !waiting) return
        waiting = false
        rows = live
    }
}
