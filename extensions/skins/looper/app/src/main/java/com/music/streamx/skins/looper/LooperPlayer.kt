package com.music.streamx.skins.looper
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.data.lyrics.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.skins.*
import com.music.streamx.ui.haptics.rememberHaptics
import com.music.streamx.ui.player.*
import com.music.streamx.ui.theme.StatusBarIcons
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun LooperPlayer(
    song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long,
    queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean,
    repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus,
    accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit,
    onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit,
    onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
    onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit,
    onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit,
    onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?,
    lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>,
    onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean,
    onDismissLyricsOffset: () -> Unit, modifier: Modifier = Modifier,
) {
    StatusBarIcons(false)
    val options=looperOptions()
    val locked=rememberSkinControlsLocked()
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val artwork=rememberSkinRemoteArtwork(song)
    val palette=playerColors(rememberSkinArtworkColor(artwork)?.toArgb())
    val top by animateColorAsState(palette.top,tween(if(reduce)0 else 600),label="Cover color")
    val bottom by animateColorAsState(palette.bottom,tween(if(reduce)0 else 600),label="Cover shade")
    val trackFormat = rememberSkinTrackFormat()
    val duration=durationMs.takeIf{it>0} ?: durationFromText(song.durationText)
    var lyricsPage by rememberSaveable{mutableStateOf(false)}
    var queueOpen by remember{mutableStateOf(false)}
    var menu by remember{mutableStateOf(false)}
    var output by remember{mutableStateOf(false)}
    var pipeline by remember{mutableStateOf(false)}
    var cast by remember{mutableStateOf(false)}
    var providers by remember{mutableStateOf(false)}
    var offset by remember{mutableStateOf(false)}
    var clearing by remember{mutableStateOf(false)}
    val haze=remember{HazeState()}
    val openOutput=rememberSkinOutputPicker{output=true}
    val translated=rememberSkinLyricsTranslation(song.videoId,lyrics,lyricsSource,lyricsUnavailable,"Loading lyrics…",rememberHaptics())
    fun action(block: ()->Unit){if(locked)onBlocked()else block()}
    SkinBackHandler(lyricsPage){lyricsPage=false}
    SkinBackHandler(queueOpen){queueOpen=false}
    val colors=MaterialTheme.colorScheme.copy(primary=palette.accent,onPrimary=Color.Black,onSurface=Color.White,onSurfaceVariant=Color.White.copy(alpha=.45f),surface=palette.bottom,surfaceContainerHigh=palette.top)
    MaterialTheme(colorScheme=colors){Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top,bottom)))){
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                if(lyricsPage){
                    AsyncImage(artwork,null,Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),contentScale=ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,style=MaterialTheme.typography.bodyLarge,color=colors.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
                }else {
                    IconButton(onClick=onCollapse){Icon(LooperIcons.Down,"Collapse player",Modifier.size(18.dp))}
                    LooperTile(openOutput,Modifier.weight(1f),color=Color.White.copy(alpha=.08f)){Text(trackFormat.takeIf{it.isNotBlank()} ?: song.playbackSource ?: "Now playing",Modifier.padding(horizontal=10.dp,vertical=8.dp),style=MaterialTheme.typography.labelSmall,maxLines=1,overflow=TextOverflow.Ellipsis)}
                }
                Box {
                    IconButton(onClick={menu=true}){Icon(LooperIcons.More,"Player options",Modifier.size(20.dp))}
                    DropdownMenu(menu,{menu=false}){
                        DropdownMenuItem(text={Text("Track options")},onClick={menu=false;onOpenMenu()})
                        DropdownMenuItem(text={Text("Audio output")},onClick={menu=false;openOutput()})
                        DropdownMenuItem(text={Text("Sleep timer")},onClick={menu=false;onOpenTimer()})
                        DropdownMenuItem(text={Text("Listen together")},onClick={menu=false;action(onListenTogether)})
                        if(lyricsPage){
                            DropdownMenuItem(text={Text(if(translated.showingTranslation)"Original lyrics"else"Translate lyrics")},onClick={menu=false;translated.toggleTranslation()},enabled=!lyrics.isNullOrEmpty())
                            DropdownMenuItem(text={Text("Romanize lyrics")},onClick={menu=false;translated.toggleRomanization()},enabled=!lyrics.isNullOrEmpty())
                            DropdownMenuItem(text={Text("Lyrics provider")},onClick={menu=false;providers=true})
                            DropdownMenuItem(text={Text("Lyrics timing")},onClick={menu=false;offset=true})
                        }
                    }
                }
                if(lyricsPage)IconButton(onClick={lyricsPage=false}){Icon(LooperIcons.Down,"Back to player",Modifier.size(18.dp))}
            }
            @Composable fun Timeline(){LooperTimeline(position,duration,locked || durationMs<=0,{action{onSeekFraction(it)}})}
            @Composable fun Transport(){LooperTransport(isPlaying,isLoading,hasPrevious,hasNext,locked,options.segmented,{action(onPrevious)},{action(onPlayPause)},{action(onNext)})}
            if(lyricsPage){
                LooperLyrics(lyrics,translated.subLines,lyricsUnavailable,position,reduce,{action{onSeek(skinLyricsSeek(it,skinPlayerSettings().lyricsOffsetMs.value))}},Modifier.weight(1f))
                Column(Modifier.padding(horizontal=28.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(28.dp)){Timeline();Transport()}
            }else BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)){
                @Composable fun Cover(modifier: Modifier){Box(modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=.04f)).clickable{lyricsPage=true}){AsyncImage(artwork,"Open lyrics",Modifier.fillMaxSize(),contentScale=ContentScale.Crop);com.music.streamx.ui.skins.SkinArtworkStats(Modifier.align(Alignment.BottomCenter).padding(10.dp))}}
                @Composable fun Controls(){Column(Modifier.fillMaxWidth().padding(horizontal=24.dp).padding(top=20.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(24.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f).padding(horizontal=8.dp)){
                        Text(song.title,style=MaterialTheme.typography.headlineLarge,maxLines=2,overflow=TextOverflow.Ellipsis)
                        Text(song.artist,Modifier.clip(Capsule).clickable{onOpenArtist(song.artistId,song.artist)}.padding(vertical=4.dp),style=MaterialTheme.typography.titleLarge,color=colors.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)
                    };LooperTile(onToggleLike,Modifier.size(56.dp),CircleShape,Color.White.copy(alpha=.04f)){Icon(LooperIcons.Favorite,if(likeStatus==LikeStatus.LIKE)"Remove from favourites"else"Add to favourites",Modifier.size(24.dp),tint=if(likeStatus==LikeStatus.LIKE)palette.accent else Color.White)}}
                    Timeline();Transport()
                    LooperUtilityRow(shuffleEnabled,repeatMode,{action(onToggleShuffle)},{action(onCycleRepeat)},{queueOpen=true},{lyricsPage=true})

                }}
                when {
                    maxWidth>=700.dp->Row(Modifier.fillMaxSize().padding(24.dp),verticalAlignment=Alignment.CenterVertically){Cover(Modifier.weight(1f).aspectRatio(1f));Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){Controls()}}
                    maxHeight<650.dp->Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally){Cover(Modifier.padding(20.dp).widthIn(max=320.dp).fillMaxWidth().aspectRatio(1f));Controls()}
                    else->Column(Modifier.fillMaxSize()) {BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal=20.dp,vertical=16.dp),contentAlignment=Alignment.Center){Cover(Modifier.size(minOf(maxWidth,maxHeight,480.dp)))};Controls()}
                }
            }
        }
    }}
    if(queueOpen)LooperQueue(song,queue,queueIndex,isPlaying,isLoading,locked,reduce,onJumpTo,onRemove,onMove,onDragActive,{clearing=true},{action(onPlayPause)},{queueOpen=false})
    if(clearing)AlertDialog(shape=DialogShape,onDismissRequest={clearing=false},title={Text("Clear play queue?")},confirmButton={TextButton(onClick={clearing=false;action(onClear)}){Text("Clear")}},dismissButton={TextButton(onClick={clearing=false}){Text("Cancel")}})
    if(output)LooperAudioOutput({output=false},{output=false;pipeline=true},{output=false;cast=true})
    if(pipeline)SkinAudioPipeline(haze,isPlaying,{pipeline=false})
    if(cast)PlayerPlatform.host.CastDialog(haze){cast=false}
    if(providers)LooperLyricsProviders(lyricsSource,lyricsProviderStates,onSelectLyricsProvider,{providers=false})
    if(offset || lyricsOffsetOpen)LooperLyricsOffset{offset=false;onDismissLyricsOffset()}
}

