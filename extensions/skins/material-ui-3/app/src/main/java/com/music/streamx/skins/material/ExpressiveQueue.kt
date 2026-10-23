package com.music.streamx.skins.material

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlin.math.roundToInt
import com.music.streamx.data.model.*

import com.music.streamx.ui.skins.*

// Keep list-edge overscroll/fling from handing a downward gesture to the sheet.
// Header drags still reach the sheet's own gesture handler.
private val QueueScrollBoundary = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        Offset(0f, available.y.coerceAtLeast(0f))
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
        Velocity(0f, available.y)
}

/** One expanded viewport keeps list scrolling independent from sheet anchor movement. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpressiveQueueSheet(song: Song, queue: List<Song>, index: Int, autoplay: Boolean, shuffle: Boolean,
    playing: Boolean, loading: Boolean, locked: Boolean, reduce: Boolean, windowHeight: Dp,
    onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onDragActive: (Boolean) -> Unit,
    onClear: () -> Unit, onAutoplay: () -> Unit, onShuffle: () -> Unit, onTimer: () -> Unit,
    onPlayPause: () -> Unit, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val maximum = (windowHeight - topInset - 20.dp).coerceAtLeast(280.dp)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
        }) {
        Box(Modifier.fillMaxWidth().height(maximum)) {
            ExpressiveQueueContent(song, queue, index, autoplay, shuffle, playing, loading, locked, reduce,
                onJump, onRemove, onMove, onDragActive, onClear, onAutoplay, onShuffle, onTimer, onPlayPause,
                Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpressiveQueueContent(song: Song, queue: List<Song>, index: Int, autoplay: Boolean, shuffle: Boolean,
    playing: Boolean, loading: Boolean, locked: Boolean, reduce: Boolean,
    onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onAutoplay: () -> Unit,
    onShuffle: () -> Unit, onTimer: () -> Unit, onPlayPause: () -> Unit, modifier: Modifier) {
    var dragKey by remember { mutableStateOf<String?>(null) }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var optionsOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val rowHeight = with(LocalDensity.current) { 72.dp.toPx() }
    val keys = remember(queue) { val occurrences = mutableMapOf<String, Int>(); queue.map { "${it.videoId}#${occurrences.merge(it.videoId, 1, Int::plus)}" } }
    val currentKeys by rememberUpdatedState(keys)
    val currentQueue by rememberUpdatedState(queue)
    val currentIndex by rememberUpdatedState(index)
    val move by rememberUpdatedState(onMove)
    val draggingChanged by rememberUpdatedState(onDragActive)
    val remove by rememberUpdatedState(onRemove)
    DisposableEffect(Unit) { onDispose { draggingChanged(false) } }
    LaunchedEffect(locked, index) { if (locked || dragIndex <= index) { dragKey = null; dragIndex = -1; dragOffset = 0f; draggingChanged(false) } }
    Column(modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Queue", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)
            Box {
                IconButton(onClick = { optionsOpen = true }) { Icon(ExpressiveSymbols.MoreHoriz, "Queue options", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                DropdownMenu(expanded = optionsOpen, onDismissRequest = { optionsOpen = false }) {
                    DropdownMenuItem(text = { Text("Clear queue") }, enabled = !locked, onClick = { optionsOpen = false; onClear() })
                    DropdownMenuItem(text = { Text(if (autoplay) "Turn autoplay off" else "Turn autoplay on") }, enabled = !locked,
                        onClick = { optionsOpen = false; onAutoplay() })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
            FilterChip(selected=shuffle,onClick=onShuffle,enabled=!locked,label={Text("Shuffle")},leadingIcon={Icon(ExpressiveSymbols.Shuffle,null,Modifier.size(18.dp))})
            AssistChip(onClick=onTimer,label={Text("Sleep timer")},leadingIcon={Icon(ExpressiveSymbols.Timer,null,Modifier.size(18.dp))})
        }
        ElevatedCard(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),shape=MaterialTheme.shapes.large,
            colors=CardDefaults.elevatedCardColors(containerColor=MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Text("NOW PLAYING",Modifier.padding(start=16.dp,top=16.dp),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
            ExpressiveQueueRow(queue.getOrNull(index) ?: song,true,{},background=MaterialTheme.colorScheme.surfaceContainerHigh,trailing={
                FilledIconButton(onClick=onPlayPause,enabled=!locked,modifier=Modifier.size(48.dp)) {
                    if(loading) CircularProgressIndicator(Modifier.size(20.dp),color=MaterialTheme.colorScheme.onPrimary,strokeWidth=2.dp)
                    else Icon(if(playing) ExpressiveSymbols.Pause else ExpressiveSymbols.PlayArrow,if(playing) "Pause" else "Play")
                }
            })
        }
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("Up next",Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
            Text("${(queue.size-index-1).coerceAtLeast(0)} tracks",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(Modifier.fillMaxWidth().weight(1f).nestedScroll(QueueScrollBoundary).clip(RoundedCornerShape(0.dp)), state = listState, contentPadding = PaddingValues(bottom = 8.dp)) {
            itemsIndexed(queue.drop((index + 1).coerceAtLeast(0)), key = { offset, _ -> keys[offset + (index + 1).coerceAtLeast(0)] }) { offset, entry ->
                val absolute = offset + (index + 1).coerceAtLeast(0)
                val key = keys[absolute]
                val liveAbsolute by rememberUpdatedState(absolute)
                var rowMenu by remember { mutableStateOf(false) }
                val previewTarget = if (dragKey != null) {
                    expressiveQueueDropTarget(dragIndex, (dragOffset / rowHeight).roundToInt(), index, queue.size) ?: dragIndex
                } else -1
                val neighborOffset by animateFloatAsState(
                    targetValue = if (dragKey == null) 0f else expressiveQueuePreviewShift(absolute, dragIndex, previewTarget) * rowHeight,
                    // Once committed, actual positions already include the preview's displacement.
                    animationSpec = if (dragKey == null || reduce) snap() else tween(160),
                    label = "queueNeighborOffset",
                )
                Box(Modifier.zIndex(if (dragKey == key) 1f else 0f)
                    .graphicsLayer { translationY = if (dragKey == key) dragOffset else neighborOffset }) {
                    ExpressiveQueueRow(entry, false, { if (!locked) onJump(liveAbsolute) },
                        onLongClick = { if (!locked) rowMenu = true },
                        background = if (dragKey == key) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
                        number = offset + 1,
                        modifier = Modifier.semantics {
                            if (!locked) customActions = buildList {
                                add(CustomAccessibilityAction("Remove from queue") { val from=currentKeys.indexOf(key); if(from>currentIndex){remove(from);true}else false })
                                for((direction,label) in listOf(-1 to "Move up",1 to "Move down")) {
                                    if(expressiveQueueMoveTarget(liveAbsolute,direction,currentIndex,currentQueue.size)!=null)
                                        add(CustomAccessibilityAction(label) {val from=currentKeys.indexOf(key);val target=expressiveQueueMoveTarget(from,direction,currentIndex,currentQueue.size);if(target!=null){move(from,target);true}else false})
                                }
                            }
                        }, trailing = {
                            Box(Modifier.size(48.dp).pointerInput(locked) {
                                if (!locked) detectDragGesturesAfterLongPress(
                                    onDragStart = { dragKey = key; dragIndex = liveAbsolute; dragOffset = 0f; draggingChanged(true) },
                                    onDragEnd = {
                                        val from = currentKeys.indexOf(dragKey)
                                        val target = expressiveQueueDropTarget(from, (dragOffset / rowHeight).roundToInt(), currentIndex, currentQueue.size)
                                        // Commit after release so moving a keyed row cannot cancel its own pointer handler.
                                        dragKey = null; dragOffset = 0f; dragIndex = -1
                                        if (target != null) {
                                            // Otherwise LazyColumn follows the moved first-visible key and hides the row moved above it.
                                            listState.requestScrollToItem(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
                                            move(from, target)
                                        }
                                        draggingChanged(false)
                                    },
                                    onDragCancel = { dragKey = null; dragOffset = 0f; draggingChanged(false) },
                                    onDrag = { change, amount ->
                                        change.consume(); dragOffset += amount.y
                                    })
                            }, contentAlignment = Alignment.Center) {
                                Icon(ExpressiveSymbols.DragHandle, "Drag to reorder", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (locked) .15f else .3f))
                            }
                        })
                    DropdownMenu(expanded = rowMenu, onDismissRequest = { rowMenu = false }) {
                        DropdownMenuItem(text = { Text("Remove from queue") }, enabled = !locked,
                            onClick = { rowMenu = false; remove(liveAbsolute) })
                    }
                }
            }
        }
        HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)
        ListItem(headlineContent={Text("Autoplay")},supportingContent={Text("Keep listening when the queue ends")},
            trailingContent={Switch(checked=autoplay,onCheckedChange={onAutoplay()},enabled=!locked)},
            colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerLow),
            modifier=Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ExpressiveQueueRow(song: Song, highlighted: Boolean, onClick: () -> Unit, background: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    modifier: Modifier = Modifier, number: Int? = null, onLongClick: (() -> Unit)? = null, trailing: @Composable RowScope.() -> Unit) {
    Row(modifier.fillMaxWidth().height(72.dp).background(background).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically) {
        Row(Modifier.weight(1f).fillMaxHeight().combinedClickable(onClick=onClick,onLongClick=onLongClick),verticalAlignment=Alignment.CenterVertically) {
            number?.let { Text(it.toString(),Modifier.width(28.dp),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant) }
            AsyncImage(song.thumbnailUrl.artworkAt(ROW_ART_PX),null,contentScale=ContentScale.Crop,modifier=Modifier.size(48.dp).clip(MaterialTheme.shapes.medium))
            Column(Modifier.weight(1f).padding(horizontal=12.dp)) {
                Text(song.title,color=MaterialTheme.colorScheme.onSurface,style=MaterialTheme.typography.bodyLarge,fontWeight=if(highlighted)FontWeight.SemiBold else FontWeight.Normal,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(song.artist,color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
        }
        trailing()
    }
}

/** A reorder may only touch upcoming entries, never the currently playing track. */
fun expressiveQueueMoveTarget(from: Int, direction: Int, currentIndex: Int, size: Int): Int? {
    if (direction !in listOf(-1, 1) || from <= currentIndex || from !in 0 until size) return null
    return (from + direction).takeIf { it > currentIndex && it in 0 until size }
}

/** Commit one bounded move on release, including multi-row drags. */
fun expressiveQueueDropTarget(from: Int, rows: Int, currentIndex: Int, size: Int): Int? {
    if (from <= currentIndex || from !in 0 until size) return null
    return (from.toLong() + rows).coerceIn((currentIndex + 1L), size - 1L).toInt().takeIf { it != from }
}

/** Reserve the dragged row's destination while its neighbors slide out of the way. */
fun expressiveQueuePreviewShift(row: Int, from: Int, target: Int): Int = when {
    from < target && row > from && row <= target -> -1
    from > target && row >= target && row < from -> 1
    else -> 0
}
