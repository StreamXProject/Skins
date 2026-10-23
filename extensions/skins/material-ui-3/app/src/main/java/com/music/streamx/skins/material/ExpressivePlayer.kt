package com.music.streamx.skins.material
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import com.music.streamx.ui.theme.StatusBarIcons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.data.lyrics.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.skins.*
import com.music.streamx.ui.player.PlayerPlatform
import com.music.streamx.ui.player.RepeatModes
import com.music.streamx.ui.haptics.rememberHaptics
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun ExpressivePlayer(
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
    StatusBarIcons(dark=MaterialTheme.colorScheme.surface.luminance()>.5f)
    val locked=rememberSkinControlsLocked()
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val artwork=rememberSkinRemoteArtwork(song)
    val shownDuration=durationMs.takeIf{it>0} ?: durationFromText(song.durationText)
    var page by rememberSaveable {mutableStateOf("player")}
    var queueOpen by remember {mutableStateOf(false)}
    var providersOpen by remember {mutableStateOf(false)}
    var outputOpen by remember {mutableStateOf(false)}
    var pipelineOpen by remember {mutableStateOf(false)}
    var castOpen by remember {mutableStateOf(false)}
    var clearConfirm by remember {mutableStateOf(false)}
    val haze=remember{HazeState()}
    val openOutput=rememberSkinOutputPicker {outputOpen=true}
    SkinBackHandler(page!="player"){page="player"}
    SkinBackHandler(queueOpen){queueOpen=false}
    val translation=rememberSkinLyricsTranslation(song.videoId,lyrics,lyricsSource,lyricsUnavailable,"Loading lyrics…",rememberHaptics())
    fun guarded(action: ()->Unit) {if(locked)onBlocked() else action()}
    Surface(modifier.fillMaxSize(),color=MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            TopAppBar(title={Column {Text(if(page=="lyrics")"Lyrics" else "Now playing",style=MaterialTheme.typography.titleMedium);Text(song.playbackSource ?: "Your queue",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}},
                navigationIcon={IconButton(onClick={if(page=="lyrics")page="player" else onCollapse()}){Icon(ExpressiveSymbols.KeyboardArrowDown,if(page=="lyrics")"Back to player" else "Collapse player")}},
                actions={IconButton(onClick=onOpenMenu){Icon(ExpressiveSymbols.MoreVert,"Track options")}})
            if(page=="lyrics") {
                ExpressiveLyrics(lyrics,translation.subLines,lyricsUnavailable,position,{guarded{onSeek(skinLyricsSeek(it,skinPlayerSettings().lyricsOffsetMs.value))}},reduce,Modifier.weight(1f))
                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                    TextButton(onClick=translation.toggleTranslation,enabled=!lyrics.isNullOrEmpty()){Text(if(translation.showingTranslation)"Original" else "Translate")}
                    TextButton(onClick=translation.toggleRomanization,enabled=!lyrics.isNullOrEmpty()){Text("Romanize")}
                    IconButton(onClick={providersOpen=true}){Icon(ExpressiveSymbols.Tune,"Lyrics providers")}
                }
                ExpressiveSeek(position,shownDuration,locked || durationMs<=0,{guarded{onSeekFraction(it)}},Modifier.padding(horizontal=24.dp))
                ExpressivePlayButton(isPlaying,isLoading,reduce,{guarded(onPlayPause)},Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
            } else BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val wide=maxWidth>=700.dp
                @Composable fun Artwork(modifier: Modifier){Box(modifier.clip(RoundedCornerShape(32.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh)){AsyncImage(artwork,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop);com.music.streamx.ui.skins.SkinArtworkStats(Modifier.align(Alignment.BottomCenter).padding(10.dp))}}
                @Composable fun Controls(){Column(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {Text(song.title,style=MaterialTheme.typography.headlineMedium,maxLines=3,overflow=TextOverflow.Ellipsis);Text(song.artist,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).clickable(role=androidx.compose.ui.semantics.Role.Button){onOpenArtist(song.artistId,song.artist)}.padding(vertical=8.dp),color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)}
                        FilledTonalIconButton(onClick=onToggleLike){Icon(if(likeStatus==LikeStatus.LIKE)ExpressiveSymbols.Favorite else ExpressiveSymbols.FavoriteBorder,if(likeStatus==LikeStatus.LIKE)"Remove from favourites" else "Add to favourites")}
                    }
                    ExpressiveSeek(position,shownDuration,locked || durationMs<=0,{guarded{onSeekFraction(it)}})
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                        IconToggleButton(checked=shuffleEnabled,onCheckedChange={guarded(onToggleShuffle)}){Icon(ExpressiveSymbols.Shuffle,"Shuffle")}
                        IconButton(onClick={guarded(onPrevious)},enabled=hasPrevious && !locked){Icon(ExpressiveSymbols.SkipPrevious,"Previous",Modifier.size(32.dp))}
                        ExpressivePlayButton(isPlaying,isLoading,reduce,{guarded(onPlayPause)})
                        IconButton(onClick={guarded(onNext)},enabled=hasNext && !locked){Icon(ExpressiveSymbols.SkipNext,"Next",Modifier.size(32.dp))}
                        IconToggleButton(checked=repeatMode!=RepeatModes.OFF,onCheckedChange={guarded(onCycleRepeat)}){Icon(if(repeatMode==RepeatModes.ONE)ExpressiveSymbols.RepeatOne else ExpressiveSymbols.Repeat,"Repeat mode")}
                    }
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        IconButton(onClick=openOutput){Icon(ExpressiveSymbols.Devices,"Audio output")}
                        IconButton(onClick={page="lyrics"}){Icon(ExpressiveSymbols.Lyrics,"Lyrics")}
                        IconButton(onClick=onListenTogether){Icon(ExpressiveSymbols.Groups,"Listen together")}
                        IconButton(onClick=onOpenTimer){Icon(ExpressiveSymbols.Bedtime,"Sleep timer")}
                        IconButton(onClick={queueOpen=true}){Icon(ExpressiveSymbols.QueueMusic,"Queue")}
                    }
                    TextButton(onClick=onOpenPlaybackSource){Text(song.playbackSource ?: "Playback source")}
                }}
                if(wide)Row(Modifier.fillMaxSize().padding(horizontal=32.dp),verticalAlignment=Alignment.CenterVertically) {
                    Artwork(Modifier.weight(1f).aspectRatio(1f))
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){Controls()}
                } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
                    Box(Modifier.widthIn(max=440.dp).fillMaxWidth().padding(horizontal=24.dp,vertical=16.dp)){Artwork(Modifier.fillMaxWidth().aspectRatio(1f))}
                    Controls()
                }
            }
        }
    }
    if(queueOpen)ExpressiveQueueSheet(song,queue,queueIndex,autoplayEnabled,shuffleEnabled,isPlaying,isLoading,locked,reduce,windowHeight,onJumpTo,onRemove,onMove,onDragActive,{clearConfirm=true},onToggleAutoplay,onToggleShuffle,onOpenTimer,{guarded(onPlayPause)},{queueOpen=false})
    if(clearConfirm)AlertDialog(onDismissRequest={clearConfirm=false},title={Text("Clear the queue?")},text={Text("Remove the queued tracks. This does not delete music from your library.")},confirmButton={TextButton(onClick={clearConfirm=false;guarded(onClear)}){Text("Clear queue")}},dismissButton={TextButton(onClick={clearConfirm=false}){Text("Cancel")}})
    if(outputOpen)ExpressiveAudioOutput({outputOpen=false},{outputOpen=false;pipelineOpen=true},{outputOpen=false;castOpen=true})
    if(castOpen)PlayerPlatform.host.CastDialog(haze){castOpen=false}
    if(pipelineOpen)SkinAudioPipeline(haze,isPlaying,{pipelineOpen=false})
    if(providersOpen)ExpressiveLyricsProviders(lyricsSource,lyricsProviderStates,onSelectLyricsProvider,{providersOpen=false})
    if(lyricsOffsetOpen)ExpressiveLyricsOffset(onDismissLyricsOffset)
}

