package com.music.streamx.skins.applemusic

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.animateScrollBy
import com.music.streamx.ui.components.GlassMenu
import com.music.streamx.ui.components.GlassMenuRow
import com.music.streamx.ui.components.GlassMenuSeparator
import com.music.streamx.ui.components.MenuAnchor
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
import com.music.streamx.ui.components.cardZoomSource

private val categories = listOf("Playlists", "Artists", "Albums", "Songs", "Made for You", "Downloaded")

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
    // A section's own view and filter, as Apple's ••• and filter menus set them.
    var gridView by remember { mutableStateOf(prefs.getBoolean("libraryGrid", false)) }
    var listFilter by remember(selected) { mutableStateOf("All") }
    var viewMenu by remember { mutableStateOf(false) }
    var filterMenu by remember { mutableStateOf(false) }
    fun finishEditing() {
        prefs.edit().putStringSet("hiddenLibraryCategories",hidden).putString("libraryCategoryOrder",order.joinToString("|")).apply()
        dragged=null;dragOffset=0f;editing=false
    }
    SkinBackHandler(editing || selected != null) { if(editing)finishEditing() else selected=null }
    // What was pinned, as the cards already loaded for this Library.
    val pins = rememberSkinPinnedItems()
    val known = remember(shelves) { shelves.flatMap { it.items }.filter { it.browseId != null }.associateBy { it.browseId } }
    val pinned = pins.mapNotNull { known[it] }
    val c = MaterialTheme.colorScheme
    fun categoryShelf(name: String): HomeShelf {
        val matches = shelves.filter { shelf ->
            val title = shelf.title.lowercase()
            when(name) {
                "Playlists" -> "playlist" in title
                "Artists" -> "artist" in title
                "Albums" -> "album" in title
                "Songs" -> ("song" in title || "track" in title || "liked" in title) && "video" !in title
                "Made for You" -> title == "made for you"
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
        .filter { entry -> when (listFilter) {
            "Favourites" -> entry.browseId in pins
            "Downloads" -> entry.browseId?.let { it.startsWith("local:") || "download" in it.lowercase() } == true
            else -> true
        } }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
    // The large title scrolls with the list, as on Apple's own Library and the Settings pages;
    // only the buttons stay, over the shared top fade, with the title handed to the bar.
    val pageTitle = if(selected == null || editing) "Library" else selected!!
    // As on iOS, the playlist search sits hidden above the list until the list is pulled down past its top.
    var searchShown by remember(selected) { mutableStateOf(false) }
    val revealSearch = remember(selected) { object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
        override fun onPostScroll(consumed: androidx.compose.ui.geometry.Offset, available: androidx.compose.ui.geometry.Offset,
            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
            if (selected == "Playlists" && available.y > 6f && source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput) searchShown = true
            return androidx.compose.ui.geometry.Offset.Zero
        }
    } }
    // The host's glass over this list: the bar frosts it, the buttons refract it.
    val glassBackdrop = LocalSkinGlassFactory.current()
    val glassButton: @Composable (androidx.compose.foundation.shape.CornerBasedShape) -> Modifier = { shape ->
        glassBackdrop?.glass(shape) ?: Modifier.clip(shape).background(c.surface).border(.5.dp,c.onSurface.copy(alpha=.12f),shape)
    }
    // The Library's own name stays put in the bar, as Apple's does; a section's scrolls up into it.
    val rootTitle = selected == null || editing
    LazyColumn(Modifier.fillMaxSize().then(glassBackdrop?.source ?: Modifier).nestedScroll(revealSearch),state=navigation.listState,contentPadding=PaddingValues(top=LIBRARY_BAR_HEIGHT,bottom=contentPadding.calculateBottomPadding())) {
        if(!rootTitle) item(key="large-title") {
            Text(pageTitle,Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,top=if(selected != null && !editing)6.dp else 0.dp,bottom=6.dp),
                style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-.4).sp))
        }
        if(selected == "Playlists") item(key="playlist-search") {
            androidx.compose.animation.AnimatedVisibility(searchShown || query.isNotEmpty(),
                enter=androidx.compose.animation.expandVertically()+androidx.compose.animation.fadeIn(),
                exit=androidx.compose.animation.shrinkVertically()+androidx.compose.animation.fadeOut()) {
                TextField(query,{query=it},Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=6.dp).height(52.dp),
                    placeholder={Text("Search")},leadingIcon={Icon(Icons.Rounded.Search,null,tint=c.onBackground)},
                    trailingIcon=if(query.isNotEmpty())({IconButton(onClick={query=""}){Icon(LibraryGlyphs.Close,"Clear playlist search")}})else null,
                    singleLine=true,shape=CircleShape,
                    colors=TextFieldDefaults.colors(focusedContainerColor=c.surface,unfocusedContainerColor=c.surface,
                        focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
            }
        }
        if(selected in listOf("Playlists","Albums","Artists")) item(key="collection-status") {
            if(collectionError!=null) Row(Modifier.padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(collectionError!!,Modifier.weight(1f),color=c.error)
                TextButton(onClick={retry++}){Text("Retry")}
            }
        }
        if(selected == null) {
            // Pins sit at the top of Apple's Library, three to a row, above the list.
            if(!editing && pinned.isNotEmpty()) {
                items(pinned.chunked(3),key={row->"pins-${row.first().browseId}"}) { row ->
                    CompositionLocalProvider(com.music.streamx.ui.components.LocalCardZoomShelf provides "Pinned") {
                        Row(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(top=4.dp,bottom=14.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            row.forEach { entry -> AppleMusicPinnedTile(entry,{
                                com.music.streamx.ui.components.CardZoom.open("Pinned",entry.browseId);onItem(entry,"Pinned")
                            },{onLongPress?.invoke(entry)},Modifier.weight(1f)) }
                            repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
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
            // Apple's Library ends its list with what was added lately, two to a row.
            val recent = shelves.firstOrNull { it.title == "Recently Added" }?.items.orEmpty()
            if(!editing && recent.isNotEmpty()) {
                item(key="recent-title") {
                    Text("Recently Added",Modifier.padding(start=20.dp,end=20.dp,top=28.dp,bottom=12.dp),
                        style=MaterialTheme.typography.titleLarge.copy(fontSize=22.sp,fontWeight=FontWeight.Bold,letterSpacing=0.sp))
                }
                items(recent.chunked(2),key={row->"recent-${row.first().browseId ?: row.first().videoId}"}) { row ->
                    // Each tile zooms into the page it opens, as Home's cards do.
                    CompositionLocalProvider(com.music.streamx.ui.components.LocalCardZoomShelf provides "Recently Added") {
                        Row(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(bottom=18.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                            row.forEach { entry -> AppleMusicRecentTile(entry,{
                                com.music.streamx.ui.components.CardZoom.open("Recently Added",entry.browseId);onItem(entry,"Recently Added")
                            },Modifier.weight(1f)) }
                            if(row.size==1) Spacer(Modifier.weight(1f))
                        }
                    }
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
            if (gridView && selected in listOf("Playlists","Albums","Artists")) items(entries.chunked(2)) { pair ->
                Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    pair.forEach { entry -> AppleMusicRecentTile(entry,{onItem(entry,selected!!)},Modifier.weight(1f)) }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            } else itemsIndexed(entries,key={i,it->"${it.browseId ?: it.videoId}-$i"}) { index,entry ->
                if(selected == "Playlists") AppleMusicPlaylistRow(entry,{onItem(entry,"Playlists")},{onLongPress?.invoke(entry)},showDivider=index<entries.lastIndex)
                else AppleMusicLibraryRow(entry,{onItem(entry,selected!!)}, {onLongPress?.invoke(entry)})
            }
            if(selected !in listOf("Playlists","Albums","Artists") && shelf?.moreBrowseId!=null) item { TextButton(onClick={onShowAll(shelf) },modifier=Modifier.padding(horizontal=12.dp)){Text("Show all")} }
            if(entries.isEmpty()) item { Text(if(selected=="Playlists" && query.isNotBlank())"No matching playlists" else "Nothing here yet",Modifier.padding(24.dp),color=c.onSurfaceVariant) }
            if(selected=="Downloaded") items(links+deviceItems) { entry -> AppleMusicLibraryRow(entry,{onItem(entry,"Downloaded")},{onLongPress?.invoke(entry)}) }
            // Room below a short list, so it still drags up under the frosted bar
            // and hands its title to it, as Apple's does, instead of not moving at all.
            item(key="scroll-room") { Spacer(Modifier.fillParentMaxHeight(.82f)) }
        }
    }
    val list = navigation.listState
    // A section shorter than the screen springs back to the top once let go, as
    // Apple's does; the room below it is only there to be dragged into.
    LaunchedEffect(list, selected, editing) {
        if (selected == null || editing) return@LaunchedEffect
        // Where the room below the rows sits with the list at rest, learnt as soon
        // as it's seen there; the way back is the distance from it.
        var roomRest = -1
        launch {
            snapshotFlow { list.layoutInfo }.collect { info ->
                if (list.firstVisibleItemIndex == 0 && list.firstVisibleItemScrollOffset == 0)
                    info.visibleItemsInfo.firstOrNull { it.key == "scroll-room" }?.let { roomRest = it.offset }
            }
        }
        snapshotFlow { list.isScrollInProgress }.collect { scrolling ->
            if (scrolling) return@collect
            val info = list.layoutInfo
            val room = info.visibleItemsInfo.firstOrNull { it.key == "scroll-room" } ?: return@collect
            val atTop = list.firstVisibleItemIndex == 0 && list.firstVisibleItemScrollOffset == 0
            if (atTop) { roomRest = room.offset; return@collect }
            // Only while the list's own rows end above the foot of the screen.
            if (roomRest < 0 || room.offset >= info.viewportEndOffset - 1) return@collect
            val back = (room.offset - roomRest).toFloat()
            // The fling settling can interrupt the spring as it starts; that is caught
            // here, or it would end this watcher, and the next stop springs it back.
            if (back < 0f) runCatching { list.animateScrollBy(back, androidx.compose.animation.core.spring(dampingRatio = .8f, stiffness = 300f)) }
        }
    }
    val scrolledUnder by remember { derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 0 } }
    val titleInBar by remember { derivedStateOf { list.firstVisibleItemIndex > 0 || list.firstVisibleItemScrollOffset > 90 } }
    val fadeAlpha by androidx.compose.animation.core.animateFloatAsState(if(scrolledUnder) 1f else 0f, label="libraryTopFade")
    val barTitleAlpha by androidx.compose.animation.core.animateFloatAsState(if(titleInBar) 1f else 0f, label="libraryBarTitle")
    // The bar's own material: solid behind the status bar and buttons, feathered just below,
    // so rows pass under it rather than being cut off at its edge.
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    // Frosted glass, as Apple's bar is: what scrolls under it blurs and darkens rather than vanishing.
    if(glassBackdrop != null) Box(Modifier.offset(y=-statusBar).fillMaxWidth().height(statusBar+LIBRARY_BAR_HEIGHT).graphicsLayer{alpha=fadeAlpha}
        .then(glassBackdrop.frost(c.background))
        .drawBehind{drawLine(c.onSurface.copy(alpha=.12f),androidx.compose.ui.geometry.Offset(0f,size.height),androidx.compose.ui.geometry.Offset(size.width,size.height),.5.dp.toPx())})
    else Box(Modifier.offset(y=-statusBar).fillMaxWidth().height(statusBar+LIBRARY_BAR_HEIGHT+20.dp).graphicsLayer{alpha=fadeAlpha}
        .background(Brush.verticalGradient(0f to c.background,(statusBar+LIBRARY_BAR_HEIGHT)/(statusBar+LIBRARY_BAR_HEIGHT+20.dp) to c.background.copy(alpha=.94f),1f to c.background.copy(alpha=0f))))
    // At the leading edge: the add, edit and account buttons take the trailing half of the bar.
    if(rootTitle) Text(pageTitle,Modifier.align(Alignment.TopStart).padding(start=20.dp,top=11.dp).height(64.dp).wrapContentHeight(),
        style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-.4).sp),maxLines=1)
    else Text(pageTitle,Modifier.align(Alignment.TopStart).padding(start=76.dp,top=11.dp).height(44.dp).wrapContentHeight().graphicsLayer{alpha=barTitleAlpha},
        style=MaterialTheme.typography.titleLarge.copy(fontWeight=FontWeight.Bold),maxLines=1)
    Column(Modifier.padding(top=11.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal=20.dp).heightIn(min=if(selected != null && !editing)44.dp else 64.dp), verticalAlignment=Alignment.CenterVertically) {
                if(selected != null && !editing) IconButton(onClick={selected=null},modifier=Modifier.liquidPress().size(44.dp).then(glassButton(CircleShape))) { Icon(Icons.Rounded.ArrowBack, "Library") }
                Spacer(Modifier.weight(1f))
                if(editing) {
                    IconButton(onClick=::finishEditing,modifier=Modifier.liquidPress().size(44.dp).clip(CircleShape).background(c.primary)) {
                        Icon(LibraryGlyphs.Check,"Done editing library",Modifier.size(25.dp),tint=Color.White)
                    }
                } else {
                if (rootTitle) Row(Modifier.liquidPress().then(glassButton(CircleShape))) {
                    IconButton(onClick={onNewPlaylist?.invoke()}, enabled=onNewPlaylist != null, modifier=Modifier.width(54.dp).height(44.dp)) {
                        Icon(LibraryGlyphs.Add, "New playlist", Modifier.size(25.dp))
                    }
                    IconButton(onClick={editing=true}, modifier=Modifier.width(54.dp).height(44.dp)) {
                        Icon(LibraryGlyphs.Edit,"Edit library",Modifier.size(25.dp))
                    }
                } else {
                    // A section's page: + and filter on one glass pill, ••• on its own, as Apple's.
                    Row(Modifier.liquidPress().then(glassButton(CircleShape))) {
                        if (selected == "Playlists") IconButton(onClick={onNewPlaylist?.invoke()}, enabled=onNewPlaylist != null, modifier=Modifier.width(54.dp).height(44.dp)) {
                            Icon(LibraryGlyphs.Plus, "New playlist", Modifier.size(24.dp))
                        }
                        IconButton(onClick={filterMenu=true}, modifier=Modifier.width(54.dp).height(44.dp)) {
                            Icon(LibraryGlyphs.Filter, "Filter", Modifier.size(24.dp), tint=if(listFilter!="All") c.primary else LocalContentColor.current)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick={viewMenu=true}, modifier=Modifier.liquidPress().size(44.dp).then(glassButton(CircleShape))) {
                        Icon(LibraryGlyphs.More, "View options", Modifier.size(24.dp))
                    }
                }
                // The account is the Library's alone; a section's page has no use for it.
                if(rootTitle) Spacer(Modifier.width(12.dp))
                if(rootTitle) Box(Modifier.liquidPress().size(44.dp).then(glassButton(CircleShape))
                    .semantics { contentDescription="Account" }.clickable(enabled=actions.onAccount!=null){actions.onAccount?.invoke()},contentAlignment=Alignment.Center) {
                    val photo=actions.account?.thumbnailUrl
                    if(photo!=null) AsyncImage(photo,"Account",Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                    else Text(actions.account?.name?.split(' ')?.filter{it.isNotBlank()}?.take(2)?.joinToString(""){it.take(1)}?.uppercase()?.takeIf{it.isNotBlank()} ?: "♪",
                        style=MaterialTheme.typography.titleMedium.copy(fontSize=17.sp,fontWeight=FontWeight.SemiBold))
                }
                }
            }
        }
    // Apple's ••• menu: how the section is laid out, then its order.
    if (viewMenu) GlassMenu(onDismiss={viewMenu=false}, anchor=MenuAnchor.recent(), frost={ glassBackdrop?.frost(c.surface) ?: Modifier.background(c.surface) }) {
        GlassMenuRow(LibraryGlyphs.Grid, "Grid", { gridView=true; prefs.edit().putBoolean("libraryGrid",true).apply(); viewMenu=false }, checked=gridView)
        GlassMenuRow(LibraryGlyphs.ListLines, "List", { gridView=false; prefs.edit().putBoolean("libraryGrid",false).apply(); viewMenu=false }, checked=!gridView)
        GlassMenuSeparator()
        // Only the orders the library can actually be put in.
        listOfNotNull("Title" to "Title", "Recent" to "Date Added", ("Artist" to "Artist").takeIf { selected == "Albums" || selected == "Songs" }).forEach { (key, label) ->
            GlassMenuRow(null, label, { sort=key; prefs.edit().putString("librarySort",key).apply(); viewMenu=false }, checked=sort==key)
        }
    }
    // Apple's filter: everything, or just what's starred or saved to the device.
    if (filterMenu) GlassMenu(onDismiss={filterMenu=false}, anchor=MenuAnchor.recent(), frost={ glassBackdrop?.frost(c.surface) ?: Modifier.background(c.surface) }) {
        GlassMenuRow(LibraryGlyphs.Playlist, "All ${selected ?: "Items"}", { listFilter="All"; filterMenu=false }, checked=listFilter=="All")
        GlassMenuSeparator()
        GlassMenuRow(LibraryGlyphs.Star, "Favourites", { listFilter="Favourites"; filterMenu=false }, checked=listFilter=="Favourites")
        GlassMenuRow(LibraryGlyphs.DownloadCircle, "Downloads", { listFilter="Downloads"; filterMenu=false }, checked=listFilter=="Downloads")
    }
    }
}

/** The pinned button row's height, which the list starts under. */
private val LIBRARY_BAR_HEIGHT = 75.dp

internal object LibraryGlyphs {
    private fun glyph(name:String, block: androidx.compose.ui.graphics.vector.PathBuilder.()->Unit): ImageVector =
        ImageVector.Builder(name,26.dp,26.dp,26f,26f).apply {
            path(stroke=SolidColor(Color.Black),strokeLineWidth=1.8f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round,pathBuilder=block)
        }.build()
    val Close = glyph("library_clear") { moveTo(6f,6f);lineTo(20f,20f);moveTo(20f,6f);lineTo(6f,20f) }
    val Plus = glyph("library_plus") { moveTo(13f,4f);lineTo(13f,22f);moveTo(4f,13f);lineTo(22f,13f) }
    val Filter = glyph("library_filter") { moveTo(4f,8f);lineTo(22f,8f);moveTo(7f,13f);lineTo(19f,13f);moveTo(10f,18f);lineTo(16f,18f) }
    val More = ImageVector.Builder("library_more",26.dp,26.dp,26f,26f).apply {
        for (x in listOf(6f,13f,20f)) path(fill=SolidColor(Color.Black)) { moveTo(x+2f,13f);arcToRelative(2f,2f,0f,true,true,-4f,0f);arcToRelative(2f,2f,0f,true,true,4f,0f) }
    }.build()
    val Grid = glyph("library_grid") { for ((x,y) in listOf(4f to 4f,15f to 4f,4f to 15f,15f to 15f)) { moveTo(x+2f,y);lineTo(x+5f,y);arcToRelative(2f,2f,0f,false,true,2f,2f);lineTo(x+7f,y+5f);arcToRelative(2f,2f,0f,false,true,-2f,2f);lineTo(x+2f,y+7f);arcToRelative(2f,2f,0f,false,true,-2f,-2f);lineTo(x,y+2f);arcToRelative(2f,2f,0f,false,true,2f,-2f) } }
    val ListLines = glyph("library_list") { for (y in listOf(7f,13f,19f)) { moveTo(9f,y);lineTo(23f,y);moveTo(3.5f,y);lineTo(4.5f,y) } }
    val Star = glyph("library_star") { moveTo(13f,3.5f);lineTo(15.9f,9.6f);lineTo(22.5f,10.4f);lineTo(17.6f,14.9f);lineTo(18.9f,21.5f);lineTo(13f,18.2f);lineTo(7.1f,21.5f);lineTo(8.4f,14.9f);lineTo(3.5f,10.4f);lineTo(10.1f,9.6f);close() }
    val DownloadCircle = glyph("library_download_circle") { moveTo(23f,13f);arcToRelative(10f,10f,0f,true,true,-20f,0f);arcToRelative(10f,10f,0f,true,true,20f,0f);moveTo(13f,7.5f);lineTo(13f,18f);moveTo(9f,14f);lineTo(13f,18f);lineTo(17f,14f) }
    val Playlist = glyph("library_playlist") { moveTo(3f,7f);lineTo(15f,7f);moveTo(3f,12f);lineTo(15f,12f);moveTo(3f,17f);lineTo(10f,17f);moveTo(19f,5f);lineTo(19f,17f);moveTo(19f,17f);arcToRelative(2.5f,2.5f,0f,true,true,-2.5f,-2.5f);lineTo(19f,14.5f);moveTo(19f,5f);lineTo(23f,6.5f) }
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
        // A person in a rounded square, Apple's mark for its Made for You mixes.
        glyph("library_made_for_you") { moveTo(7f,3f);lineTo(19f,3f);curveTo(21.5f,3f,23f,4.5f,23f,7f);lineTo(23f,19f);curveTo(23f,21.5f,21.5f,23f,19f,23f);lineTo(7f,23f);curveTo(4.5f,23f,3f,21.5f,3f,19f);lineTo(3f,7f);curveTo(3f,4.5f,4.5f,3f,7f,3f);close()
            moveTo(16.5f,10f);arcToRelative(3.5f,3.5f,0f,true,true,-7f,0f);arcToRelative(3.5f,3.5f,0f,true,true,7f,0f)
            moveTo(7f,20f);curveTo(8f,16.5f,10.5f,15f,13f,15f);curveTo(15.5f,15f,18f,16.5f,19f,20f) },
        glyph("library_downloaded") { moveTo(24f,13f);arcToRelative(11f,11f,0f,true,true,-22f,0f);arcToRelative(11f,11f,0f,true,true,22f,0f);moveTo(13f,6f);lineTo(13f,19f);moveTo(8f,14f);lineTo(13f,19f);lineTo(18f,14f) }
    )
}

@Composable internal fun AppleMusicFeedHeader(title: String,actions: SkinLibraryActions) {
    val c=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,top=11.dp,bottom=12.dp).heightIn(min=64.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-.4).sp))
        Box(Modifier.liquidPress().size(44.dp).clip(CircleShape).background(c.surface).border(.5.dp,c.onSurface.copy(alpha=.16f),CircleShape)
            .semantics {contentDescription="Account"}.clickable(enabled=actions.onAccount!=null){actions.onAccount?.invoke()},contentAlignment=Alignment.Center) {
            val photo=actions.account?.thumbnailUrl
            if(photo!=null) AsyncImage(photo,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
            else Text(actions.account?.name?.split(' ')?.filter{it.isNotBlank()}?.take(2)?.joinToString(""){it.take(1)}?.uppercase()?.takeIf{it.isNotBlank()} ?: "♪",
                style=MaterialTheme.typography.titleMedium.copy(fontSize=17.sp,fontWeight=FontWeight.SemiBold))
        }
    }
}

