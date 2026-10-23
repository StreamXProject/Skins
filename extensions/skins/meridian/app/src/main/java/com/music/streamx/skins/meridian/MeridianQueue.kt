package com.music.streamx.skins.meridian

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Song

@Composable internal fun MeridianQueue(queue: List<Song>,currentIndex: Int,playing: Boolean,onJump: (Int)->Unit,onRemove: (Int)->Unit,onMove: (Int,Int)->Unit,onClear: ()->Unit,onPlay: ()->Unit,locked: Boolean,autoplay: Boolean,onAutoplay: ()->Unit) {
    val c=MaterialTheme.colorScheme
    var clear by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Text("In sequence.",style=MaterialTheme.typography.headlineLarge,modifier=Modifier.weight(1f));IconButton({clear=true},enabled=!locked){Icon(MeridianIcons.ClearAll,"Clear queue")}}
        Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
            Text("Keep the music going",Modifier.weight(1f),color=c.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium)
            Switch(autoplay,{onAutoplay()},enabled=!locked)
        }
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(bottom=24.dp)) {
            itemsIndexed(queue.drop(currentIndex.coerceAtLeast(0))) { offset,song->
                val index=currentIndex.coerceAtLeast(0)+offset
                var menu by remember(song.videoId,index){mutableStateOf(false)}
                Row(Modifier.fillMaxWidth().background(if(offset==0)c.surfaceContainerHigh else c.background).clickable(enabled=!locked){if(offset==0)onPlay()else onJump(index)}.padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(if(offset==0)"•"else offset.toString().padStart(2,'0'),Modifier.width(32.dp),style=MaterialTheme.typography.labelSmall,color=c.primary)
                    AsyncImage(song.thumbnailUrl,null,Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(c.surface),contentScale=ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,color=c.onSurfaceVariant,style=MaterialTheme.typography.bodySmall,maxLines=1,overflow=TextOverflow.Ellipsis)}
                    Box {IconButton({menu=true},enabled=!locked){Icon(if(offset==0 && playing)MeridianIcons.GraphicEq else MeridianIcons.MoreHoriz,"Queue actions")};DropdownMenu(menu,{menu=false}) {
                        if(offset>0){DropdownMenuItem(text={Text("Play next")},onClick={menu=false;onMove(index,currentIndex+1)});DropdownMenuItem(text={Text("Remove")},onClick={menu=false;onRemove(index)})}
                        else DropdownMenuItem(text={Text(if(playing)"Pause"else"Play")},onClick={menu=false;onPlay()})
                    }}
                }
                HorizontalDivider(Modifier.padding(horizontal=24.dp),color=c.outlineVariant.copy(alpha=.5f))
            }
        }
    }
    if(clear)AlertDialog(onDismissRequest={clear=false},title={Text("Clear the queue?")},text={Text("Remove the upcoming tracks from this listening session.")},confirmButton={TextButton({clear=false;onClear()}){Text("Clear")}},dismissButton={TextButton({clear=false}){Text("Keep listening")}})
}