internal fun transportShape(index: Int,segmented: Boolean): Shape=if(!segmented)Capsule else when(index){0->RoundedCornerShape(topStart=40.dp,bottomStart=40.dp,topEnd=12.dp,bottomEnd=12.dp);2->RoundedCornerShape(topStart=12.dp,bottomStart=12.dp,topEnd=40.dp,bottomEnd=40.dp);else->RoundedCornerShape(12.dp)}
@Composable private fun LooperTransport(playing: Boolean,loading: Boolean,previous: Boolean,next: Boolean,locked: Boolean,segmented: Boolean,onPrevious: ()->Unit,onPlay: ()->Unit,onNext: ()->Unit) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        listOf(Triple(LooperIcons.Previous,"Previous",onPrevious),Triple(if(playing)LooperIcons.Pause else LooperIcons.Play,if(playing)"Pause"else"Play",onPlay),Triple(LooperIcons.Next,"Next",onNext)).forEachIndexed{i,(icon,label,click)->
            val enabled=!locked && (i==1 || if(i==0)previous else next)
            LooperTile({if(enabled)click()},Modifier.weight(1f).height(80.dp).semantics{if(!enabled)disabled()},transportShape(i,segmented),if(i==1 && !playing)MaterialTheme.colorScheme.primary else Color.White.copy(alpha=.04f),bordered=false){
                if(i==1 && loading)CircularProgressIndicator(Modifier.size(22.dp),strokeWidth=2.dp,color=Color.White)else Icon(icon,label,Modifier.size(26.dp),tint=if(!enabled)Color.White.copy(alpha=.25f)else if(i==1 && !playing)Color.Black else Color.White)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun LooperTimeline(position: PlaybackPosition,duration: Long,disabled: Boolean,onSeek: (Float)->Unit){
    var draft by remember{mutableStateOf<Float?>(null)}
    Column {
        Slider(value=draft ?: if(duration>0)(position.positionMs.toFloat()/duration).coerceIn(0f,1f)else 0f,onValueChange={draft=it},onValueChangeFinished={draft?.let(onSeek);draft=null},enabled=duration>0 && !disabled,modifier=Modifier.height(28.dp).semantics{contentDescription="Playback position"},thumb={Box(Modifier.size(4.dp,16.dp).background(MaterialTheme.colorScheme.primary,Capsule))},track={s->Box(Modifier.fillMaxWidth().height(12.dp).clip(Capsule).background(Color.White.copy(alpha=.08f))){Box(Modifier.fillMaxHeight().fillMaxWidth(s.value).background(MaterialTheme.colorScheme.primary))}})
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(clock(draft?.let{(it*duration).toLong()} ?: position.positionMs),style=MaterialTheme.typography.bodyMedium);Text(if(duration>0)clock(duration)else"—:—",style=MaterialTheme.typography.bodyMedium)}
    }
}
@Composable private fun LooperLyrics(lines: List<LyricLine>?,subLines: List<LyricLine>?,unavailable: Boolean,position: PlaybackPosition,reduce: Boolean,onSeek: (Long)->Unit,modifier: Modifier){
    if(lines.isNullOrEmpty()){Box(modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Text(if(unavailable)"No lyrics available"else"Loading lyrics…")};return}
    val state=rememberLazyListState();val active=lines.indexOfLast{it.timeMs<=skinLyricsPosition(position.positionMs,skinPlayerSettings().lyricsOffsetMs.value)}.coerceAtLeast(0)
    LaunchedEffect(active){if(!state.isScrollInProgress){if(reduce)state.scrollToItem(active)else state.animateScrollToItem(active)}}
    // Mask only the lyric viewport so the actual artwork gradient shows through.
    // Extra bottom inset lets the final lyric scroll above the fading region.
    LazyColumn(modifier.fillMaxWidth().graphicsLayer{compositingStrategy=CompositingStrategy.Offscreen}.drawWithCache {
        val height=size.height.coerceAtLeast(1f)
        val fadeHeight=minOf(88.dp.toPx(),height*.3f)
        val mask=Brush.verticalGradient(0f to Color.White,((height-fadeHeight)/height) to Color.White,1f to Color.Transparent)
        onDrawWithContent {drawContent();drawRect(mask,blendMode=BlendMode.DstIn)}
    },state=state,contentPadding=PaddingValues(start=24.dp,top=24.dp,end=24.dp,bottom=112.dp),verticalArrangement=Arrangement.spacedBy(24.dp)){
        itemsIndexed(lines){index,line->Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable{onSeek(line.timeMs)}.padding(vertical=8.dp).semantics{if(index==active)stateDescription="Current lyric"}){Text(line.text.ifBlank{"♪"},style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold,color=if(index==active)MaterialTheme.colorScheme.primary else Color.White.copy(alpha=.3f));subLines?.getOrNull(index)?.let{Text(it.text,color=Dim,style=MaterialTheme.typography.bodyLarge)}}
        }
    }
}
@Composable internal fun LooperMiniPlayer(song: Song,playing: Boolean,loading: Boolean,position: PlaybackPosition,durationMs: Long,saved: Boolean,onSave: ()->Unit,onPlayPause: ()->Unit,onExpand: ()->Unit,onNext: ()->Unit,onPrevious: ()->Unit,locked: Boolean,onBlocked: ()->Unit,modifier: Modifier){
    val artwork=rememberSkinRemoteArtwork(song)
    Row(modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=3.dp).height(64.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
        LooperTile(onExpand,Modifier.weight(1f).fillMaxHeight(),endShape(true)){AsyncImage(artwork,null,Modifier.padding(start=10.dp).size(50.dp).clip(CircleShape),contentScale=ContentScale.Crop);Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,style=MaterialTheme.typography.bodyLarge,color=Dim,maxLines=1,overflow=TextOverflow.Ellipsis)}}
        LooperTile({if(locked)onBlocked()else onPlayPause()},Modifier.width(72.dp).fillMaxHeight(),endShape(false)){if(loading)CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp)else Icon(if(playing)LooperIcons.Pause else LooperIcons.Play,if(playing)"Pause"else"Play",Modifier.size(24.dp))}
    }
}
internal fun clock(ms: Long): String {val seconds=ms.coerceAtLeast(0)/1000;return "${seconds/60}:${(seconds%60).toString().padStart(2,'0')}"}
internal fun durationFromText(value: String?): Long {
    val tokens=value?.trim()?.split(':') ?: return 0L
    if(tokens.size !in 2..3)return 0L
    val parts=tokens.map{it.toLongOrNull() ?: return 0L}
    if(parts.any{it<0} || parts.drop(1).any{it>=60})return 0L
    return parts.fold(0L){total,n->if(total>(Long.MAX_VALUE-n)/60)return 0L else total*60+n}.let{if(it>Long.MAX_VALUE/1000)0L else it*1000}
}