/** One Recently Added tile: square art, then a title and a grey line, as Apple lays them out. */
@Composable internal fun AppleMusicRecentTile(item: ShelfItem, onClick: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Column(modifier.clickable(onClick=onClick)) {
        Box(Modifier.cardZoomSource(item.browseId,8.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(c.surfaceVariant)) {
            if(!item.thumbnailUrl.isNullOrBlank()) AsyncImage(item.thumbnailUrl,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        }
        Text(item.title,Modifier.padding(top=7.dp),style=MaterialTheme.typography.bodyMedium.copy(fontSize=15.sp),maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        if(item.subtitle.isNotBlank()) Text(item.subtitle,style=MaterialTheme.typography.bodyMedium.copy(fontSize=15.sp),color=c.onSurfaceVariant,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

/** One pin: the art (round for an artist), the name under it, as Apple's Library shows its pins. */
@OptIn(ExperimentalFoundationApi::class)
@Composable private fun AppleMusicPinnedTile(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val artist = item.browseId?.contains(":artist:") == true
    val shape = if (artist) CircleShape else RoundedCornerShape(8.dp)
    Column(modifier.combinedClickable(onClick=onClick,onLongClick=onLongPress),horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.cardZoomSource(item.browseId,if(artist)60.dp else 8.dp).fillMaxWidth().aspectRatio(1f).clip(shape).background(c.surfaceVariant)) {
            if(!item.thumbnailUrl.isNullOrBlank()) AsyncImage(item.thumbnailUrl,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        }
        Text(item.title,Modifier.padding(top=6.dp),style=MaterialTheme.typography.bodySmall.copy(fontSize=13.sp),maxLines=1,
            overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}
