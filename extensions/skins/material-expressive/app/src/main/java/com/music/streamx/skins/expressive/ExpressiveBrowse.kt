package com.music.streamx.skins.expressive

import androidx.compose.foundation.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.animation.core.*
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.ui.skins.skinPlayerSettings
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable internal fun ExpressiveBottomNavigation(tabs: List<BottomTab>,selectedIndex: Int,onTabSelected: (Int)->Unit) {
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val spatial=MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    Surface(color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=12.dp,vertical=12.dp).selectableGroup(),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically) {
            tabs.forEachIndexed {index,tab->
                val selected=selectedIndex==index
                val weight by animateFloatAsState(if(selected)1.7f else 1f,animationSpec=if(reduce)snap() else spatial,label="navigationWidth-$index")
                Surface(shape=RoundedCornerShape(if(selected)24.dp else 32.dp),color=if(selected)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                    contentColor=if(selected)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier=Modifier.weight(weight).heightIn(min=72.dp).clip(RoundedCornerShape(if(selected)24.dp else 32.dp)).selectable(selected=selected,role=Role.Tab,onClick={onTabSelected(index)})) {
                    if(selected)Row(Modifier.padding(horizontal=12.dp),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                        Icon(expressiveIcon(tab.icon),null,Modifier.size(26.dp));Spacer(Modifier.width(8.dp));Text(tab.label,style=MaterialTheme.typography.labelLarge,maxLines=1,overflow=TextOverflow.Ellipsis)
                    } else Column(Modifier.padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
                        Icon(expressiveIcon(tab.icon),null,Modifier.size(24.dp));Text(tab.label,style=MaterialTheme.typography.labelSmall,maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
@Composable internal fun ExpressiveHomeHeader(account: Account?,onAccount: ()->Unit,onLocal: ()->Unit,onHistory: ()->Unit,onExplore: ()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
        FilledTonalIconButton(onClick=onAccount,modifier=Modifier.size(48.dp)) {
            if(account?.thumbnailUrl!=null)AsyncImage(account.thumbnailUrl,"Account",modifier=Modifier.size(36.dp).clip(CircleShape),contentScale=ContentScale.Crop) else Icon(ExpressiveSymbols.AccountCircle,"Account")
        }
        Text("For you",Modifier.weight(1f).padding(horizontal=12.dp),style=MaterialTheme.typography.titleLarge,maxLines=1)
        IconButton(onClick=onLocal){Icon(ExpressiveSymbols.Folder,"Local files")}
        IconButton(onClick=onHistory){Icon(ExpressiveSymbols.History,"History")}
        IconButton(onClick=onExplore){Icon(ExpressiveSymbols.Explore,"Explore")}
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ExpressiveLibraryRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit) {
    Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.padding(horizontal=16.dp,vertical=4.dp).clip(RoundedCornerShape(24.dp)).combinedClickable(onClick=onClick,onLongClick=onLongPress)) {
        ListItem(headlineContent={Text(item.title,maxLines=2,overflow=TextOverflow.Ellipsis)},supportingContent={Text(item.subtitle,maxLines=2,overflow=TextOverflow.Ellipsis)},leadingContent={AsyncImage(item.thumbnailUrl,null,Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),contentScale=ContentScale.Crop)},trailingContent={Icon(ExpressiveSymbols.ChevronRight,null)},colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainer))
    }
}
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.expressiveFeedShelves(shelves: List<HomeShelf>,layout: SkinLayout,onClick: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,shelfBrowseId: (HomeShelf)->String?) {
    if(shelves.isNotEmpty())item(key="expressive-feed-heading") {
        Surface(Modifier.padding(horizontal=16.dp,vertical=12.dp).fillMaxWidth(),color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(topStart=16.dp,topEnd=40.dp,bottomStart=40.dp,bottomEnd=16.dp)) {
            Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("IN YOUR ELEMENT",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Find your\nnext favourite.",style=MaterialTheme.typography.displayLarge,color=MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Albums, artists and playlists for your kind of day.",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onPrimaryContainer)
            }
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
                    Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainer),modifier=Modifier.width(layout.shelfWidthDp.dp).clip(RoundedCornerShape(28.dp)).combinedClickable(onClick={onClick(entry,shelf.title)},onLongClick=onLongPress?.let{{it(entry)}})) {
                        Column {
                            AsyncImage(entry.thumbnailUrl,null,Modifier.fillMaxWidth().aspectRatio(1f).padding(8.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh),contentScale=ContentScale.Crop)
                            Column(Modifier.padding(start=16.dp,end=16.dp,bottom=16.dp)) {
                                Text(entry.title,style=MaterialTheme.typography.titleMedium,minLines=2,maxLines=2,overflow=TextOverflow.Ellipsis)
                                Text(entry.subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,minLines=2,maxLines=2,overflow=TextOverflow.Ellipsis)
                            }
                        }
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
                Button(onClick=onPlay,enabled=songs.isNotEmpty(),contentPadding=PaddingValues(horizontal=32.dp,vertical=20.dp)){Icon(ExpressiveSymbols.PlayArrow,null);Spacer(Modifier.width(8.dp));Text("Play")}
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