@Composable internal fun ExpressivePlayButton(playing: Boolean,loading: Boolean,reduce: Boolean,onClick: ()->Unit,modifier: Modifier=Modifier) {
    val corner by animateFloatAsState(if(playing)28f else 40f,animationSpec=if(reduce)snap() else spring(dampingRatio=.75f,stiffness=500f),label="playShape")
    FilledIconButton(onClick=onClick,modifier=modifier.size(80.dp),shape=RoundedCornerShape(corner.dp)) {
        if(loading)CircularProgressIndicator(Modifier.size(32.dp),color=MaterialTheme.colorScheme.onPrimary,strokeWidth=3.dp)
        else Icon(if(playing)ExpressiveSymbols.Pause else ExpressiveSymbols.PlayArrow,if(playing)"Pause" else "Play",Modifier.size(40.dp))
    }
}
private fun time(ms: Long): String {val seconds=ms.coerceAtLeast(0)/1000;return "${seconds/60}:${(seconds%60).toString().padStart(2,'0')}"}
@Composable private fun ExpressiveSeek(position: PlaybackPosition,duration: Long,locked: Boolean,onSeek: (Float)->Unit,modifier: Modifier=Modifier) {
    var dragging by remember {mutableStateOf<Float?>(null)}
    val fraction=if(duration>0)(position.positionMs.toFloat()/duration).coerceIn(0f,1f) else 0f
    Column(modifier.fillMaxWidth()) {
        Slider(value=dragging ?: fraction,onValueChange={dragging=it},onValueChangeFinished={dragging?.let(onSeek);dragging=null},enabled=duration>0 && !locked,modifier=Modifier.semantics{contentDescription="Playback position"})
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(time(if(dragging!=null)(dragging!!*duration).toLong() else position.positionMs),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(if(duration>0)time(duration) else "—:—",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}
@Composable private fun ExpressiveLyrics(lines: List<LyricLine>?,subLines: List<LyricLine>?,unavailable: Boolean,position: PlaybackPosition,onSeek: (Long)->Unit,reduce: Boolean,modifier: Modifier) {
    if(lines.isNullOrEmpty()){Box(modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Text(if(unavailable)"Lyrics aren’t available for this track" else "Loading lyrics…",Modifier.padding(32.dp))};return}
    val list=rememberLazyListState()
    val current=lines.indexOfLast{it.timeMs<=skinLyricsPosition(position.positionMs,skinPlayerSettings().lyricsOffsetMs.value)}.coerceAtLeast(0)
    LaunchedEffect(current){if(!list.isScrollInProgress){if(reduce)list.scrollToItem(current) else list.animateScrollToItem(current)}}
    LazyColumn(modifier.fillMaxWidth(),state=list,contentPadding=PaddingValues(horizontal=24.dp,vertical=32.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        itemsIndexed(lines){index,line->Column(Modifier.fillMaxWidth().clickable{onSeek(line.timeMs)}.padding(vertical=8.dp).semantics{if(index==current)stateDescription="Current lyric"}) {
            Text(line.text.ifEmpty{"♪"},style=MaterialTheme.typography.headlineMedium,fontWeight=if(index==current)FontWeight.Bold else FontWeight.Normal,color=if(index==current)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            subLines?.getOrNull(index)?.let {Text(it.text,style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        }}
    }
}
@Composable internal fun ExpressiveMiniPlayer(song: Song,playing: Boolean,loading: Boolean,position: PlaybackPosition,durationMs: Long,saved: Boolean,onSave: ()->Unit,onPlayPause: ()->Unit,onExpand: ()->Unit,onNext: ()->Unit,onPrevious: ()->Unit,locked: Boolean,onBlocked: ()->Unit,modifier: Modifier) {
    val artwork=rememberSkinRemoteArtwork(song)
    Surface(modifier.padding(horizontal=12.dp,vertical=8.dp).fillMaxWidth().clickable(onClick=onExpand),shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column {
            Row(Modifier.fillMaxWidth().padding(start=12.dp,top=8.dp,bottom=8.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically) {
                AsyncImage(artwork,null,Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),contentScale=ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleSmall,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,style=MaterialTheme.typography.bodySmall,maxLines=1,overflow=TextOverflow.Ellipsis)}
                IconButton(onClick=onSave){Icon(if(saved)ExpressiveSymbols.Favorite else ExpressiveSymbols.FavoriteBorder,if(saved)"Remove from favourites" else "Add to favourites")}
                FilledTonalIconButton(onClick={if(locked)onBlocked() else onPlayPause()}){if(loading)CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Icon(if(playing)ExpressiveSymbols.Pause else ExpressiveSymbols.PlayArrow,if(playing)"Pause" else "Play")}
                IconButton(onClick={if(locked)onBlocked() else onNext()}){Icon(ExpressiveSymbols.SkipNext,"Next")}
            }
            LinearProgressIndicator(progress={if(durationMs>0)(position.positionMs.toFloat()/durationMs).coerceIn(0f,1f) else 0f},modifier=Modifier.fillMaxWidth().height(3.dp),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.surfaceContainerHighest)
        }
    }
}

internal fun durationFromText(text: String?): Long {
    val parts=text?.trim()?.split(":") ?: return 0
    if(parts.size !in 2..3)return 0
    val values=parts.map{it.toLongOrNull() ?: return 0}
    if(values.any{it<0} || values.drop(1).any{it>=60})return 0
    return values.fold(0L){sum,n->if(sum>Long.MAX_VALUE/60)return 0 else sum*60+n}.let{if(it>Long.MAX_VALUE/1000)0 else it*1000}
}
