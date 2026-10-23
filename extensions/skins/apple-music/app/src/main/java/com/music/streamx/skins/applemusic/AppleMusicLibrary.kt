package com.music.streamx.skins.applemusic

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.semantics.Role
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.ui.skins.*

private val categories = listOf("Playlists", "Artists", "Albums", "Songs", "Downloaded")

@Composable internal fun AppleMusicLibrary(shelves: List<HomeShelf>, links: List<ShelfItem>, deviceItems: List<ShelfItem>, contentPadding: PaddingValues,
    onItem: (ShelfItem, String) -> Unit, onLongPress: ((ShelfItem) -> Unit)?, onShowAll: (HomeShelf) -> Unit, onNewPlaylist: (() -> Unit)?) {
    val actions = LocalSkinLibraryActions.current
    val prefs = LocalContext.current.getSharedPreferences("skin.apple-music", Context.MODE_PRIVATE)
    var hidden by remember { mutableStateOf(prefs.getStringSet("hiddenLibraryCategories", emptySet())!!.filter { it in categories }.toSet()) }
    var order by remember { mutableStateOf(
        (prefs.getString("libraryCategoryOrder", "")!!.split('|').filter { it in categories } + categories).distinct()
    ) }
    var dragged by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val rowStep = with(LocalDensity.current) { 52.dp.toPx() }
    val moveCategory by rememberUpdatedState<(String,Int)->Boolean>({ name,direction ->
        val from=order.indexOf(name);val to=(from+direction).coerceIn(0,order.lastIndex)
        if(from>=0 && from!=to) {order=order.toMutableList().apply { add(to,removeAt(from)) };true} else false
    })
    val navigation = actions.navigation ?: remember { SkinLibraryNavigation() }
    var selected by navigation.categoryState
    var query by navigation.queryState
    var collectionError by remember { mutableStateOf<String?>(null) }
    var collectionLoading by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var sort by remember { mutableStateOf(prefs.getString("librarySort", "Recent")) }
    var menu by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    fun finishEditing() {
        prefs.edit().putStringSet("hiddenLibraryCategories",hidden).putString("libraryCategoryOrder",order.joinToString("|")).apply()
        dragged=null;dragOffset=0f;editing=false
    }
    SkinBackHandler(editing || selected != null) { if(editing)finishEditing() else selected=null }
    val c = MaterialTheme.colorScheme
    fun categoryShelf(name: String): HomeShelf {
        val matches = shelves.filter { shelf ->
            val title = shelf.title.lowercase()
            when(name) {
                "Playlists" -> "playlist" in title
                "Artists" -> "artist" in title
                "Albums" -> "album" in title
                "Songs" -> ("song" in title || "track" in title || "liked" in title) && "video" !in title
                else -> false
            }
        }
        return if(matches.size == 1) matches.first() else HomeShelf(name, matches.flatMap { it.items })
    }
    val shelf = selected?.let(::categoryShelf)
    val collectionId = shelf?.moreBrowseId.takeIf { selected in listOf("Playlists","Albums","Artists") }
    LaunchedEffect(collectionId,retry) {
        collectionError=null
        val loadCollection=actions.loadCollection
        if(collectionId != null && loadCollection != null) {
            collectionLoading=true
            try {
                val loaded=loadCollection(collectionId)
                navigation.collectionId=collectionId
                navigation.collectionItems=loaded
            } catch(cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch(error: Exception) { collectionError=error.message ?: "Could not load collection" }
            finally {collectionLoading=false}
        }
    }
    val allEntries = if(collectionId != null && navigation.collectionId==collectionId) navigation.collectionItems ?: shelf?.items else shelf?.items
    val entries = when(sort) {
        "Title" -> allEntries?.sortedBy { it.title.lowercase() }
        "Artist" -> allEntries?.sortedBy { it.subtitle.lowercase() }
        else -> allEntries
    }.orEmpty().filter { selected != "Playlists" || query.isBlank() || it.title.contains(query,ignoreCase=true) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
    Column(Modifier.padding(top=11.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal=20.dp).heightIn(min=if(selected != null && !editing)44.dp else 64.dp), verticalAlignment=Alignment.CenterVertically) {
                if(selected != null && !editing) IconButton(onClick={selected=null},modifier=Modifier.size(44.dp).clip(CircleShape).background(c.surface)
                    .border(.5.dp,c.onSurface.copy(alpha=.12f),CircleShape)) { Icon(Icons.Rounded.ArrowBack, "Library") }
                if(selected == null || editing) Text("Library", Modifier.weight(1f), style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp, fontWeight=FontWeight.ExtraBold, letterSpacing=(-.4).sp))
                else Spacer(Modifier.weight(1f))
                if(editing) {
                    IconButton(onClick=::finishEditing,modifier=Modifier.size(44.dp).clip(CircleShape).background(c.primary)) {
                        Icon(LibraryGlyphs.Check,"Done editing library",Modifier.size(25.dp),tint=Color.White)
                    }
                } else {
                Row(Modifier.clip(CircleShape).background(c.surface).border(.5.dp,c.onSurface.copy(alpha=.12f),CircleShape)) {
                    IconButton(onClick={onNewPlaylist?.invoke()}, enabled=onNewPlaylist != null, modifier=Modifier.width(54.dp).height(44.dp)) {
                        Icon(LibraryGlyphs.Add, "New playlist", Modifier.size(25.dp))
                    }
                    Box {
                        IconButton(onClick={if(selected==null)editing=true else menu=true}, modifier=Modifier.width(54.dp).height(44.dp)) {
                            Icon(LibraryGlyphs.Edit,if(selected==null)"Edit library"else"Sort library",Modifier.size(25.dp))
                        }
                        DropdownMenu(menu,{menu=false}) {
                            listOf("Recent", "Title", "Artist").forEach { value ->
                                DropdownMenuItem(text={Text(if(sort==value) "✓  $value" else value)}, onClick={sort=value;prefs.edit().putString("librarySort",value).apply();menu=false})
                            }
                            HorizontalDivider()
                            DropdownMenuItem(text={Text("Edit library")},onClick={selected=null;editing=true;menu=false})
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(44.dp).clip(CircleShape).background(c.surface).border(.5.dp,c.onSurface.copy(alpha=.16f),CircleShape)
                    .semantics { contentDescription="Account" }.clickable(enabled=actions.onAccount!=null){actions.onAccount?.invoke()},contentAlignment=Alignment.Center) {
                    val photo=actions.account?.thumbnailUrl
                    if(photo!=null) AsyncImage(photo,"Account",Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                    else Text(actions.account?.name?.split(' ')?.filter{it.isNotBlank()}?.take(2)?.joinToString(""){it.take(1)}?.uppercase()?.takeIf{it.isNotBlank()} ?: "♪",
                        style=MaterialTheme.typography.titleMedium.copy(fontSize=17.sp,fontWeight=FontWeight.SemiBold))
                }
                }
            }
            if(selected != null && !editing) Text(selected!!,
                Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,top=17.dp,bottom=4.dp),
                style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-.4).sp))
        }
    LazyColumn(Modifier.weight(1f),state=navigation.listState,contentPadding=PaddingValues(bottom=contentPadding.calculateBottomPadding())) {
        if(selected == "Playlists") item(key="playlist-search") {
            TextField(query,{query=it},Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),
                placeholder={Text("Search playlists")},leadingIcon={Icon(Icons.Rounded.Search,null)},
                trailingIcon=if(query.isNotEmpty())({IconButton(onClick={query=""}){Icon(LibraryGlyphs.Close,"Clear playlist search")}})else null,
                singleLine=true,shape=RoundedCornerShape(12.dp),
                colors=TextFieldDefaults.colors(focusedContainerColor=c.surface,unfocusedContainerColor=c.surface,
                    focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
        }
        if(selected in listOf("Playlists","Albums","Artists")) item(key="collection-status") {
            if(collectionLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal=20.dp))
            if(collectionError!=null) Row(Modifier.padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(collectionError!!,Modifier.weight(1f),color=c.error)
                TextButton(onClick={retry++}){Text("Retry")}
            }
        }
        if(selected == null) {
            items(order.filter { editing || it !in hidden },key={it}) { name ->
                val index=categories.indexOf(name)
                val visible=name !in hidden
                Box(Modifier.zIndex(if(dragged==name)1f else 0f).graphicsLayer { translationY=if(dragged==name)dragOffset else 0f }
                    .background(if(dragged==name)c.surface else Color.Transparent)) {
                    val interaction=if(editing) Modifier.toggleable(visible,role=Role.Checkbox) { checked ->
                        hidden=if(checked)hidden-name else hidden+name
                    } else Modifier.clickable {
                        if(name=="Downloaded" && actions.onDownloads!=null) actions.onDownloads?.invoke() else selected=name
                    }
                    Row(Modifier.fillMaxWidth().then(interaction).height(52.dp).padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
                        if(editing) {
                            Box(Modifier.size(22.dp).clip(CircleShape).background(if(visible)c.primary else Color.Transparent)
                                .border(if(visible)0.dp else 1.5.dp,if(visible)Color.Transparent else c.onSurface.copy(alpha=.3f),CircleShape),contentAlignment=Alignment.Center) {
                                if(visible)Icon(LibraryGlyphs.Check,null,Modifier.size(15.dp),tint=Color.White)
                            }
                            Spacer(Modifier.width(20.dp))
                        }
                        Icon(LibraryGlyphs.categories[index],null,Modifier.size(24.dp),tint=c.primary)
                        Text(name,Modifier.weight(1f).padding(start=16.dp),style=MaterialTheme.typography.bodyLarge.copy(fontSize=18.sp,fontWeight=FontWeight.Normal,letterSpacing=0.sp))
                        if(editing) Box(Modifier.width(40.dp).fillMaxHeight().semantics { contentDescription="Reorder $name" }
                            .pointerInput(name) { detectDragGestures(
                                onDragStart={dragged=name;dragOffset=0f},
                                onDragEnd={dragged=null;dragOffset=0f},
                                onDragCancel={dragged=null;dragOffset=0f},
                                onDrag={change,delta ->
                                    change.consume();dragOffset+=delta.y
                                    if(dragOffset>rowStep/2) {if(moveCategory(name,1))dragOffset-=rowStep else dragOffset=0f}
                                    else if(dragOffset < -rowStep/2) {if(moveCategory(name,-1))dragOffset+=rowStep else dragOffset=0f}
                                }) },contentAlignment=Alignment.CenterEnd) {
                            Icon(LibraryGlyphs.Handle,null,Modifier.size(24.dp),tint=c.onSurface.copy(alpha=.28f))
                        } else Icon(Icons.Rounded.ChevronRight,null,Modifier.size(20.dp),tint=c.onSurface.copy(alpha=.28f))
                    }
                    HorizontalDivider(Modifier.align(Alignment.BottomCenter).padding(start=if(editing)102.dp else 60.dp,end=20.dp),thickness=.5.dp,color=c.onSurface.copy(alpha=.18f))
                }
            }
        } else if(actions.loading || (collectionLoading && entries.isEmpty())) {
            items(6,key={"loading-$it"}) {
                Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(if(selected=="Playlists")64.dp else 48.dp).clip(RoundedCornerShape(6.dp)).background(c.surfaceVariant))
                    Column(Modifier.weight(1f).padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
                        Box(Modifier.fillMaxWidth(.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant))
                        Box(Modifier.fillMaxWidth(.45f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(c.surfaceVariant.copy(alpha=.6f)))
                    }
                }
            }
        } else {
            itemsIndexed(entries,key={i,it->"${it.browseId ?: it.videoId}-$i"}) { index,entry ->
                if(selected == "Playlists") AppleMusicPlaylistRow(entry,{onItem(entry,"Playlists")},{onLongPress?.invoke(entry)},showDivider=index<entries.lastIndex)
                else AppleMusicLibraryRow(entry,{onItem(entry,selected!!)}, {onLongPress?.invoke(entry)})
            }
            if(selected !in listOf("Playlists","Albums","Artists") && shelf?.moreBrowseId!=null) item { TextButton(onClick={onShowAll(shelf) },modifier=Modifier.padding(horizontal=12.dp)){Text("Show all")} }
            if(entries.isEmpty()) item { Text(if(selected=="Playlists" && query.isNotBlank())"No matching playlists" else "Nothing here yet",Modifier.padding(24.dp),color=c.onSurfaceVariant) }
            if(selected=="Downloaded") items(links+deviceItems) { entry -> AppleMusicLibraryRow(entry,{onItem(entry,"Downloaded")},{onLongPress?.invoke(entry)}) }
        }
    }
    }
}

