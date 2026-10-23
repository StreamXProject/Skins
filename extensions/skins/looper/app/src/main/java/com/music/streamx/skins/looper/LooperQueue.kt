package com.music.streamx.skins.looper

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Song
import kotlin.math.roundToInt

internal fun queueTarget(from: Int,rows: Int,current: Int,size: Int): Int? {
    if(from<=current || from !in 0 until size)return null
    return (from.toLong()+rows).coerceIn(current+1L,size-1L).toInt().takeIf{it!=from}
}
internal fun queueShift(row: Int,from: Int,to: Int)=when{from<to && row>from && row<=to->-1;from>to && row>=to && row<from->1;else->0}
@OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class)
@Composable internal fun LooperQueue(song: Song,queue: List<Song>,index: Int,playing: Boolean,loading: Boolean,locked: Boolean,reduce: Boolean,onJump: (Int)->Unit,onRemove: (Int)->Unit,onMove: (Int,Int)->Unit,onDragActive: (Boolean)->Unit,onClear: ()->Unit,onPlay: ()->Unit,onDismiss: ()->Unit) {
    val sheet=rememberModalBottomSheetState(skipPartiallyExpanded=true)
    val list=rememberLazyListState()
    val density=LocalDensity.current
    val rowPixels=with(density){72.dp.toPx()}
    val keys=remember(queue){val counts=mutableMapOf<String,Int>();queue.map{"${it.videoId}#${counts.merge(it.videoId,1,Int::plus)}"}}
    val latestKeys by rememberUpdatedState(keys);val latestIndex by rememberUpdatedState(index);val latestSize by rememberUpdatedState(queue.size)
    val move by rememberUpdatedState(onMove);val dragActive by rememberUpdatedState(onDragActive)
    var from by remember{mutableIntStateOf(-1)};var dragKey by remember{mutableStateOf<String?>(null)};var offset by remember{mutableFloatStateOf(0f)}
    var menu by remember{mutableStateOf(false)}
    DisposableEffect(Unit){onDispose{dragActive(false)}}
    LaunchedEffect(index,locked){if(locked || from<=index){from=-1;dragKey=null;offset=0f;dragActive(false)}}
    val target=queueTarget(from,(offset/rowPixels).roundToInt(),index,queue.size) ?: from
    ModalBottomSheet(onDismissRequest=onDismiss,sheetState=sheet,containerColor=Ink,shape=RoundedCornerShape(topStart=24.dp,topEnd=24.dp),dragHandle={Box(Modifier.padding(top=10.dp,bottom=6.dp).size(32.dp,4.dp).background(Color.White.copy(alpha=.2f),Capsule))}) {
        Box(Modifier.fillMaxWidth().fillMaxHeight(.75f)){
            Column(Modifier.fillMaxSize()){
                Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Text("Play queue",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("${queue.size} songs",style=MaterialTheme.typography.bodyMedium,color=Dim);Box{IconButton(onClick={menu=true}){Icon(LooperIcons.More,"Queue options",Modifier.size(18.dp))};DropdownMenu(menu,{menu=false}){DropdownMenuItem(text={Text("Clear queue")},onClick={menu=false;onClear()},enabled=!locked)}}}
                LazyColumn(Modifier.weight(1f).fillMaxWidth(),state=list,contentPadding=PaddingValues(bottom=16.dp)){
                    val first=index.coerceAtLeast(0)
                    itemsIndexed(queue.drop(first),key={i,_->keys[i+first]}){i,entry->
                        val absolute=i+first;val key=keys[absolute];val current=absolute==index
                        var rowMenu by remember{mutableStateOf(false)}
                        val shift by animateFloatAsState(if(dragKey==null)0f else queueShift(absolute,from,target)*rowPixels,if(reduce || dragKey==null)snap()else tween(180),label="Queue placement")
                        Row(Modifier.fillMaxWidth().height(72.dp).zIndex(if(dragKey==key)1f else 0f).graphicsLayer{translationY=if(dragKey==key)offset else shift}.background(if(dragKey==key)Color(0xFF202020)else Ink).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                            Row(Modifier.weight(1f).fillMaxHeight().combinedClickable(onClick={if(!locked && !current)onJump(absolute)},onLongClick={rowMenu=true}).semantics{if(!locked && !current)customActions=buildList{
                                add(CustomAccessibilityAction("Remove from queue"){onRemove(absolute);true})
                                queueTarget(absolute,-1,index,queue.size)?.let{to->add(CustomAccessibilityAction("Move up"){onMove(absolute,to);true})}
                                queueTarget(absolute,1,index,queue.size)?.let{to->add(CustomAccessibilityAction("Move down"){onMove(absolute,to);true})}
                            }},verticalAlignment=Alignment.CenterVertically){
                                AsyncImage(entry.thumbnailUrl,null,Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),contentScale=ContentScale.Crop)
                                Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(entry.title,style=MaterialTheme.typography.titleMedium,color=if(current)MaterialTheme.colorScheme.primary else Color.White,maxLines=1,overflow=TextOverflow.Ellipsis);Text(entry.artist,style=MaterialTheme.typography.bodyMedium,color=Dim,maxLines=1,overflow=TextOverflow.Ellipsis)}
                            }
                            if(current)IconButton(onClick=onPlay,enabled=!locked){if(loading)CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp)else Icon(if(playing)LooperIcons.Pause else LooperIcons.Play,if(playing)"Pause"else"Play",Modifier.size(20.dp))}
                            else Box(Modifier.size(48.dp).pointerInput(key,locked){if(!locked)detectDragGesturesAfterLongPress(
                                onDragStart={from=latestKeys.indexOf(key);dragKey=key;offset=0f;dragActive(true)},
                                onDrag={change,delta->change.consume();offset+=delta.y},
                                onDragCancel={from=-1;dragKey=null;offset=0f;dragActive(false)},
                                onDragEnd={val source=latestKeys.indexOf(dragKey);val destination=queueTarget(source,(offset/rowPixels).roundToInt(),latestIndex,latestSize);from=-1;dragKey=null;offset=0f;if(destination!=null){list.requestScrollToItem(list.firstVisibleItemIndex,list.firstVisibleItemScrollOffset);move(source,destination)};dragActive(false)}
                            )}.semantics{contentDescription="Drag to reorder"},contentAlignment=Alignment.Center){Text("≡",color=Dim)}
                            DropdownMenu(rowMenu,{rowMenu=false}){DropdownMenuItem(text={Text("Remove from queue")},onClick={rowMenu=false;onRemove(absolute)},enabled=!locked && !current)}
                        }
                    }
                }
            }
        }
    }
}
