package com.music.streamx.skins.looper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.*

/** Looper PremiumSection equivalent: matching faint border, clipped ink and press scale. */
@Composable internal fun LooperTile(onClick: ()->Unit,modifier: Modifier=Modifier,shape: Shape=Capsule,color: Color=Ink,selected: Boolean=false,bordered: Boolean=true,content: @Composable RowScope.()->Unit) {
    val interaction=remember{MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState()
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val scale by animateFloatAsState(if(pressed && !reduce).97f else 1f,tween(if(reduce)0 else 140),label="Looper press")
    Row(modifier.graphicsLayer{scaleX=scale;scaleY=scale}.clip(shape).background(color).then(if(bordered)Modifier.border(.8.dp,Color.White.copy(alpha=.05f),shape)else Modifier).clickable(interactionSource=interaction,indication=null,onClick=onClick),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically,content=content)
}
@Composable internal fun LooperIconButton(icon: ImageVector,label: String,onClick: ()->Unit){LooperTile(onClick,Modifier.size(48.dp),CircleShape){Icon(icon,label,Modifier.size(20.dp),tint=Color.White)}}
@Composable internal fun LooperBrowseHeader(onSearch: ()->Unit,onSettings: ()->Unit,onExplore: ()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
        TextButton(onClick=onExplore,shape=Capsule){Text("Explore",style=MaterialTheme.typography.labelLarge,color=Dim)}
        Spacer(Modifier.weight(1f));LooperIconButton(LooperIcons.Search,"Search",onSearch);Spacer(Modifier.width(5.dp));LooperIconButton(LooperIcons.Settings,"Settings",onSettings)
    }
}
@Composable internal fun LooperHomeHeader(account: Account?,onAccount: ()->Unit,onLocal: ()->Unit,onHistory: ()->Unit,onExplore: ()->Unit)=LooperBrowseHeader(onExplore,onAccount,onExplore)
internal fun endShape(left: Boolean)=RoundedCornerShape(topStart=if(left)36.dp else 12.dp,bottomStart=if(left)36.dp else 12.dp,topEnd=if(left)12.dp else 36.dp,bottomEnd=if(left)12.dp else 36.dp)
@Composable internal fun LooperBottomNavigation(tabs: List<BottomTab>,selectedIndex: Int,onTabSelected: (Int)->Unit) {
    val entries=listOf(Triple(0,"Home",LooperIcons.Home),Triple(3,"Songs",LooperIcons.Songs),Triple(2,"Library",LooperIcons.Library)).filter{it.first<tabs.size}
    val active=entries.indexOfFirst{it.first==selectedIndex}
    @Composable fun Item(i: Int,modifier: Modifier){
        val (index,label,icon)=entries[i];val chosen=index==selectedIndex
        Row(modifier.height(72.dp).clip(if(active==1 && i==1)RoundedCornerShape(12.dp)else if(i==0)endShape(true)else endShape(false)).selectable(chosen,role=Role.Tab,onClick={onTabSelected(index)}),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){Icon(icon,null,Modifier.size(18.dp),tint=if(chosen)Pink else Dim);Spacer(Modifier.width(6.dp));Text(label,color=if(chosen)Pink else Dim,style=MaterialTheme.typography.labelLarge,fontWeight=if(chosen)androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal)}
    }
    @Composable fun Group(modifier: Modifier,left: Boolean,indices: List<Int>){Row(modifier.clip(endShape(left)).background(Ink).border(.8.dp,Color.White.copy(alpha=.05f),endShape(left))){indices.forEach{Item(it,Modifier.weight(1f))}}}
    Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(start=16.dp,end=16.dp,top=2.dp,bottom=16.dp).selectableGroup(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        if(entries.size!=3)entries.indices.forEach{Item(it,Modifier.weight(1f).background(Ink,Capsule))}
        else when(active){
            0->{Group(Modifier.weight(1f),true,listOf(0));Group(Modifier.weight(2f),false,listOf(1,2))}
            2->{Group(Modifier.weight(2f),true,listOf(0,1));Group(Modifier.weight(1f),false,listOf(2))}
            else->{Group(Modifier.weight(1f),true,listOf(0));Item(1,Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Ink).border(.8.dp,Color.White.copy(alpha=.05f),RoundedCornerShape(12.dp)));Group(Modifier.weight(1f),false,listOf(2))}
        }
    }
}
@Composable private fun Section(title: String,subtitle: String?=null,onMore: (()->Unit)?=null) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.headlineLarge);subtitle?.takeIf{it.isNotBlank()}?.let{Text(it,style=MaterialTheme.typography.bodyLarge,color=Dim)}};onMore?.let{LooperTile(it,Modifier.heightIn(min=48.dp).padding(start=8.dp)){Text("View All →",Modifier.padding(horizontal=16.dp),style=MaterialTheme.typography.labelLarge)}}}
}
internal fun LazyListScope.looperFeedShelves(shelves: List<HomeShelf>,layout: SkinLayout,onClick: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,shelfBrowseId: (HomeShelf)->String?) {
    val pickShelf=shelves.firstOrNull{it.items.count{entry->entry.videoId!=null}>=3} ?: shelves.firstOrNull()
    if(pickShelf!=null && LooperPreferences.quickPicks){
        item{Section("Quick Picks","Today Mix for you")}
        item {QuickPicks(pickShelf,{onClick(it,pickShelf.title)},onLongPress)}
    }
    shelves.forEachIndexed{index,shelf->
        val more=shelfBrowseId(shelf)?.let{id->{onClick(ShelfItem(shelf.title,shelf.subtitle,null,null,id),shelf.title)}}
        item(key="heading-$index"){Section(shelf.title,shelf.subtitle,more)}
        if(shelf.items.count{it.videoId!=null}>shelf.items.size/2)itemsIndexed(shelf.items.take(5),key={i,_->"song-$index-$i"}){_,entry->LooperLibraryRow(entry,{onClick(entry,shelf.title)},{onLongPress?.invoke(entry)})}
        else item(key="shelf-$index"){LazyRow(contentPadding=PaddingValues(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){items(shelf.items){entry->Column(Modifier.width(120.dp).clip(RoundedCornerShape(8.dp)).clickable{onClick(entry,shelf.title)}){AsyncImage(entry.thumbnailUrl,null,Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)),contentScale=ContentScale.Crop);Text(entry.title,Modifier.padding(top=6.dp),style=MaterialTheme.typography.bodyLarge,maxLines=1,overflow=TextOverflow.Ellipsis);Text(entry.subtitle,style=MaterialTheme.typography.bodyMedium,color=Dim,maxLines=1,overflow=TextOverflow.Ellipsis)}}}}
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun QuickPicks(shelf: HomeShelf,onClick: (ShelfItem)->Unit,onLongPress: ((ShelfItem)->Unit)?) {
    val pages=shelf.items.take(18).chunked(6);if(pages.isEmpty())return
    val pager=rememberPagerState(pageCount={pages.size})
    HorizontalPager(pager,modifier=Modifier.fillMaxWidth()){page->Column(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        pages[page].chunked(3).forEach{row->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            row.forEach{entry->Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(Ink).combinedClickable(onClick={onClick(entry)},onLongClick=onLongPress?.let{{it(entry)}})){
                AsyncImage(entry.thumbnailUrl,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                Box(Modifier.fillMaxWidth().height(48.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent,Color.Black.copy(alpha=.8f)))))
                Text(entry.title,Modifier.align(Alignment.BottomStart).padding(8.dp),style=MaterialTheme.typography.labelLarge,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis)
            }}
            repeat(3-row.size){Spacer(Modifier.weight(1f))}
        }}
    }}
    if(pages.size>1)Row(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalArrangement=Arrangement.Center){repeat(pages.size){i->Box(Modifier.padding(horizontal=3.dp).size(if(i==pager.currentPage)16.dp else 6.dp,6.dp).background(if(i==pager.currentPage)Color.White else Dim.copy(alpha=.4f),Capsule))}}
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun LooperLibraryRow(item: ShelfItem,onClick: ()->Unit,onLongPress: ()->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).combinedClickable(onClick=onClick,onLongClick=onLongPress).heightIn(min=68.dp).padding(horizontal=16.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){
        AsyncImage(item.thumbnailUrl,null,Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(Ink),contentScale=ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal=14.dp)){Text(item.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(item.subtitle,style=MaterialTheme.typography.bodyLarge,color=Dim,maxLines=1,overflow=TextOverflow.Ellipsis)}
        IconButton(onClick=onLongPress){Icon(LooperIcons.More,"More options",Modifier.size(18.dp),tint=Dim)}
    }
}
@Composable internal fun LooperLibrary(shelves: List<HomeShelf>,links: List<ShelfItem>,deviceItems: List<ShelfItem>,contentPadding: PaddingValues,onItem: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,onShowAll: (HomeShelf)->Unit,onNewPlaylist: (()->Unit)?) {
    var category by remember(shelves){mutableStateOf<HomeShelf?>(null)}
    SkinBackHandler(category!=null){category=null}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=contentPadding){
        item{Section(category?.title ?: "Library")}
        val selected=category
        if(selected!=null){item{TextButton(onClick={category=null}){Text("← Categories")}};items(selected.items){entry->LooperLibraryRow(entry,{onItem(entry,selected.title)},{onLongPress?.invoke(entry)})}}
        else {
            val recent=shelves.firstOrNull{it.title.contains("recent",true)}
            if(recent!=null){item{Text("Recent Played",Modifier.padding(20.dp).background(Ink,Capsule).padding(horizontal=16.dp,vertical=10.dp),style=MaterialTheme.typography.labelLarge)};item{LazyRow(contentPadding=PaddingValues(horizontal=20.dp)){items(recent.items.take(8)){entry->LooperTile({onItem(entry,recent.title)},Modifier.padding(end=6.dp).width(140.dp).height(40.dp),RoundedCornerShape(8.dp)){AsyncImage(entry.thumbnailUrl,null,Modifier.size(28.dp).clip(RoundedCornerShape(4.dp)),contentScale=ContentScale.Crop);Text(entry.title,Modifier.padding(horizontal=6.dp),style=MaterialTheme.typography.labelSmall,maxLines=1,overflow=TextOverflow.Ellipsis)}}}}}
            item{Text("Categories",Modifier.padding(20.dp).background(Ink,Capsule).padding(horizontal=16.dp,vertical=10.dp),style=MaterialTheme.typography.labelLarge)}
            item{Column(Modifier.padding(horizontal=20.dp).clip(Panel).background(Ink)){shelves.filterNot{it===recent}.forEach{shelf->LooperSettingsRow(LooperIcons.Library,null,shelf.title,null,null,null,null,true,{category=shelf},null);LooperSettingsDivider()};onNewPlaylist?.let{LooperSettingsRow(null,null,"Create playlist",null,null,null,null,true,it,null)}}}
            items(links+deviceItems){entry->LooperLibraryRow(entry,{onItem(entry,"On device")},{onLongPress?.invoke(entry)})}
        }
    }
}
@Composable internal fun LooperReleaseHeader(page: DetailPage,songs: List<Song>,onPlay: ()->Unit,onShuffle: ()->Unit,onSearch: ()->Unit,onMore: ((List<Song>)->Unit)?,onArtist: (String,String)->Unit,onToggleLibrary: (()->Unit)?,topPadding: Dp) {
    Column(Modifier.fillMaxWidth().padding(top=topPadding),horizontalAlignment=Alignment.CenterHorizontally){AsyncImage(page.thumbnailUrl,null,Modifier.widthIn(max=320.dp).fillMaxWidth(.75f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)),contentScale=ContentScale.Crop);Text(page.title,Modifier.padding(20.dp),style=MaterialTheme.typography.headlineLarge);Text(page.subtitle,color=Dim);Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){LooperTile(onPlay,Modifier.weight(1f).height(56.dp)){Icon(LooperIcons.Play,null,Modifier.size(20.dp));Text(" Play")};LooperIconButton(LooperIcons.Shuffle,"Shuffle",onShuffle);LooperIconButton(LooperIcons.Search,"Search tracks",onSearch);onMore?.let{LooperIconButton(LooperIcons.More,"More",{it(songs)})}}}
}
@Composable internal fun LooperBottomFade(modifier: Modifier,withMiniPlayer: Boolean,pageColor: Color){Box(modifier.fillMaxWidth().height(if(withMiniPlayer)180.dp else 100.dp).background(Brush.verticalGradient(listOf(Color.Transparent,Color.Black))))}
@Composable internal fun LooperLibraryStats(onOpen: ()->Unit){TextButton(onClick=onOpen){Text("Listening history")}}
