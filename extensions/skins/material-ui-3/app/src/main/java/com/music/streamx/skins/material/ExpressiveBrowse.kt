package com.music.streamx.skins.material

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.SkinLayout

@Composable internal fun ExpressiveBottomNavigation(tabs: List<BottomTab>,selectedIndex: Int,onTabSelected: (Int)->Unit) {
    NavigationBar(modifier=Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).heightIn(min=96.dp),containerColor=MaterialTheme.colorScheme.surfaceContainer,tonalElevation=0.dp,windowInsets=WindowInsets.navigationBars) {
        tabs.forEachIndexed {index,tab->NavigationBarItem(selected=selectedIndex==index,onClick={onTabSelected(index)},icon={Icon(expressiveIcon(tab.icon),null)},label={Text(tab.label,maxLines=1)},alwaysShowLabel=true)}
    }
}
@Composable internal fun ExpressiveHomeHeader(account: Account?,onAccount: ()->Unit,onLocal: ()->Unit,onHistory: ()->Unit,onExplore: ()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
        FilledTonalIconButton(onClick=onAccount,modifier=Modifier.size(48.dp)) {
            if(account?.thumbnailUrl!=null)AsyncImage(account.thumbnailUrl,"Account",modifier=Modifier.size(36.dp).clip(CircleShape),contentScale=ContentScale.Crop) else Icon(ExpressiveSymbols.AccountCircle,"Account")
        }
        Text("Listen",Modifier.weight(1f).padding(horizontal=12.dp),style=MaterialTheme.typography.titleLarge,maxLines=1)
        IconButton(onClick=onLocal){Icon(ExpressiveSymbols.Folder,"Local files")}
        IconButton(onClick=onHistory){Icon(ExpressiveSymbols.History,"History")}
        IconButton(onClick=onExplore){Icon(ExpressiveSymbols.Explore,"Explore")}
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ExpressiveLibraryRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit) {
    ListItem(headlineContent={Text(item.title,maxLines=2,overflow=TextOverflow.Ellipsis)},supportingContent={Text(item.subtitle,maxLines=2,overflow=TextOverflow.Ellipsis)},leadingContent={AsyncImage(item.thumbnailUrl,null,Modifier.size(60.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer),contentScale=ContentScale.Crop)},trailingContent={Icon(ExpressiveSymbols.ChevronRight,null)},modifier=Modifier.combinedClickable(onClick=onClick,onLongClick=onLongPress).padding(horizontal=4.dp),colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surface))
}
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.expressiveFeedShelves(shelves: List<HomeShelf>,layout: SkinLayout,onClick: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,shelfBrowseId: (HomeShelf)->String?) {
    if(shelves.isNotEmpty())item(key="expressive-feed-heading") {
        Column(Modifier.padding(start=20.dp,end=20.dp,top=12.dp,bottom=20.dp)) {
            Text("Your next favourite.",style=MaterialTheme.typography.displayLarge)
            Text("Discover albums, artists and playlists.",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=8.dp))
        }
    }
    shelves.forEachIndexed {index,shelf->item(key="expressive-shelf-$index") {
        Column(Modifier.padding(vertical=12.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(shelf.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                shelfBrowseId(shelf)?.let {id->IconButton(onClick={onClick(ShelfItem(shelf.title,shelf.subtitle,null,null,id),shelf.title)}) {Icon(ExpressiveSymbols.ArrowForward,"More ${shelf.title}")}}
            }
            if(shelf.subtitle.isNotBlank())Text(shelf.subtitle,Modifier.padding(horizontal=20.dp,vertical=4.dp),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                itemsIndexed(shelf.items){cardIndex,entry->
                    Column(Modifier.width(layout.shelfWidthDp.dp).combinedClickable(onClick={onClick(entry,shelf.title)},onLongClick=onLongPress?.let{{it(entry)}})) {
                        AsyncImage(entry.thumbnailUrl,null,Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(if(index==0 && cardIndex%2==0)32.dp else 16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),contentScale=ContentScale.Crop)
                        Text(entry.title,Modifier.padding(top=10.dp),style=MaterialTheme.typography.titleMedium,minLines=2,maxLines=2,overflow=TextOverflow.Ellipsis)
                        Text(entry.subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,minLines=2,maxLines=2,overflow=TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }}
}
@Composable internal fun ExpressiveReleaseHeader(page: DetailPage,songs: List<Song>,onPlay: ()->Unit,onShuffle: ()->Unit,onSearch: ()->Unit,onMore: ((List<Song>)->Unit)?,onArtist: (String,String)->Unit,onToggleLibrary: (()->Unit)?,topPadding: Dp) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(top=topPadding)) {
        val wide=maxWidth>=600.dp
        @Composable fun Artwork(){AsyncImage(page.thumbnailUrl,null,Modifier.widthIn(max=320.dp).fillMaxWidth(if(wide).4f else .7f).aspectRatio(1f).clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),contentScale=ContentScale.Crop)}
        @Composable fun Info(){Column(Modifier.widthIn(max=640.dp).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(page.type.name.lowercase().replaceFirstChar{it.uppercase()},style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
            Text(page.title,style=MaterialTheme.typography.headlineLarge)
            Text(page.subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
                Button(onClick=onPlay,enabled=songs.isNotEmpty(),contentPadding=PaddingValues(horizontal=24.dp,vertical=16.dp)){Icon(ExpressiveSymbols.PlayArrow,null);Spacer(Modifier.width(8.dp));Text("Play")}
                FilledTonalIconButton(onClick=onShuffle,enabled=songs.isNotEmpty()){Icon(ExpressiveSymbols.Shuffle,"Shuffle")}
                IconButton(onClick=onSearch){Icon(ExpressiveSymbols.Search,"Search tracks")}
                onToggleLibrary?.let {IconButton(onClick=it){Icon(if(page.library?.saved==true)ExpressiveSymbols.Check else ExpressiveSymbols.Add,"Save to library")}}
                onMore?.let {IconButton(onClick={it(songs)}){Icon(ExpressiveSymbols.MoreVert,"More")}}
            }
        }}
        if(wide)Row(Modifier.fillMaxWidth().padding(horizontal=24.dp),verticalAlignment=Alignment.CenterVertically){Artwork();Info()} else Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Artwork();Info()}
    }
}
@Composable internal fun ExpressiveBottomFade(modifier: Modifier,withMiniPlayer: Boolean,pageColor: Color) {
    val surface=MaterialTheme.colorScheme.background
    Box(modifier.fillMaxWidth().height(if(withMiniPlayer)176.dp else 116.dp).background(Brush.verticalGradient(0f to surface.copy(alpha=0f),.5f to surface.copy(alpha=.2f),1f to surface)))
}
@Composable internal fun ExpressiveLibraryStats(onOpen: ()->Unit) { TextButton(onClick=onOpen,modifier=Modifier.padding(horizontal=16.dp)){Icon(ExpressiveSymbols.Insights,null);Spacer(Modifier.width(8.dp));Text("Listening stats")} }
@Composable internal fun ExpressiveSleepTimerDialog(minutes: Int?,afterTrack: Boolean,running: Boolean,presets: List<Int>,onStart: (Int)->Unit,onAfterTrack: ()->Unit,onCancel: ()->Unit,onDismiss: ()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,icon={Icon(ExpressiveSymbols.Bedtime,null)},title={Text("Sleep timer")},text={Column {
        Text(if(afterTrack)"Stops after this track" else if(running)"${minutes ?: 0} minutes remaining" else "Choose when playback stops",color=MaterialTheme.colorScheme.onSurfaceVariant)
        presets.forEach {value->TextButton(onClick={onStart(value);onDismiss()},modifier=Modifier.fillMaxWidth()){Text("$value minutes")}}
        TextButton(onClick={onAfterTrack();onDismiss()},modifier=Modifier.fillMaxWidth()){Text("After this track")}
    }},confirmButton={TextButton(onClick=onDismiss){Text("Done")}},dismissButton={if(running || afterTrack)TextButton(onClick={onCancel();onDismiss()}){Text("Turn off")}})
}
