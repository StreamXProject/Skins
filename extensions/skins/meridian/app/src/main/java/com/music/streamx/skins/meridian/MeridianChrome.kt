package com.music.streamx.skins.meridian

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

@Composable internal fun MeridianHomeHeader(account: Account?,onAccount: ()->Unit,onLocal: ()->Unit,onHistory: ()->Unit,onExplore: ()->Unit) {
    val c=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal=24.dp).padding(top=8.dp,bottom=28.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            Row(Modifier.weight(1f),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(7.dp).background(c.primary,CircleShape));Spacer(Modifier.width(9.dp));Eyebrow("Meridian / Your collection")}
            IconButton(onAccount){Icon(MeridianIcons.PersonOutline,"Account")}
        }
        Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.padding(top=12.dp,bottom=20.dp)) {
            Text("Find your\nfrequency.",Modifier.weight(1f),style=MaterialTheme.typography.displayLarge)
            MeridianDial(Modifier.size(88.dp))
        }
        HorizontalDivider(color=c.outlineVariant)
        Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.SpaceBetween){
            TextButton(onHistory){Icon(MeridianIcons.History,null,Modifier.size(16.dp));Spacer(Modifier.width(7.dp));Text("History")}
            TextButton(onLocal){Icon(MeridianIcons.FolderOpen,null,Modifier.size(16.dp));Spacer(Modifier.width(7.dp));Text("On device")}
            IconButton(onExplore){Icon(MeridianIcons.ArrowForward,"Explore")}
        }
    }
}
@Composable internal fun MeridianBrowseHeader(onSearch: ()->Unit,onSettings: ()->Unit,onExplore: ()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
        Eyebrow("Meridian",Modifier.weight(1f));IconButton(onSearch){Icon(MeridianIcons.Search,"Search")};IconButton(onSettings){Icon(MeridianIcons.Settings,"Settings")};IconButton(onExplore){Icon(MeridianIcons.Explore,"Explore")}
    }
}
@Composable internal fun MeridianBottomNavigation(tabs: List<BottomTab>,selectedIndex: Int,onTabSelected: (Int)->Unit) {
    val c=MaterialTheme.colorScheme
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    Surface(color=c.background,tonalElevation=0.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=16.dp)) {
            HorizontalDivider(color=c.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index,tab ->
                    val selected=index==selectedIndex
                    val fill by animateColorAsState(if(selected)c.primary else Color.Transparent,tween(if(reduce)0 else 260),label="Dock selection")
                    val ink by animateColorAsState(if(selected)c.onPrimary else c.onSurfaceVariant,tween(if(reduce)0 else 260),label="Dock ink")
                    Surface(onClick={onTabSelected(index)},modifier=Modifier.weight(1f).height(60.dp),shape=Edge,color=fill,contentColor=ink) {
                        Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                            Icon(when(index){0->MeridianIcons.Home;1->MeridianIcons.Explore;2->MeridianIcons.Library;else->MeridianIcons.Search},tab.label,Modifier.size(23.dp));Spacer(Modifier.height(4.dp));Text(tab.label,style=MaterialTheme.typography.labelSmall,maxLines=1)
                        }
                    }
                }
            }
        }
    }
}
@Composable internal fun MeridianMiniPlayer(song: Song,playing: Boolean,loading: Boolean,position: PlaybackPosition,durationMs: Long,saved: Boolean,onSave: ()->Unit,onPlayPause: ()->Unit,onExpand: ()->Unit,onNext: ()->Unit,onPrevious: ()->Unit,locked: Boolean,onBlocked: ()->Unit,modifier: Modifier) {
    val c=MaterialTheme.colorScheme
    val art=rememberSkinRemoteArtwork(song)
    Surface(modifier.padding(horizontal=16.dp,vertical=8.dp).fillMaxWidth(),color=c.surfaceContainerHigh,contentColor=c.onSurface,shape=Edge,border=BorderStroke(1.dp,c.outlineVariant)) {
        Column {
            Row(Modifier.height(68.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(1f).fillMaxHeight().clickable(onClick=onExpand).padding(start=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    AsyncImage(art,null,Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(c.surface),contentScale=ContentScale.Crop)
                    Column(Modifier.padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,style=MaterialTheme.typography.bodyMedium,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
                }
                IconButton({if(locked)onBlocked() else onPlayPause()}){if(loading)CircularProgressIndicator(Modifier.size(19.dp),strokeWidth=2.dp)else Icon(if(playing)MeridianIcons.Pause else MeridianIcons.PlayArrow,if(playing)"Pause"else"Play",tint=c.primary)}
                IconButton({if(locked)onBlocked()else onNext()}){Icon(MeridianIcons.SkipNext,"Next")}
            }
            LinearProgressIndicator(progress={if(durationMs>0)(position.positionMs.toFloat()/durationMs).coerceIn(0f,1f)else 0f},modifier=Modifier.fillMaxWidth().height(2.dp),color=c.primary,trackColor=c.outlineVariant,drawStopIndicator={})
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.meridianFeedShelves(shelves: List<HomeShelf>,layout: SkinLayout,onClick: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,shelfBrowseId: (HomeShelf)->String?) {
    shelves.forEachIndexed { section,shelf ->
        item(key="meridian-heading-$section") {
            Row(Modifier.fillMaxWidth().padding(start=24.dp,end=16.dp,top=12.dp,bottom=16.dp),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)){Eyebrow("${(section+1).toString().padStart(2,'0')} / Selection");Text(shelf.title,style=MaterialTheme.typography.headlineMedium,modifier=Modifier.padding(top=5.dp))}
                shelfBrowseId(shelf)?.let { id ->IconButton({onClick(ShelfItem(shelf.title,"",null,null,id),shelf.title)}){Icon(MeridianIcons.ArrowForward,"More ${shelf.title}")}}
            }
        }
        item(key="meridian-shelf-$section") {
            LazyRow(contentPadding=PaddingValues(horizontal=24.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),modifier=Modifier.padding(bottom=24.dp)) {
                itemsIndexed(shelf.items) { index,item ->
                    val c=MaterialTheme.colorScheme
                    Column(Modifier.width(if(section==0)236.dp else 160.dp).combinedClickable(onClick={onClick(item,shelf.title)},onLongClick={onLongPress?.invoke(item)})) {
                        Box(Modifier.fillMaxWidth().aspectRatio(if(section==0)1.12f else 1f).clip(Edge).background(c.surfaceContainerHigh)){
                            AsyncImage(item.thumbnailUrl,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                            Text((index+1).toString().padStart(2,'0'),Modifier.align(Alignment.BottomStart).padding(8.dp).background(c.background.copy(alpha=.85f),RoundedCornerShape(5.dp)).padding(horizontal=7.dp,vertical=4.dp),style=MaterialTheme.typography.labelSmall,color=c.onBackground)
                        }
                        Text(item.title,style=MaterialTheme.typography.titleMedium,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=10.dp))
                        Text(item.subtitle,style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
@Composable internal fun MeridianReleaseHeader(page: DetailPage,songs: List<Song>,onPlay: ()->Unit,onShuffle: ()->Unit,onSearch: ()->Unit,onMore: ((List<Song>)->Unit)?,onArtist: (String,String)->Unit,onToggleLibrary: (()->Unit)?,topPadding: Dp) {
    val c=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top=topPadding).padding(horizontal=24.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        AsyncImage(page.thumbnailUrl,null,Modifier.widthIn(max=300.dp).fillMaxWidth().aspectRatio(1f).clip(Edge).background(c.surface),contentScale=ContentScale.Crop)
        Eyebrow("${songs.size} tracks",Modifier.align(Alignment.Start).padding(top=24.dp))
        Text(page.title,style=MaterialTheme.typography.displayLarge,modifier=Modifier.fillMaxWidth().padding(top=6.dp),maxLines=3,overflow=TextOverflow.Ellipsis)
        Text(page.subtitle,Modifier.fillMaxWidth().padding(top=8.dp),color=c.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().padding(vertical=24.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            MeridianButton("Play",onPlay,Modifier.weight(1f),true);IconButton(onShuffle){Icon(MeridianIcons.Shuffle,"Shuffle")};IconButton(onSearch){Icon(MeridianIcons.Search,"Search tracks")};onToggleLibrary?.let{IconButton(it){Icon(MeridianIcons.LibraryAdd,"Save to library")}};onMore?.let{IconButton({it(songs)}){Icon(MeridianIcons.MoreHoriz,"More")}}
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun MeridianLibraryRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit) {
    val c=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().combinedClickable(onClick=onClick,onLongClick=onLongPress).padding(horizontal=24.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
        AsyncImage(item.thumbnailUrl,null,Modifier.size(58.dp).clip(RoundedCornerShape(8.dp)).background(c.surfaceContainerHigh),contentScale=ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal=14.dp)){Text(item.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(item.subtitle,color=c.onSurfaceVariant,style=MaterialTheme.typography.bodyMedium,maxLines=1,overflow=TextOverflow.Ellipsis)}
        Icon(MeridianIcons.ArrowForward,null,Modifier.size(17.dp),tint=c.onSurfaceVariant)
    }
}
@Composable internal fun MeridianLibrary(shelves: List<HomeShelf>,links: List<ShelfItem>,deviceItems: List<ShelfItem>,contentPadding: PaddingValues,onItem: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,onShowAll: (HomeShelf)->Unit,onNewPlaylist: (()->Unit)?) {
    LazyColumn(contentPadding=contentPadding,modifier=Modifier.fillMaxSize()) {
        item {Column(Modifier.padding(24.dp)){Eyebrow("Your archive");Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Collected.",style=MaterialTheme.typography.displayLarge,modifier=Modifier.weight(1f));onNewPlaylist?.let{IconButton(it){Icon(MeridianIcons.Add,"New playlist")}}};HorizontalDivider(Modifier.padding(top=24.dp),color=MaterialTheme.colorScheme.outlineVariant)}}
        items(links){item->MeridianLibraryRow(item,{onItem(item,"Library")},{onLongPress?.invoke(item)})}
        shelves.forEach { shelf->
            item {Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Text(shelf.title,style=MaterialTheme.typography.headlineSmall,modifier=Modifier.weight(1f));TextButton({onShowAll(shelf)}){Text("View all")}}}
            items(shelf.items.take(5)){item->MeridianLibraryRow(item,{onItem(item,shelf.title)},{onLongPress?.invoke(item)})}
        }
        if(deviceItems.isNotEmpty()) {item{Eyebrow("On this device",Modifier.padding(24.dp))};items(deviceItems){item->MeridianLibraryRow(item,{onItem(item,"On device")},{onLongPress?.invoke(item)})}}
    }
}
@Composable internal fun MeridianBottomFade(modifier: Modifier,withMiniPlayer: Boolean,pageColor: Color) {
    Box(modifier.fillMaxWidth().height(if(withMiniPlayer)88.dp else 32.dp).background(Brush.verticalGradient(listOf(Color.Transparent,pageColor))))
}
@Composable internal fun MeridianLibraryStats(onOpen: ()->Unit) { TextButton(onOpen,modifier=Modifier.padding(horizontal=16.dp)){Text("Listening history");Spacer(Modifier.width(8.dp));Icon(MeridianIcons.History,null,Modifier.size(18.dp))} }
