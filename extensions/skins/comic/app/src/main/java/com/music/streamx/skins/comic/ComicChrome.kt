package com.music.streamx.skins.comic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.*

@Composable internal fun ComicHomeHeader(account: Account?,onAccount: ()->Unit,onLocal: ()->Unit,onHistory: ()->Unit,onExplore: ()->Unit) {
    val c=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal=24.dp).padding(top=8.dp,bottom=28.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            Row(Modifier.weight(1f),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(c.primary,CircleShape));Spacer(Modifier.width(9.dp));Eyebrow("comic / your universe")}
            IconButton(onAccount){Icon(ComicIcons.PersonOutline,"Account")}
        }
        Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.padding(top=12.dp,bottom=20.dp)) {
            Text("TURN IT\nUP!",Modifier.weight(1f),style=MaterialTheme.typography.displayLarge)
            ComicDial(Modifier.size(88.dp))
        }
        HorizontalDivider(color=c.outlineVariant)
        Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.SpaceBetween){
            TextButton(onHistory){Icon(ComicIcons.History,null,Modifier.size(16.dp));Spacer(Modifier.width(7.dp));Text("History")}
            TextButton(onLocal){Icon(ComicIcons.FolderOpen,null,Modifier.size(16.dp));Spacer(Modifier.width(7.dp));Text("On device")}
            IconButton(onExplore){Icon(ComicIcons.ArrowForward,"Explore")}
        }
    }
}
@Composable internal fun ComicBrowseHeader(onSearch: ()->Unit,onSettings: ()->Unit,onExplore: ()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
        Text("COMIC!",Modifier.weight(1f),style=MaterialTheme.typography.headlineMedium);IconButton(onSearch){Icon(ComicIcons.Search,"Search")};IconButton(onSettings){Icon(ComicIcons.Settings,"Settings")};IconButton(onExplore){Icon(ComicIcons.Explore,"Explore")}
    }
}
@Composable internal fun ComicBottomNavigation(tabs: List<BottomTab>,selectedIndex: Int,onTabSelected: (Int)->Unit) {
    val c=MaterialTheme.colorScheme
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    Surface(color=c.background,tonalElevation=0.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=16.dp)) {
            HorizontalDivider(color=c.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index,tab ->
                    val selected=index==selectedIndex
                    val fill by animateColorAsState(if(selected)ComicYellow else Color.Transparent,tween(if(reduce)0 else 260),label="Dock selection")
                    val ink by animateColorAsState(if(selected)Color(0xFF191919) else c.onSurface,tween(if(reduce)0 else 260),label="Dock ink")
                    Surface(onClick={onTabSelected(index)},modifier=Modifier.weight(1f).height(60.dp).then(if(selected)Modifier.inkShadow(c.onSurface,3.dp)else Modifier),shape=Edge,color=fill,contentColor=ink,border=if(selected)BorderStroke(2.dp,c.onSurface)else null) {
                        Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                            Icon(when(index){0->ComicIcons.Home;1->ComicIcons.Explore;2->ComicIcons.Library;else->ComicIcons.Search},tab.label,Modifier.size(23.dp));Spacer(Modifier.height(4.dp));Text(tab.label,style=MaterialTheme.typography.labelSmall,maxLines=1)
                        }
                    }
                }
            }
        }
    }
}
@Composable internal fun ComicMiniPlayer(song: Song,playing: Boolean,loading: Boolean,position: PlaybackPosition,durationMs: Long,saved: Boolean,onSave: ()->Unit,onPlayPause: ()->Unit,onExpand: ()->Unit,onNext: ()->Unit,onPrevious: ()->Unit,locked: Boolean,onBlocked: ()->Unit,modifier: Modifier) {
    val c=MaterialTheme.colorScheme
    val art=rememberSkinRemoteArtwork(song)
    Surface(modifier.padding(horizontal=16.dp,vertical=8.dp).fillMaxWidth().inkShadow(c.onSurface,4.dp),color=c.surface,contentColor=c.onSurface,shape=Edge,border=BorderStroke(2.dp,c.onSurface)) {
        Column {
            Row(Modifier.height(68.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(1f).fillMaxHeight().clickable(onClick=onExpand).padding(start=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    AsyncImage(art,null,Modifier.size(50.dp).border(1.5.dp,c.onSurface).background(c.surface),contentScale=ContentScale.Crop)
                    Column(Modifier.padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,style=MaterialTheme.typography.bodyMedium,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
                }
                IconButton({if(locked)onBlocked() else onPlayPause()}){if(loading)CircularProgressIndicator(Modifier.size(19.dp),strokeWidth=2.dp)else Icon(if(playing)ComicIcons.Pause else ComicIcons.PlayArrow,if(playing)"Pause"else"Play",tint=c.primary)}
                IconButton({if(locked)onBlocked()else onNext()}){Icon(ComicIcons.SkipNext,"Next")}
            }
            LinearProgressIndicator(progress={if(durationMs>0)(position.positionMs.toFloat()/durationMs).coerceIn(0f,1f)else 0f},modifier=Modifier.fillMaxWidth().height(2.dp),color=ComicRed,trackColor=c.surfaceContainerHigh,drawStopIndicator={})
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.comicFeedShelves(shelves: List<HomeShelf>,layout: SkinLayout,onClick: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,shelfBrowseId: (HomeShelf)->String?) {
    shelves.forEachIndexed { section,shelf ->
        item(key="comic-heading-$section") {
            Row(Modifier.fillMaxWidth().padding(start=24.dp,end=16.dp,top=12.dp,bottom=16.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)){Eyebrow("PANEL ${(section+1).toString().padStart(2,'0')}");Text(shelf.title.uppercase(),style=MaterialTheme.typography.headlineMedium,modifier=Modifier.padding(top=5.dp))}
                shelfBrowseId(shelf)?.let { id ->IconButton({onClick(ShelfItem(shelf.title,"",null,null,id),shelf.title)}){Icon(ComicIcons.ArrowForward,"More ${shelf.title}")}}
            }
        }
        item(key="comic-shelf-$section") {
            LazyRow(contentPadding=PaddingValues(horizontal=24.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),modifier=Modifier.padding(bottom=24.dp)) {
                itemsIndexed(shelf.items) { index,item ->
                    val c=MaterialTheme.colorScheme
                    Column(Modifier.width(if(section==0)236.dp else 160.dp).combinedClickable(onClick={onClick(item,shelf.title)},onLongClick={onLongPress?.invoke(item)})) {
                        Box(Modifier.fillMaxWidth().aspectRatio(1f).inkShadow(c.onSurface,5.dp).border(2.dp,c.onSurface).padding(2.dp).background(c.surface)){
                            AsyncImage(item.thumbnailUrl,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                            Text((index+1).toString().padStart(2,'0'),Modifier.align(Alignment.BottomStart).padding(8.dp).background(ComicYellow).border(1.dp,Color(0xFF191919)).padding(horizontal=7.dp,vertical=4.dp),style=MaterialTheme.typography.labelSmall,color=Color(0xFF191919))
                        }
                        Text(item.title,style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=10.dp))
                        Text(item.subtitle,style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
@Composable internal fun ComicReleaseHeader(page: DetailPage,songs: List<Song>,onPlay: ()->Unit,onShuffle: ()->Unit,onSearch: ()->Unit,onMore: ((List<Song>)->Unit)?,onArtist: (String,String)->Unit,onToggleLibrary: (()->Unit)?,topPadding: Dp) {
    val c=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top=topPadding).padding(horizontal=24.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        AsyncImage(page.thumbnailUrl,null,Modifier.widthIn(max=300.dp).fillMaxWidth().aspectRatio(1f).clip(Edge).background(c.surface),contentScale=ContentScale.Crop)
        Eyebrow("${songs.size} tracks",Modifier.align(Alignment.Start).padding(top=24.dp))
        Text(page.title,style=MaterialTheme.typography.displayLarge,modifier=Modifier.fillMaxWidth().padding(top=6.dp),maxLines=3,overflow=TextOverflow.Ellipsis)
        Text(page.subtitle,Modifier.fillMaxWidth().padding(top=8.dp),color=c.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().padding(vertical=24.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            ComicButton("Play",onPlay,Modifier.weight(1f),true);IconButton(onShuffle){Icon(ComicIcons.Shuffle,"Shuffle")};IconButton(onSearch){Icon(ComicIcons.Search,"Search tracks")};onToggleLibrary?.let{IconButton(it){Icon(ComicIcons.LibraryAdd,"Save to library")}};onMore?.let{IconButton({it(songs)}){Icon(ComicIcons.MoreHoriz,"More")}}
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ComicLibraryRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit) {
    val c=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick=onClick,onLongClick=onLongPress).padding(horizontal=24.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
        AsyncImage(item.thumbnailUrl,null,Modifier.size(58.dp).border(1.5.dp,c.onSurface).background(c.surfaceContainerHigh),contentScale=ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal=14.dp)){Text(item.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(item.subtitle,color=c.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium,maxLines=1,overflow=TextOverflow.Ellipsis)}
        Icon(ComicIcons.ArrowForward,null,Modifier.size(17.dp),tint=c.onSurfaceVariant)
    }
}
@Composable internal fun ComicLibrary(shelves: List<HomeShelf>,links: List<ShelfItem>,deviceItems: List<ShelfItem>,contentPadding: PaddingValues,onItem: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,onShowAll: (HomeShelf)->Unit,onNewPlaylist: (()->Unit)?) {
    LazyColumn(contentPadding=contentPadding,modifier=Modifier.fillMaxSize()) {
        item {Column(Modifier.padding(24.dp)){Eyebrow("YOUR STASH");Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("THE COLLECTION!",style=MaterialTheme.typography.displayLarge,modifier=Modifier.weight(1f));onNewPlaylist?.let{IconButton(it){Icon(ComicIcons.Add,"New playlist")}}};HorizontalDivider(Modifier.padding(top=24.dp),color=MaterialTheme.colorScheme.outlineVariant)}}
        items(links){item->ComicLibraryRow(item,{onItem(item,"Library")},{onLongPress?.invoke(item)})}
        shelves.forEach { shelf->
            item {Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Text(shelf.title,style=MaterialTheme.typography.headlineSmall,modifier=Modifier.weight(1f));TextButton({onShowAll(shelf)}){Text("View all")}}}
            items(shelf.items.take(5)){item->ComicLibraryRow(item,{onItem(item,shelf.title)},{onLongPress?.invoke(item)})}
        }
        if(deviceItems.isNotEmpty()) {item{Eyebrow("On this device",Modifier.padding(24.dp))};items(deviceItems){item->ComicLibraryRow(item,{onItem(item,"On device")},{onLongPress?.invoke(item)})}}
    }
}
@Composable internal fun ComicBottomFade(modifier: Modifier,withMiniPlayer: Boolean,pageColor: Color) {
    Box(modifier.fillMaxWidth().height(if(withMiniPlayer)88.dp else 32.dp).background(Brush.verticalGradient(listOf(Color.Transparent,pageColor))))
}
@Composable internal fun ComicLibraryStats(onOpen: ()->Unit) { TextButton(onOpen,modifier=Modifier.padding(horizontal=16.dp)){Text("Listening history");Spacer(Modifier.width(8.dp));Icon(ComicIcons.History,null,Modifier.size(18.dp))} }