private object LibraryGlyphs {
    private fun glyph(name:String, block: androidx.compose.ui.graphics.vector.PathBuilder.()->Unit): ImageVector =
        ImageVector.Builder(name,26.dp,26.dp,26f,26f).apply {
            path(stroke=SolidColor(Color.Black),strokeLineWidth=1.8f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round,pathBuilder=block)
        }.build()
    val Close = glyph("library_clear") { moveTo(6f,6f);lineTo(20f,20f);moveTo(20f,6f);lineTo(6f,20f) }
    val Check = glyph("library_check") {moveTo(4f,13f);lineTo(10f,19f);lineTo(22f,5f)}
    val Handle = glyph("library_handle") {for(y in listOf(7f,13f,19f)){moveTo(3f,y);lineTo(23f,y)}}
    val Add = ImageVector.Builder("library_add_playlist",26.dp,26.dp,26f,26f).apply {
        path(fill=SolidColor(Color.Black),pathFillType=PathFillType.EvenOdd) {
            moveTo(12f,7f);arcToRelative(5f,5f,0f,true,true,-10f,0f);arcToRelative(5f,5f,0f,true,true,10f,0f);close()
            moveTo(6.2f,3.7f);lineTo(7.8f,3.7f);lineTo(7.8f,6.2f);lineTo(10.3f,6.2f)
            lineTo(10.3f,7.8f);lineTo(7.8f,7.8f);lineTo(7.8f,10.3f);lineTo(6.2f,10.3f)
            lineTo(6.2f,7.8f);lineTo(3.7f,7.8f);lineTo(3.7f,6.2f);lineTo(6.2f,6.2f);close()
        }
        path(stroke=SolidColor(Color.Black),strokeLineWidth=1.8f,strokeLineCap=StrokeCap.Round) {
            moveTo(15f,5f);lineTo(24f,5f);moveTo(15f,10f);lineTo(24f,10f)
            moveTo(3f,16f);lineTo(24f,16f);moveTo(3f,22f);lineTo(24f,22f)
        }
    }.build()
    val Edit = ImageVector.Builder("library_checklist",26.dp,26.dp,26f,26f).apply {
        path(fill=SolidColor(Color.Black),pathFillType=PathFillType.EvenOdd) {
            moveTo(10.4f,6f);arcToRelative(4.4f,4.4f,0f,true,true,-8.8f,0f)
            arcToRelative(4.4f,4.4f,0f,true,true,8.8f,0f);close()
            moveTo(3.1f,5.8f);lineTo(4.2f,4.8f);lineTo(5.5f,6.3f);lineTo(8.1f,3.1f)
            lineTo(9.1f,4.1f);lineTo(5.5f,8.5f);close()
        }
        path(stroke=SolidColor(Color.Black),strokeLineWidth=1.7f,strokeLineCap=StrokeCap.Round) {
            moveTo(14f,6f);lineTo(25f,6f);moveTo(14f,20f);lineTo(25f,20f)
            moveTo(10f,20f);arcToRelative(4f,4f,0f,true,true,-8f,0f)
            arcToRelative(4f,4f,0f,true,true,8f,0f);close()
        }
    }.build()
    private val note: androidx.compose.ui.graphics.vector.PathBuilder.()->Unit = {
        moveTo(17f,3f);lineTo(24f,1f);lineTo(24f,7.5f);lineTo(19f,9f);lineTo(19f,20f)
        curveTo(19f,23.2f,15f,25.5f,11.3f,25.5f)
        curveTo(6.6f,25.5f,7f,20.3f,11f,18.7f)
        curveTo(13f,17.8f,15.5f,17.2f,17f,17.5f);close()
    }
    val categories=listOf(
        ImageVector.Builder("library_playlists_filled",26.dp,26.dp,26f,26f).apply {
            path(fill=SolidColor(Color.Black),pathBuilder=note)
            path(stroke=SolidColor(Color.Black),strokeLineWidth=1.8f,strokeLineCap=StrokeCap.Round) {
                moveTo(2f,5f);lineTo(12f,5f);moveTo(2f,9f);lineTo(12f,9f);moveTo(2f,13f);lineTo(12f,13f)
            }
        }.build(),
        glyph("library_artists") { moveTo(19f,3f);curveTo(23f,6f,17f,12f,14f,9f);curveTo(11f,6f,16f,0f,19f,3f);moveTo(14f,9f);lineTo(6f,22f);lineTo(3f,23f);lineTo(4f,19f);lineTo(12f,7f);moveTo(10f,16f);curveTo(18f,26f,22f,19f,17f,16f) },
        glyph("library_albums") { moveTo(6f,2f);lineTo(20f,2f);moveTo(4f,5f);lineTo(22f,5f);moveTo(5f,8f);lineTo(21f,8f);lineTo(21f,24f);lineTo(5f,24f);close() },
        ImageVector.Builder("library_songs_filled",26.dp,26.dp,26f,26f).apply {
            path(fill=SolidColor(Color.Black),pathBuilder=note)
        }.build(),
        glyph("library_downloaded") { moveTo(24f,13f);arcToRelative(11f,11f,0f,true,true,-22f,0f);arcToRelative(11f,11f,0f,true,true,22f,0f);moveTo(13f,6f);lineTo(13f,19f);moveTo(8f,14f);lineTo(13f,19f);lineTo(18f,14f) }
    )
}

@Composable internal fun AppleMusicFeedHeader(title: String,actions: SkinLibraryActions) {
    val c=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,top=11.dp,bottom=12.dp).heightIn(min=64.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-.4).sp))
        Box(Modifier.size(44.dp).clip(CircleShape).background(c.surface).border(.5.dp,c.onSurface.copy(alpha=.16f),CircleShape)
            .semantics {contentDescription="Account"}.clickable(enabled=actions.onAccount!=null){actions.onAccount?.invoke()},contentAlignment=Alignment.Center) {
            val photo=actions.account?.thumbnailUrl
            if(photo!=null) AsyncImage(photo,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            else Text(actions.account?.name?.split(' ')?.filter{it.isNotBlank()}?.take(2)?.joinToString(""){it.take(1)}?.uppercase()?.takeIf{it.isNotBlank()} ?: "♪",
                style=MaterialTheme.typography.titleMedium.copy(fontSize=17.sp,fontWeight=FontWeight.SemiBold))
        }
    }
}