/** The two padded capsule groups from Looper's buildUtilityRow. */
@Composable private fun LooperUtilityRow(shuffle: Boolean,repeatMode: Int,onShuffle: ()->Unit,onRepeat: ()->Unit,onQueue: ()->Unit,onLyrics: ()->Unit){
    BoxWithConstraints(Modifier.fillMaxWidth()){
        val labels=maxWidth>=350.dp
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Row(Modifier.clip(Capsule).background(Color.White.copy(alpha=.04f)).padding(6.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                LooperTile(onShuffle,Modifier.heightIn(min=40.dp).width(56.dp),Capsule,if(shuffle)Color.White.copy(alpha=.06f)else Color.Transparent,bordered=false){Icon(LooperIcons.Shuffle,"Shuffle",Modifier.size(16.dp),tint=Color.White.copy(alpha=if(shuffle)1f else .5f))}
                LooperTile(onRepeat,Modifier.heightIn(min=40.dp).width(56.dp),Capsule,if(repeatMode!=RepeatModes.OFF)Color.White.copy(alpha=.06f)else Color.Transparent,bordered=false){Icon(if(repeatMode==RepeatModes.ONE)LooperIcons.RepeatOne else LooperIcons.Repeat,"Repeat mode",Modifier.size(20.dp),tint=Color.White.copy(alpha=if(repeatMode!=RepeatModes.OFF)1f else .5f))}
            }
            Spacer(Modifier.width(2.dp))
            Row(Modifier.clip(Capsule).background(Color.White.copy(alpha=.04f)).padding(6.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                LooperTile(onQueue,Modifier.heightIn(min=40.dp),Capsule,Color.White.copy(alpha=.06f),bordered=false){Row(Modifier.padding(horizontal=if(labels)16.dp else 12.dp),verticalAlignment=Alignment.CenterVertically){Icon(LooperIcons.Queue,"Next Up",Modifier.size(16.dp),tint=Color.White);if(labels){Spacer(Modifier.width(8.dp));Text("Next Up",style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Medium,color=Color.White)}}}
                LooperTile(onLyrics,Modifier.heightIn(min=40.dp),Capsule,Color.White.copy(alpha=.06f),bordered=false){Row(Modifier.padding(horizontal=if(labels)16.dp else 12.dp),verticalAlignment=Alignment.CenterVertically){Icon(LooperIcons.Lyrics,"Lyrics",Modifier.size(16.dp),tint=Color.White);if(labels){Spacer(Modifier.width(8.dp));Text("Lyrics",style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Medium,color=Color.White)}}}
            }
        }
    }
}
