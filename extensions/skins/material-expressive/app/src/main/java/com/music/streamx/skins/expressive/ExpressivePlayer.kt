package com.music.streamx.skins.expressive
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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

@OptIn(ExperimentalMaterial3Api::class,ExperimentalMaterial3ExpressiveApi::class)
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
            TopAppBar(title={Column(Modifier.heightIn(min=48.dp).clickable(onClick=onOpenPlaybackSource)) {Text(if(page=="lyrics")"Lyrics" else "Now playing",style=MaterialTheme.typography.titleMedium);Text(song.playbackSource ?: "Your queue",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}},
                navigationIcon={IconButton(onClick={if(page=="lyrics")page="player" else onCollapse()}){Icon(ExpressiveSymbols.KeyboardArrowDown,if(page=="lyrics")"Back to player" else "Collapse player")}},
                actions={IconButton(onClick=onOpenMenu){Icon(ExpressiveSymbols.MoreVert,"Track options")}})
            if(page=="lyrics") {
                ExpressiveLyrics(lyrics,translation.subLines,lyricsUnavailable,position,{guarded{onSeek(skinLyricsSeek(it,skinPlayerSettings().lyricsOffsetMs.value))}},reduce,Modifier.weight(1f))
                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                    FilledTonalButton(onClick=translation.toggleTranslation,enabled=!lyrics.isNullOrEmpty()){Text(if(translation.showingTranslation)"Original" else "Translate")}
                    FilledTonalButton(onClick=translation.toggleRomanization,enabled=!lyrics.isNullOrEmpty()){Text("Romanize")}
                    IconButton(onClick={providersOpen=true}){Icon(ExpressiveSymbols.Tune,"Lyrics providers")}
                }
                ExpressiveSeek(position,shownDuration,locked || durationMs<=0,isPlaying,reduce,{guarded{onSeekFraction(it)}},Modifier.padding(horizontal=24.dp))
                ExpressivePlayButton(isPlaying,isLoading,reduce,{guarded(onPlayPause)},Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
            } else BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val wide=maxWidth>=700.dp
                val compactArtwork=(maxHeight*.40f).coerceIn(180.dp,360.dp)
                @Composable fun Artwork(modifier: Modifier){Box(modifier.clip(RoundedCornerShape(36.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh)){AsyncImage(artwork,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop);com.music.streamx.ui.skins.SkinArtworkStats(Modifier.align(Alignment.BottomCenter).padding(10.dp))}}
                @Composable fun Controls(){Column(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {Text(song.title,style=MaterialTheme.typography.headlineLarge.copy(fontWeight=FontWeight.Bold),maxLines=2,overflow=TextOverflow.Ellipsis);Text(song.artist,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).clickable(role=androidx.compose.ui.semantics.Role.Button){onOpenArtist(song.artistId,song.artist)}.padding(vertical=8.dp),color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)}
                        FilledTonalIconButton(onClick=onToggleLike){Icon(if(likeStatus==LikeStatus.LIKE)ExpressiveSymbols.Favorite else ExpressiveSymbols.FavoriteBorder,if(likeStatus==LikeStatus.LIKE)"Remove from favourites" else "Add to favourites")}
                    }
                    ExpressiveSeek(position,shownDuration,locked || durationMs<=0,isPlaying,reduce,{guarded{onSeekFraction(it)}})
                    ExpressiveTransport(isPlaying,isLoading,hasPrevious,hasNext,locked,reduce,
                        {guarded(onPrevious)},{guarded(onPlayPause)},{guarded(onNext)})
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
                        IconToggleButton(checked=shuffleEnabled,onCheckedChange={guarded(onToggleShuffle)}){Icon(ExpressiveSymbols.Shuffle,"Shuffle")}
                        FilledTonalIconButton(onClick=openOutput){Icon(ExpressiveSymbols.Devices,"Audio output")}
                        FilledTonalIconButton(onClick={guarded(onListenTogether)}){Icon(ExpressiveSymbols.Groups,"Listen together")}
                        FilledTonalIconButton(onClick=onOpenTimer){Icon(ExpressiveSymbols.Timer,"Sleep timer")}
                        IconToggleButton(checked=repeatMode!=RepeatModes.OFF,onCheckedChange={guarded(onCycleRepeat)}){Icon(if(repeatMode==RepeatModes.ONE)ExpressiveSymbols.RepeatOne else ExpressiveSymbols.Repeat,"Repeat mode")}
                    }
                    Surface(color=MaterialTheme.colorScheme.surfaceContainerHigh,shape=RoundedCornerShape(28.dp)) {
                        Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick={queueOpen=true},modifier=Modifier.weight(1f).heightIn(min=52.dp)){Icon(ExpressiveSymbols.QueueMusic,null);Spacer(Modifier.width(8.dp));Text("Up next")}
                            FilledTonalButton(onClick={page="lyrics"},modifier=Modifier.weight(1f).heightIn(min=52.dp)){Icon(ExpressiveSymbols.Lyrics,null);Spacer(Modifier.width(8.dp));Text("Lyrics")}
                        }
                    }
                }}
                if(wide)Row(Modifier.fillMaxSize().padding(horizontal=32.dp),verticalAlignment=Alignment.CenterVertically) {
                    Artwork(Modifier.weight(1f).aspectRatio(1f))
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){Controls()}
                } else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
                    Box(Modifier.widthIn(max=compactArtwork).fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp)){Artwork(Modifier.fillMaxWidth().aspectRatio(1f))}
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable internal fun ExpressivePlayButton(playing: Boolean,loading: Boolean,reduce: Boolean,onClick: ()->Unit,modifier: Modifier=Modifier) {
    val spatial=MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val corner by animateFloatAsState(if(playing)24f else 48f,animationSpec=if(reduce)snap() else spatial,label="playShape")
    Button(onClick=onClick,modifier=modifier.height(88.dp).widthIn(min=112.dp),shape=RoundedCornerShape(corner.dp),contentPadding=PaddingValues(20.dp)) {
        if(loading) {if(reduce)CircularProgressIndicator(Modifier.size(32.dp),color=MaterialTheme.colorScheme.onPrimary) else LoadingIndicator(Modifier.size(40.dp),color=MaterialTheme.colorScheme.onPrimary)}
        else Icon(if(playing)ExpressiveSymbols.Pause else ExpressiveSymbols.PlayArrow,if(playing)"Pause" else "Play",Modifier.size(40.dp))
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable private fun ExpressiveTransport(playing: Boolean,loading: Boolean,previous: Boolean,next: Boolean,locked: Boolean,reduce: Boolean,onPrevious: ()->Unit,onPlay: ()->Unit,onNext: ()->Unit) {
    val backInteraction=remember{MutableInteractionSource()}
    val playInteraction=remember{MutableInteractionSource()}
    val nextInteraction=remember{MutableInteractionSource()}
    val spatial=MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val corner by animateFloatAsState(if(playing)24f else 44f,animationSpec=if(reduce)snap() else spatial,label="transportShape")
    ButtonGroup(Modifier.fillMaxWidth(),expandedRatio=if(reduce)0f else .12f,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick=onPrevious,enabled=previous && !locked,interactionSource=backInteraction,
            modifier=Modifier.weight(1f).height(80.dp).then(if(reduce)Modifier else Modifier.animateWidth(backInteraction)),contentPadding=PaddingValues(0.dp)) {Icon(ExpressiveSymbols.SkipPrevious,"Previous",Modifier.size(32.dp))}
        Button(onClick=onPlay,enabled=!locked,interactionSource=playInteraction,
            modifier=Modifier.weight(1.7f).height(80.dp).then(if(reduce)Modifier else Modifier.animateWidth(playInteraction)),
            shapes=ButtonDefaults.shapes(shape=RoundedCornerShape(corner.dp),pressedShape=RoundedCornerShape(16.dp)),contentPadding=PaddingValues(0.dp)) {
            if(loading) {if(reduce)CircularProgressIndicator(Modifier.size(28.dp),color=MaterialTheme.colorScheme.onPrimary) else LoadingIndicator(Modifier.size(40.dp),color=MaterialTheme.colorScheme.onPrimary)}
            else Icon(if(playing)ExpressiveSymbols.Pause else ExpressiveSymbols.PlayArrow,if(playing)"Pause" else "Play",Modifier.size(40.dp))
        }
        FilledTonalButton(onClick=onNext,enabled=next && !locked,interactionSource=nextInteraction,
            modifier=Modifier.weight(1f).height(80.dp).then(if(reduce)Modifier else Modifier.animateWidth(nextInteraction)),contentPadding=PaddingValues(0.dp)) {Icon(ExpressiveSymbols.SkipNext,"Next",Modifier.size(32.dp))}
    }
}
private fun time(ms: Long): String {val seconds=ms.coerceAtLeast(0)/1000;return "${seconds/60}:${(seconds%60).toString().padStart(2,'0')}"}
@OptIn(ExperimentalMaterial3Api::class,ExperimentalMaterial3ExpressiveApi::class)
@Composable private fun ExpressiveSeek(position: PlaybackPosition,duration: Long,locked: Boolean,playing: Boolean,reduce: Boolean,onSeek: (Float)->Unit,modifier: Modifier=Modifier) {
    var dragging by remember {mutableStateOf<Float?>(null)}
    val fraction=if(duration>0)(position.positionMs.toFloat()/duration).coerceIn(0f,1f) else 0f
    Column(modifier.fillMaxWidth()) {
        // Slider owns gestures, keyboard/accessibility and RTL. Official wavy progress draws its track.
        Slider(value=dragging ?: fraction,onValueChange={dragging=it},onValueChangeFinished={dragging?.let(onSeek);dragging=null},enabled=duration>0 && !locked,
            modifier=Modifier.semantics{contentDescription="Playback position"},
            thumb={Box(Modifier.size(4.dp,24.dp).background(if(locked)MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,RoundedCornerShape(2.dp)))},
            track={state->LinearWavyProgressIndicator(progress={state.value},modifier=Modifier.fillMaxWidth().height(12.dp).clearAndSetSemantics{},
                color=if(locked)MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.outlineVariant,
                stopSize=0.dp,gapSize=6.dp,amplitude={if(reduce)0f else .8f},wavelength=24.dp,waveSpeed=if(playing && !reduce)24.dp else 0.dp)})
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(time(if(dragging!=null)(dragging!!*duration).toLong() else position.positionMs),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(if(duration>0)time(duration) else "—:—",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable private fun ExpressiveLyrics(lines: List<LyricLine>?,subLines: List<LyricLine>?,unavailable: Boolean,position: PlaybackPosition,onSeek: (Long)->Unit,reduce: Boolean,modifier: Modifier) {
    if(lines.isNullOrEmpty()) {
        Box(modifier.fillMaxWidth().padding(24.dp),contentAlignment=Alignment.Center) {
            Surface(shape=RoundedCornerShape(32.dp),color=MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.padding(32.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Icon(ExpressiveSymbols.Lyrics,null,Modifier.size(40.dp),tint=MaterialTheme.colorScheme.primary)
                    Text(if(unavailable)"No lyrics just yet" else "Finding the words…",style=MaterialTheme.typography.headlineMedium)
                    Text(if(unavailable)"Try another lyrics provider from the controls below." else "Your track keeps playing while lyrics load.",color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }
    val list=rememberLazyListState()
    val current=lines.indexOfLast{it.timeMs<=skinLyricsPosition(position.positionMs,skinPlayerSettings().lyricsOffsetMs.value)}.coerceAtLeast(0)
    LaunchedEffect(current){if(!list.isScrollInProgress){if(reduce)list.scrollToItem(current) else list.animateScrollToItem(current)}}
    val spatial=MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effects=MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    LazyColumn(modifier.fillMaxWidth(),state=list,contentPadding=PaddingValues(horizontal=16.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        itemsIndexed(lines,key={index,line->"$index:${line.timeMs}"}){index,line->
            val active=index==current
            val fill by animateColorAsState(if(active)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,animationSpec=if(reduce)snap() else effects,label="lyricFill")
            val corner by animateFloatAsState(if(active)32f else 16f,animationSpec=if(reduce)snap() else spatial,label="lyricShape")
            Surface(shape=RoundedCornerShape(corner.dp),color=fill,contentColor=if(active)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(corner.dp)).clickable(role=Role.Button){onSeek(line.timeMs)}.semantics{if(active)stateDescription="Current lyric"}) {
                Column(Modifier.padding(horizontal=24.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text(line.text.ifEmpty{"♪"},style=MaterialTheme.typography.headlineMedium,fontWeight=if(active)FontWeight.Bold else FontWeight.Medium)
                    subLines?.getOrNull(index)?.let {Text(it.text,style=MaterialTheme.typography.bodyLarge)}
                }
            }
        }
    }
}
@Composable internal fun ExpressiveMiniPlayer(song: Song,playing: Boolean,loading: Boolean,position: PlaybackPosition,durationMs: Long,saved: Boolean,onSave: ()->Unit,onPlayPause: ()->Unit,onExpand: ()->Unit,onNext: ()->Unit,onPrevious: ()->Unit,locked: Boolean,onBlocked: ()->Unit,modifier: Modifier) {
    val artwork=rememberSkinRemoteArtwork(song)
    Surface(onClick=onExpand,modifier=modifier.padding(horizontal=12.dp,vertical=8.dp).fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.primaryContainer) {
        Column {
            Row(Modifier.fillMaxWidth().padding(start=12.dp,top=4.dp,bottom=4.dp,end=4.dp),verticalAlignment=Alignment.CenterVertically) {
                AsyncImage(artwork,null,Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape),contentScale=ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(song.title,style=MaterialTheme.typography.titleSmall,maxLines=1,overflow=TextOverflow.Ellipsis);Text(song.artist,style=MaterialTheme.typography.bodySmall,maxLines=1,overflow=TextOverflow.Ellipsis)}
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
