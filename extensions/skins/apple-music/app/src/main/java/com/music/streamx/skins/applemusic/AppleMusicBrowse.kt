package com.music.streamx.skins.applemusic

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.*

@Composable internal fun AppleMusicMiniPlayer(song: Song,playing: Boolean,loading: Boolean,position: PlaybackPosition,durationMs: Long,
    saved: Boolean,onSave: ()->Unit,onPlayPause: ()->Unit,onExpand: ()->Unit,onNext: ()->Unit,onPrevious: ()->Unit,
    locked: Boolean,onBlocked: ()->Unit,modifier: Modifier) {
    val art=rememberSkinRemoteArtwork(song)
    val c=MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=2.dp).height(64.dp).clip(CircleShape).background(c.surfaceVariant.copy(alpha=.96f))
        .border(.5.dp,c.onSurface.copy(alpha=.1f),CircleShape).clickable(onClick=onExpand).padding(start=10.dp,end=8.dp),verticalAlignment=Alignment.CenterVertically) {
        AsyncImage(art,null,Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)),contentScale=ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal=12.dp)) {
            Text(song.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
            Text(song.artist,style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
        IconButton(onClick={if(locked)onBlocked()else onPlayPause()}) {
            if(loading)CircularProgressIndicator(Modifier.size(22.dp),strokeWidth=2.dp,color=c.onSurface)
            else Icon(if(playing)AppleIcons.Pause else AppleIcons.Play,if(playing)"Pause"else"Play",Modifier.size(29.dp))
        }
        IconButton(onClick={if(locked)onBlocked()else onNext()}){Icon(AppleIcons.Next,"Next",Modifier.size(28.dp))}
    }
}
@Composable internal fun AppleMusicHomeHeader(account: Account?,onAccount: ()->Unit,onLocal: ()->Unit,onHistory: ()->Unit,onExplore: ()->Unit) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {IconButton(onClick=onAccount){Icon(Icons.Rounded.AccountCircle,"Account")}}
}
@Composable internal fun AppleMusicReleaseHeader(page: DetailPage,songs: List<Song>,onPlay: ()->Unit,onShuffle: ()->Unit,onSearch: ()->Unit,
    onMore: ((List<Song>)->Unit)?,onArtist: (String,String)->Unit,onToggleLibrary: (()->Unit)?,topPadding: Dp) {
    Column(Modifier.fillMaxWidth().padding(top=topPadding),horizontalAlignment=Alignment.CenterHorizontally) {
        AsyncImage(page.thumbnailUrl,null,Modifier.padding(horizontal=40.dp).widthIn(max=340.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)),contentScale=ContentScale.Crop)
        Text(page.title,Modifier.padding(16.dp),style=MaterialTheme.typography.headlineSmall)
        Text(page.subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.padding(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            TextButton(onClick=onPlay){Icon(AppleIcons.Play,null);Text("Play")}
            IconButton(onClick=onShuffle){Icon(Icons.Rounded.Shuffle,"Shuffle")}
            IconButton(onClick=onSearch){Icon(Icons.Rounded.Search,"Search tracks")}
            onMore?.let{IconButton(onClick={it(songs)}){Icon(Icons.Rounded.MoreHoriz,"More")}}
        }
    }
}
@Composable internal fun AppleMusicSleepTimerDialog(minutes: Int?,afterTrack: Boolean,running: Boolean,presets: List<Int>,onStart: (Int)->Unit,
    onAfterTrack: ()->Unit,onCancel: ()->Unit,onDismiss: ()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,title={Text("Sleep timer")},text={Column{presets.forEach{n->TextButton(onClick={onStart(n);onDismiss()}){Text("$n minutes")}};TextButton(onClick={onAfterTrack();onDismiss()}){Text("End of track")}}},confirmButton={TextButton(onClick=onDismiss){Text("Done")}},dismissButton={if(running || afterTrack)TextButton(onClick={onCancel();onDismiss()}){Text("Cancel timer")}})
}
/** The manifest selects FLOATING navigation; the host supplies the original bar. */
@Composable internal fun AppleMusicBottomNavigation(tabs: List<BottomTab>,selectedIndex: Int,onTabSelected: (Int)->Unit) {
    NavigationBar {tabs.forEachIndexed{i,tab->NavigationBarItem(selected=i==selectedIndex,onClick={onTabSelected(i)},icon={Icon(tab.icon,null)},label={Text(tab.label)})}}
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun AppleMusicLibraryRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit) {
    Column {
        Row(Modifier.fillMaxWidth().combinedClickable(onClick=onClick,onLongClick=onLongPress).padding(horizontal=20.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
            val artworkPrefs=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("playlist-artwork",android.content.Context.MODE_PRIVATE)
            val artwork=item.browseId?.let {artworkPrefs.getString(it,null)} ?: item.thumbnailUrl
            val artist=item.browseId?.contains(":artist:")==true
            Box(Modifier.size(48.dp).clip(if(artist)CircleShape else RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant),contentAlignment=Alignment.Center) {
                if(artist)Text(item.title.take(1),style=MaterialTheme.typography.titleLarge)
                if(!artwork.isNullOrBlank())AsyncImage(artwork,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            }
            Column(Modifier.weight(1f).padding(horizontal=12.dp)) {
                Text(item.title,maxLines=1,overflow=TextOverflow.Ellipsis)
                if(item.subtitle.isNotBlank())Text(item.subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            IconButton(onClick=onLongPress){Icon(Icons.Rounded.MoreHoriz,"More options")}
        }
        HorizontalDivider(Modifier.padding(start=80.dp),thickness=.5.dp,color=MaterialTheme.colorScheme.onSurface.copy(alpha=.12f))
    }
}
@Composable internal fun AppleMusicBottomFade(modifier: Modifier,withMiniPlayer: Boolean,pageColor: Color) {
    Box(modifier.fillMaxWidth().height(if(withMiniPlayer)140.dp else 90.dp).background(Brush.verticalGradient(listOf(Color.Transparent,pageColor))))
}
@Composable internal fun AppleMusicLibraryStats(onOpen: ()->Unit){TextButton(onClick=onOpen){Text("Listening history")}}
internal fun LazyListScope.applemusicFeedShelves(shelves: List<HomeShelf>,layout: SkinLayout,onClick: (ShelfItem,String)->Unit,
    onLongPress: ((ShelfItem)->Unit)?,shelfBrowseId: (HomeShelf)->String?) {
    shelves.forEachIndexed { i,shelf ->
        item(key="title-$i"){Text(shelf.title,Modifier.padding(20.dp),style=MaterialTheme.typography.headlineSmall)}
        item(key="shelf-$i"){LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            items(shelf.items){item->Column(Modifier.width(166.dp).clickable{onClick(item,shelf.title)}) {
                AsyncImage(item.thumbnailUrl,null,Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(10.dp)),contentScale=ContentScale.Crop)
                Text(item.title,maxLines=1,overflow=TextOverflow.Ellipsis)
            }}
        }}
    }
}


/** Playlist rows follow the library's larger artwork and single title layout. */
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun AppleMusicPlaylistRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit,showDivider: Boolean=true) {
    val c=MaterialTheme.colorScheme
    val prefs=androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("playlist-artwork",android.content.Context.MODE_PRIVATE)
    val artwork=item.browseId?.let {prefs.getString(it,null)} ?: item.thumbnailUrl
    val hue=remember(item.browseId,item.title) { ((item.browseId ?: item.title).hashCode().toLong() and 0x7fffffff).rem(360).toFloat() }
    Column {
        Row(Modifier.fillMaxWidth().combinedClickable(onClick=onClick,onLongClick=onLongPress)
            .padding(horizontal=20.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(6.dp)).background(
                Brush.linearGradient(listOf(Color.hsv(hue,.22f,.85f),Color.hsv(hue,.35f,.22f))))) {
                Text(item.title,Modifier.padding(5.dp),color=Color.Black.copy(alpha=.8f),
                    style=MaterialTheme.typography.labelSmall.copy(fontSize=8.sp),maxLines=1,overflow=TextOverflow.Ellipsis)
                if(!artwork.isNullOrBlank()) AsyncImage(artwork,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            }
            Text(item.title,Modifier.weight(1f).padding(horizontal=12.dp),
                style=MaterialTheme.typography.bodyLarge.copy(fontSize=18.sp,letterSpacing=0.sp),maxLines=1,overflow=TextOverflow.Ellipsis)
            Icon(Icons.Rounded.ChevronRight,null,Modifier.size(20.dp),tint=c.onSurface.copy(alpha=.28f))
        }
        if(showDivider) HorizontalDivider(Modifier.padding(start=96.dp,end=20.dp),thickness=.5.dp,color=c.onSurface.copy(alpha=.18f))
    }
}
