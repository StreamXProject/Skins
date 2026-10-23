package com.music.streamx.skins.comic

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Song
import com.music.streamx.ui.skins.skinPlayerSettings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

/** No list-edge displacement or velocity escapes to the enclosing player sheet. */
private val ComicQueueScrollBoundary=object: NestedScrollConnection {
    override fun onPostScroll(consumed: Offset,available: Offset,source: NestedScrollSource)=Offset(0f,available.y)
    override suspend fun onPostFling(consumed: Velocity,available: Velocity)=Velocity(0f,available.y)
}
internal fun comicQueueTarget(from: Int,rows: Int,current: Int,size: Int): Int? {
    if(from<=current || from !in 0 until size)return null
    return (from.toLong()+rows).coerceIn(current+1L,size-1L).toInt().takeIf{it!=from}
}
private fun queueShift(index: Int,from: Int,to: Int)=when {
    from<to && index>from && index<=to -> -1f
    from>to && index>=to && index<from -> 1f
    else -> 0f
}

@Composable internal fun ComicQueue(queue: List<Song>,currentIndex: Int,playing: Boolean,onJump: (Int)->Unit,onRemove: (Int)->Unit,onMove: (Int,Int)->Unit,onDragActive: (Boolean)->Unit,onClear: ()->Unit,onPlay: ()->Unit,locked: Boolean,autoplay: Boolean,onAutoplay: ()->Unit) {
    val c=MaterialTheme.colorScheme
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val list=rememberLazyListState()
    val rowPixels=with(LocalDensity.current){80.dp.toPx()}
    val keys=remember(queue){val occurrences=mutableMapOf<String,Int>();queue.map {it.queueEntryId ?: "${it.videoId}#${occurrences.merge(it.videoId,1,Int::plus)}"}}
    val latestKeys by rememberUpdatedState(keys)
    val latestCurrent by rememberUpdatedState(currentIndex)
    val latestMove by rememberUpdatedState(onMove)
    val dragChanged by rememberUpdatedState(onDragActive)
    var dragKey by remember{mutableStateOf<String?>(null)}
    var from by remember{mutableIntStateOf(-1)}
    var offset by remember{mutableFloatStateOf(0f)}
    var clear by remember{mutableStateOf(false)}
    fun cancelDrag(){dragKey=null;from=-1;offset=0f;dragChanged(false)}
    DisposableEffect(Unit){onDispose{dragChanged(false)}}
    LaunchedEffect(currentIndex,locked){if(locked || from<=currentIndex)cancelDrag()}
    LaunchedEffect(keys){if(dragKey!=null && dragKey !in keys)cancelDrag()}
    val target=comicQueueTarget(from,(offset/rowPixels).roundToInt(),currentIndex,queue.size) ?: from
    Column(Modifier.fillMaxSize().nestedScroll(ComicQueueScrollBoundary)) {
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Text("UP NEXT!",style=MaterialTheme.typography.headlineLarge,modifier=Modifier.weight(1f));IconButton({clear=true},enabled=!locked){Icon(ComicIcons.ClearAll,"Clear queue")}}
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("One more issue?",Modifier.weight(1f),color=c.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium)
            Switch(autoplay,{onAutoplay()},enabled=!locked)
        }
        Text("Hold ≡ to rearrange",Modifier.padding(start=24.dp,end=24.dp,bottom=8.dp),style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant)
        LazyColumn(Modifier.weight(1f).fillMaxWidth(),state=list,overscrollEffect=null,contentPadding=PaddingValues(bottom=24.dp)) {
            val first=currentIndex.coerceAtLeast(0)
            itemsIndexed(queue.drop(first),key={offset,_->keys[first+offset]}) { relative,song->
                val index=first+relative
                val key=keys[index]
                val current=index==currentIndex
                val dragging=key==dragKey
                var menu by remember{mutableStateOf(false)}
                val shift by animateFloatAsState(if(dragKey==null)0f else queueShift(index,from,target)*rowPixels,
                    if(reduce || dragKey==null)snap()else tween(140),label="Queue placement")
                val moveUp=comicQueueTarget(index,-1,currentIndex,queue.size)
                val moveDown=comicQueueTarget(index,1,currentIndex,queue.size)
                Column(Modifier.fillMaxWidth().height(80.dp).zIndex(if(dragging)1f else 0f).graphicsLayer{translationY=if(dragging)offset else shift}
                    .background(if(dragging)ComicYellow else if(current)c.surfaceContainerHigh else c.background)) {
                    Row(Modifier.weight(1f).fillMaxWidth().padding(start=24.dp,end=12.dp),verticalAlignment=Alignment.CenterVertically) {
                        Row(Modifier.weight(1f).fillMaxHeight().clickable(enabled=!locked && dragKey==null){if(current)onPlay()else onJump(index)}
                            .semantics{if(!locked && !current)customActions=buildList {
                                moveUp?.let{to->add(CustomAccessibilityAction("Move up"){onMove(index,to);true})}
                                moveDown?.let{to->add(CustomAccessibilityAction("Move down"){onMove(index,to);true})}
                                add(CustomAccessibilityAction("Remove from queue"){onRemove(index);true})
                            }},verticalAlignment=Alignment.CenterVertically) {
                            if(current) Box(Modifier.width(28.dp),contentAlignment=Alignment.CenterStart) {
                                ComicSignal(playing,Modifier.size(20.dp).semantics{contentDescription=if(playing)"Now playing"else"Paused"})
                            } else Text(relative.toString().padStart(2,'0'),Modifier.width(28.dp),style=MaterialTheme.typography.labelSmall,color=if(dragging)androidx.compose.ui.graphics.Color.Black else c.primary)
                            AsyncImage(song.thumbnailUrl,null,Modifier.size(44.dp).border(1.5.dp,c.onSurface).background(c.surface),contentScale=ContentScale.Crop)
                            Column(Modifier.weight(1f).padding(horizontal=10.dp)) {
                                Text(song.title,style=MaterialTheme.typography.titleMedium,color=if(dragging)androidx.compose.ui.graphics.Color.Black else c.onSurface,maxLines=1,overflow=TextOverflow.Ellipsis)
                                Text(song.artist,color=if(dragging)androidx.compose.ui.graphics.Color.DarkGray else c.onSurfaceVariant,style=MaterialTheme.typography.bodySmall,maxLines=1,overflow=TextOverflow.Ellipsis)
                            }
                        }
                        Box {
                            IconButton({menu=true},enabled=!locked && dragKey==null,modifier=Modifier.size(40.dp)){Icon(ComicIcons.MoreHoriz,"Queue actions")}
                            DropdownMenu(menu,{menu=false}) {
                                if(!current) {
                                    DropdownMenuItem(text={Text("Move up")},enabled=moveUp!=null,onClick={menu=false;moveUp?.let{onMove(index,it)}})
                                    DropdownMenuItem(text={Text("Move down")},enabled=moveDown!=null,onClick={menu=false;moveDown?.let{onMove(index,it)}})
                                    DropdownMenuItem(text={Text("Play next")},onClick={menu=false;onMove(index,currentIndex+1)})
                                    DropdownMenuItem(text={Text("Move to end")},enabled=index<queue.lastIndex,onClick={menu=false;onMove(index,queue.lastIndex)})
                                    DropdownMenuItem(text={Text("Remove")},onClick={menu=false;onRemove(index)})
                                } else DropdownMenuItem(text={Text(if(playing)"Pause"else"Play")},onClick={menu=false;onPlay()})
                            }
                        }
                        if(!current) Box(Modifier.size(44.dp).pointerInput(key,locked){if(!locked)detectDragGesturesAfterLongPress(
                            onDragStart={from=latestKeys.indexOf(key);dragKey=key;offset=0f;dragChanged(true)},
                            onDrag={change,delta->change.consume();offset+=delta.y},
                            onDragCancel={cancelDrag()},
                            onDragEnd={
                                val source=latestKeys.indexOf(key)
                                val to=comicQueueTarget(source,(offset/rowPixels).roundToInt(),latestCurrent,latestKeys.size)
                                if(to!=null){list.requestScrollToItem(list.firstVisibleItemIndex,list.firstVisibleItemScrollOffset);latestMove(source,to)}
                                cancelDrag()
                            })}.semantics{contentDescription="Drag to reorder ${song.title}"},contentAlignment=Alignment.Center) {
                            Text("≡",style=MaterialTheme.typography.titleLarge,color=if(dragging)androidx.compose.ui.graphics.Color.Black else c.onSurfaceVariant)
                        } else Spacer(Modifier.width(44.dp))
                    }
                    HorizontalDivider(Modifier.padding(horizontal=24.dp),color=c.outlineVariant.copy(alpha=.5f))
                }
            }
        }
    }
    if(clear)AlertDialog(onDismissRequest={clear=false},title={Text("Clear the queue?")},text={Text("Remove the upcoming tracks from this listening session.")},confirmButton={TextButton({clear=false;onClear()}){Text("Clear")}},dismissButton={TextButton({clear=false}){Text("Keep listening")}})
}
