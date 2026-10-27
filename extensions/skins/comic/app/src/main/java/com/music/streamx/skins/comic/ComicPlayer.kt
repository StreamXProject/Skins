package com.music.streamx.skins.comic
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.*
import com.music.streamx.data.lyrics.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.skins.*
import com.music.streamx.ui.player.*
import com.music.streamx.ui.theme.StatusBarIcons
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun ComicPlayer(
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
    val c=MaterialTheme.colorScheme
    StatusBarIcons(c.background.luminance()>.5f)
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val locked=rememberSkinControlsLocked()
    val art=rememberSkinRemoteArtwork(song)
    val total=durationMs.takeIf{it>0} ?: durationFromText(song.durationText)
    var page by rememberSaveable {mutableIntStateOf(0)}
    var output by remember {mutableStateOf(false)}
    var pipeline by remember {mutableStateOf(false)}
    var cast by remember {mutableStateOf(false)}
    var providers by remember {mutableStateOf(false)}
    var timing by remember {mutableStateOf(false)}
    var options by remember {mutableStateOf(false)}
    val haze=remember {HazeState()}
    val openOutput=rememberSkinOutputPicker{output=true}
    val trackFormat = rememberSkinTrackFormat()
    val lyricsOffset by skinPlayerSettings().lyricsOffsetMs.collectAsStateWithLifecycle()
    fun action(block: ()->Unit){if(locked)onBlocked()else block()}
    SkinBackHandler(page!=0){page=0}
    BoxWithConstraints(modifier.fillMaxSize().background(c.background)) {
        val compact=maxHeight<760.dp
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().heightIn(min=64.dp).padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically) {
                OutlinedIconButton({if(page==0)onCollapse()else page=0},shape=Edge,border=BorderStroke(1.5.dp,c.onSurface),modifier=Modifier.size(44.dp).inkShadow(c.onSurface,3.dp).background(c.surface)) {Icon(if(page==0)ComicIcons.KeyboardArrowDown else ComicIcons.ArrowBack,if(page==0)"Collapse player"else"Back to player")}
                Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {Eyebrow(when(page){1->"THE WORDS";2->"UP NEXT";else->"SIDE A / TRACK ${(queueIndex+1).coerceAtLeast(1).toString().padStart(2,'0')}"});Text(song.playbackSource ?: "Now playing",style=MaterialTheme.typography.labelSmall,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
                Box {OutlinedIconButton({options=true},shape=Edge,border=BorderStroke(1.5.dp,c.onSurface),modifier=Modifier.size(44.dp).inkShadow(c.onSurface,3.dp).background(c.surface)){Icon(ComicIcons.MoreHoriz,"Player options")};DropdownMenu(options,{options=false}) {
                    DropdownMenuItem(text={Text("Track options")},onClick={options=false;onOpenMenu()})
                    DropdownMenuItem(text={Text("Audio output")},onClick={options=false;openOutput()})
                    DropdownMenuItem(text={Text("Sleep timer")},onClick={options=false;onOpenTimer()})
                    DropdownMenuItem(text={Text("Listen together")},onClick={options=false;action(onListenTogether)})
                    if(page==1){DropdownMenuItem(text={Text("Lyrics provider")},onClick={options=false;providers=true});DropdownMenuItem(text={Text("Lyrics timing")},onClick={options=false;timing=true})}
                }}
            }
            Crossfade(page,Modifier.weight(1f).fillMaxWidth(),animationSpec=tween(if(reduce)0 else 280),label="Player page") { current ->
                when(current) {
                    1 -> ComicLyrics(lyrics,lyricsUnavailable,position.positionMs,lyricsOffset,reduce,{action{onSeek(skinLyricsSeek(it,lyricsOffset))}})
                    2 -> ComicQueue(queue,queueIndex,isPlaying,onJumpTo,onRemove,onMove,onDragActive,onClear,{action(onPlayPause)},locked,autoplayEnabled,{action(onToggleAutoplay)})
                    else -> Column(Modifier.fillMaxSize().padding(horizontal=24.dp)) {
                        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(vertical=if(compact)8.dp else 16.dp),contentAlignment=Alignment.Center) {
                            val coverSize=minOf(maxWidth-24.dp,maxHeight-16.dp,440.dp).coerceAtLeast(48.dp)
                            Box(Modifier.size(coverSize+12.dp).inkShadow(c.onSurface,6.dp).border(3.dp,c.onSurface,Edge).padding(3.dp)) {
                                Crossfade(art,animationSpec=tween(if(reduce)0 else 320),label="Artwork change") { cover ->
                                    AsyncImage(cover,"Album artwork",Modifier.fillMaxSize().background(c.surface),contentScale=ContentScale.Crop)
                                }
                                com.music.streamx.ui.skins.SkinArtworkStats(Modifier.align(Alignment.BottomCenter).padding(start=8.dp,end=8.dp,bottom=48.dp))
                                ComicBurst(if(isPlaying)"LIVE!"else"PLAY!",Modifier.align(Alignment.TopStart).offset(x=(-14).dp,y=(-14).dp).rotate(-12f))
                                Text("NOW PLAYING",Modifier.align(Alignment.BottomEnd).offset(x=12.dp,y=(-10).dp).rotate(-4f).inkShadow(c.onSurface,3.dp).background(c.surface).background(c.surface).border(2.dp,c.onSurface).padding(horizontal=14.dp,vertical=7.dp),style=MaterialTheme.typography.labelSmall,color=c.onSurface)
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=if(compact)10.dp else 20.dp),verticalAlignment=Alignment.CenterVertically) {
                            Spacer(Modifier.width(48.dp))
                            Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                                Eyebrow(song.artist.uppercase(),Modifier.padding(bottom=7.dp))
                                Text(song.title.uppercase(),style=MaterialTheme.typography.headlineLarge,maxLines=2,overflow=TextOverflow.Ellipsis,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                            }
                            OutlinedIconButton({action(onToggleLike)},shape=Edge,border=BorderStroke(1.5.dp,c.onSurface),modifier=Modifier.size(40.dp).inkShadow(c.onSurface,3.dp).background(c.surface)) {
                                Icon(if(likeStatus==LikeStatus.LIKE)ComicIcons.Favorite else ComicIcons.FavoriteBorder,if(likeStatus==LikeStatus.LIKE)"Remove from favourites"else"Add to favourites",tint=if(likeStatus==LikeStatus.LIKE)ComicRed else c.onSurface)
                            }
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
                ComicTimeline(position.positionMs,total,locked,{action{onSeekFraction(it)}})
                Row(Modifier.fillMaxWidth().padding(top=if(compact)8.dp else 16.dp,bottom=20.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                    IconButton({action(onToggleShuffle)}){Icon(ComicIcons.Shuffle,"Shuffle",tint=if(shuffleEnabled)c.primary else c.onSurface)}
                    IconButton({action(onPrevious)},enabled=hasPrevious){Icon(ComicIcons.SkipPrevious,"Previous")}
                    ComicPlayButton(isPlaying,isLoading,{action(onPlayPause)},Modifier.size(64.dp),reduce)
                    IconButton({action(onNext)},enabled=hasNext){Icon(ComicIcons.SkipNext,"Next")}
                    IconButton({action(onCycleRepeat)}){Icon(if(repeatMode==1)ComicIcons.RepeatOne else ComicIcons.Repeat,"Repeat mode",tint=if(repeatMode!=0)c.primary else c.onSurface)}
                }
                Row(Modifier.fillMaxWidth().padding(bottom=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                    TextButton(openOutput,contentPadding=PaddingValues(0.dp)){Icon(ComicIcons.SpeakerGroup,null,Modifier.size(15.dp));Spacer(Modifier.width(7.dp));Text(trackFormat.ifBlank{"Audio output"},style=MaterialTheme.typography.labelSmall,maxLines=1)}
                    ComicSignal(isPlaying,Modifier.size(28.dp,18.dp))
                }
                Row(Modifier.fillMaxWidth().padding(bottom=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    ComicButton("Now",{page=0},Modifier.weight(1f),page==0)
                    ComicButton("Words",{page=1},Modifier.weight(1f),page==1)
                    ComicButton("Up next",{page=2},Modifier.weight(1f),page==2)
                }
            }
        }
    }
    if(output)ComicAudioOutput({output=false},{output=false;pipeline=true},{output=false;cast=true})
    if(pipeline)SkinAudioPipeline(haze,isPlaying,{pipeline=false})
    if(cast)PlayerPlatform.host.CastDialog(haze){cast=false}
    if(providers)AlertDialog(onDismissRequest={providers=false},title={Text("Lyrics provider")},text={Column{LyricsSource.entries.filterNot{it.hidden}.forEach{p->TextButton({onSelectLyricsProvider(p);providers=false}){Text(p.label+(if(p==lyricsSource)" ✓"else""))}}}},confirmButton={TextButton({providers=false}){Text("Done")}})
    if(timing || lyricsOffsetOpen)AlertDialog(onDismissRequest={timing=false;onDismissLyricsOffset()},title={Text("Lyrics timing")},text={Column{Text("$lyricsOffset ms");Slider(lyricsOffset.toFloat().coerceIn(-5000f,5000f),{skinPlayerSettings().setLyricsOffsetMs(it.toInt())},valueRange=-5000f..5000f)}},confirmButton={TextButton({timing=false;onDismissLyricsOffset()}){Text("Done")}})
}

@Composable private fun ComicPlayButton(playing: Boolean,loading: Boolean,onClick: ()->Unit,modifier: Modifier,reduce: Boolean) {
    val c=MaterialTheme.colorScheme
    Surface(onClick=onClick,modifier=modifier.inkShadow(ComicRed,4.dp,CircleShape),shape=CircleShape,color=Color(0xFF191919),contentColor=Color(0xFFFFF8E6),border=BorderStroke(2.dp,c.onSurface)) {
        Box(Modifier.fillMaxSize().semantics{contentDescription=if(playing)"Pause"else"Play"},contentAlignment=Alignment.Center) {
            if(loading)CircularProgressIndicator(Modifier.size(20.dp),color=ComicYellow,strokeWidth=2.dp)
            else Crossfade(playing,animationSpec=tween(if(reduce)0 else 160),label="Play pause"){Icon(if(it)ComicIcons.Pause else ComicIcons.PlayArrow,null,Modifier.size(26.dp))}
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ComicTimeline(position: Long,total: Long,locked: Boolean,onSeek: (Float)->Unit) {
    val c=MaterialTheme.colorScheme
    var draft by remember{mutableStateOf<Float?>(null)}
    val value=draft ?: if(total>0)(position.toFloat()/total).coerceIn(0f,1f)else 0f
    Column {
        Slider(value,{draft=it},onValueChangeFinished={draft?.let(onSeek);draft=null},enabled=total>0 && !locked,
            modifier=Modifier.fillMaxWidth().height(32.dp).semantics{contentDescription="Playback position"},
            thumb={Box(Modifier.size(17.dp).background(ComicYellow,CircleShape).border(2.dp,c.onSurface,CircleShape))},
            track={ state->Box(Modifier.fillMaxWidth().height(8.dp).background(c.surface).border(2.dp,c.onSurface)){Box(Modifier.fillMaxHeight().fillMaxWidth(state.value).background(ComicRed))}})
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(clock(draft?.let{(it*total).toLong()} ?: position),style=MaterialTheme.typography.labelSmall);Text(if(total>0)clock(total)else"—:—",style=MaterialTheme.typography.labelSmall,color=c.onSurfaceVariant)}
    }
}
internal fun clock(ms: Long): String {val s=(ms.coerceAtLeast(0)/1000);return "${s/60}:${(s%60).toString().padStart(2,'0')}"}
@Composable private fun ComicLyrics(lines: List<LyricLine>?,unavailable: Boolean,position: Long,offset: Int,reduce: Boolean,onSeek: (Long)->Unit) {
    val c=MaterialTheme.colorScheme
    if(lines.isNullOrEmpty()){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(if(unavailable)"The music speaks for itself.\nNo lyrics available."else"Finding the words…",style=MaterialTheme.typography.headlineSmall)};return}
    val state=rememberLazyListState()
    val active=lines.indexOfLast{it.timeMs<=skinLyricsPosition(position,offset)}.coerceAtLeast(0)
    LaunchedEffect(active){if(!state.isScrollInProgress){if(reduce)state.scrollToItem(active)else state.animateScrollToItem(active)}}
    LazyColumn(state=state,modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=24.dp,vertical=32.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        itemsIndexed(lines){index,line->
            val color by animateColorAsState(if(index==active)c.onSurface else c.onSurfaceVariant,tween(if(reduce)0 else 300),label="Lyric highlight")
            Row(Modifier.fillMaxWidth().background(if(index==active)ComicYellow else Color.Transparent).border(if(index==active)2.dp else 0.dp,if(index==active)c.onSurface else Color.Transparent).clickable{onSeek(line.timeMs)}.padding(horizontal=10.dp,vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                Box(Modifier.padding(top=8.dp).width(3.dp).height(28.dp).background(if(index!=active)Color.Transparent else if(c.primary==ComicYellow)ComicRed else c.primary,Dial))
                Text(line.text.ifBlank{"♪"},style=MaterialTheme.typography.headlineMedium,color=if(index==active)Color(0xFF191919)else color,modifier=Modifier.weight(1f))
            }
        }
        item{Spacer(Modifier.height(100.dp))}
    }
}

private fun durationFromText(text: String?): Long {
    val parts=text?.split(':')?.map {it.toLongOrNull() ?: return 0L} ?: return 0L
    return if(parts.size in 2..3 && parts.all{it>=0}) parts.fold(0L){n,v->n*60+v}*1000L else 0L
}
