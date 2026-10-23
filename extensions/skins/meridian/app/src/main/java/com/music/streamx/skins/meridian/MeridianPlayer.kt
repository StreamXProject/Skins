package com.music.streamx.skins.meridian
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
@Composable internal fun MeridianPlayer(
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
            Row(Modifier.fillMaxWidth().heightIn(min=60.dp).padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically) {
                IconButton({if(page==0)onCollapse()else page=0}){Icon(if(page==0)MeridianIcons.KeyboardArrowDown else MeridianIcons.ArrowBack,if(page==0)"Collapse player"else"Back to player")}
                Column(Modifier.weight(1f).padding(horizontal=10.dp)) {Eyebrow(when(page){1->"The words";2->"The sequence";else->"Now / Playing"});Text(song.playbackSource ?: "Your queue",style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
                MeridianSignal(isPlaying,Modifier.size(22.dp,20.dp).padding(end=4.dp))
                Box {IconButton({options=true}){Icon(MeridianIcons.MoreHoriz,"Player options")};DropdownMenu(options,{options=false}) {
                    DropdownMenuItem(text={Text("Track options")},onClick={options=false;onOpenMenu()})
                    DropdownMenuItem(text={Text("Audio output")},onClick={options=false;openOutput()})
                    DropdownMenuItem(text={Text("Sleep timer")},onClick={options=false;onOpenTimer()})
                    DropdownMenuItem(text={Text("Listen together")},onClick={options=false;action(onListenTogether)})
                    if(page==1){DropdownMenuItem(text={Text("Lyrics provider")},onClick={options=false;providers=true});DropdownMenuItem(text={Text("Lyrics timing")},onClick={options=false;timing=true})}
                }}
            }
            Crossfade(page,Modifier.weight(1f).fillMaxWidth(),animationSpec=tween(if(reduce)0 else 280),label="Player page") { current ->
                when(current) {
                    1 -> MeridianLyrics(lyrics,lyricsUnavailable,position.positionMs,lyricsOffset,reduce,{action{onSeek(skinLyricsSeek(it,lyricsOffset))}})
                    2 -> MeridianQueue(queue,queueIndex,isPlaying,onJumpTo,onRemove,onMove,onClear,{action(onPlayPause)},locked,autoplayEnabled,{action(onToggleAutoplay)})
                    else -> Column(Modifier.fillMaxSize().padding(horizontal=24.dp)) {
                        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(vertical=if(compact)8.dp else 16.dp),contentAlignment=Alignment.Center) {
                            val coverSize=minOf(maxWidth-24.dp,maxHeight-16.dp,440.dp).coerceAtLeast(48.dp)
                            // Mat and offset keyline create a quiet, architectural frame.
                            Box(Modifier.size(coverSize+12.dp).border(1.dp,c.outlineVariant,Edge).padding(6.dp)) {
                                Crossfade(art,animationSpec=tween(if(reduce)0 else 420),label="Artwork change") { cover ->
                                    AsyncImage(cover,"Album artwork",Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp)).background(c.surface),contentScale=ContentScale.Crop)
                                }
                                com.music.streamx.ui.skins.SkinArtworkStats(Modifier.align(Alignment.BottomCenter).padding(start=8.dp,end=8.dp,bottom=10.dp))
                            }
                        }
                        Row(Modifier.padding(top=8.dp,bottom=if(compact)12.dp else 20.dp),verticalAlignment=Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)){Eyebrow("${(queueIndex+1).coerceAtLeast(1).toString().padStart(2,'0')} / ${queue.size.toString().padStart(2,'0')}");Text(song.title,style=MaterialTheme.typography.headlineMedium,maxLines=2,overflow=TextOverflow.Ellipsis,modifier=Modifier.padding(top=6.dp));Text(song.artist,style=MaterialTheme.typography.bodyLarge,color=c.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.clickable{onOpenArtist(song.artistId,song.artist)}.padding(top=6.dp))}
                            IconButton({action(onToggleLike)}){Icon(if(likeStatus==LikeStatus.LIKE)MeridianIcons.Favorite else MeridianIcons.FavoriteBorder,if(likeStatus==LikeStatus.LIKE)"Remove from favourites"else"Add to favourites",tint=c.primary)}
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
                if(page==0) Row(Modifier.fillMaxWidth().padding(bottom=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                    TextButton(openOutput,contentPadding=PaddingValues(0.dp)){Icon(MeridianIcons.SpeakerGroup,null,Modifier.size(15.dp));Spacer(Modifier.width(7.dp));Text(trackFormat.ifBlank{"Audio output"},style=MaterialTheme.typography.labelSmall,maxLines=1)}
                    Row {IconButton({action(onToggleShuffle)}){Icon(MeridianIcons.Shuffle,"Shuffle",tint=if(shuffleEnabled)c.primary else c.onSurfaceVariant,modifier=Modifier.size(20.dp))};IconButton({action(onCycleRepeat)}){Icon(if(repeatMode==1)MeridianIcons.RepeatOne else MeridianIcons.Repeat,"Repeat mode",tint=if(repeatMode!=0)c.primary else c.onSurfaceVariant,modifier=Modifier.size(20.dp))}}
                }
                MeridianTimeline(position.positionMs,total,locked,{action{onSeekFraction(it)}})
                Row(Modifier.fillMaxWidth().padding(top=if(compact)8.dp else 16.dp,bottom=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    OutlinedIconButton({action(onPrevious)},modifier=Modifier.size(56.dp),enabled=hasPrevious,shape=Edge,border=BorderStroke(1.dp,c.outlineVariant)){Icon(MeridianIcons.SkipPrevious,"Previous")}
                    MeridianPlayButton(isPlaying,isLoading,{action(onPlayPause)},Modifier.weight(1f).height(64.dp),reduce)
                    OutlinedIconButton({action(onNext)},modifier=Modifier.size(56.dp),enabled=hasNext,shape=Edge,border=BorderStroke(1.dp,c.outlineVariant)){Icon(MeridianIcons.SkipNext,"Next")}
                }
                Row(Modifier.fillMaxWidth().padding(bottom=12.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    MeridianButton("Listen",{page=0},Modifier.weight(1f),page==0)
                    MeridianButton("Lyrics",{page=1},Modifier.weight(1f),page==1)
                    MeridianButton("Queue",{page=2},Modifier.weight(1f),page==2)
                }
            }
        }
    }
    if(output)MeridianAudioOutput({output=false},{output=false;pipeline=true},{output=false;cast=true})
    if(pipeline)SkinAudioPipeline(haze,isPlaying,{pipeline=false})
    if(cast)PlayerPlatform.host.CastDialog(haze){cast=false}
    if(providers)AlertDialog(onDismissRequest={providers=false},title={Text("Lyrics provider")},text={Column{LyricsSource.entries.filterNot{it.hidden}.forEach{p->TextButton({onSelectLyricsProvider(p);providers=false}){Text(p.label+(if(p==lyricsSource)" ✓"else""))}}}},confirmButton={TextButton({providers=false}){Text("Done")}})
    if(timing || lyricsOffsetOpen)AlertDialog(onDismissRequest={timing=false;onDismissLyricsOffset()},title={Text("Lyrics timing")},text={Column{Text("$lyricsOffset ms");Slider(lyricsOffset.toFloat().coerceIn(-5000f,5000f),{skinPlayerSettings().setLyricsOffsetMs(it.toInt())},valueRange=-5000f..5000f)}},confirmButton={TextButton({timing=false;onDismissLyricsOffset()}){Text("Done")}})
}

@Composable private fun MeridianPlayButton(playing: Boolean,loading: Boolean,onClick: ()->Unit,modifier: Modifier,reduce: Boolean) {
    val c=MaterialTheme.colorScheme
    val radius by animateDpAsState(if(playing)14.dp else 24.dp,tween(if(reduce)0 else 300),label="Transport shape")
    Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(radius),color=c.primary,contentColor=c.onPrimary) {
        Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
            if(loading)CircularProgressIndicator(Modifier.size(20.dp),color=c.onPrimary,strokeWidth=2.dp)
            else Crossfade(playing,animationSpec=tween(if(reduce)0 else 180),label="Play pause"){Icon(if(it)MeridianIcons.Pause else MeridianIcons.PlayArrow,null,Modifier.size(25.dp))}
            Spacer(Modifier.width(8.dp));Text(if(playing)"Pause"else"Play",style=MaterialTheme.typography.titleMedium)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun MeridianTimeline(position: Long,total: Long,locked: Boolean,onSeek: (Float)->Unit) {
    val c=MaterialTheme.colorScheme
    var draft by remember{mutableStateOf<Float?>(null)}
    val value=draft ?: if(total>0)(position.toFloat()/total).coerceIn(0f,1f)else 0f
    Column {
        Slider(value,{draft=it},onValueChangeFinished={draft?.let(onSeek);draft=null},enabled=total>0 && !locked,
            modifier=Modifier.fillMaxWidth().height(32.dp).semantics{contentDescription="Playback position"},
            thumb={Box(Modifier.size(12.dp).background(c.primary,CircleShape).border(3.dp,c.background,CircleShape))},
            track={ state->Box(Modifier.fillMaxWidth().height(3.dp).background(c.outlineVariant,Dial)){Box(Modifier.fillMaxHeight().fillMaxWidth(state.value).background(c.primary,Dial))}})
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(clock(draft?.let{(it*total).toLong()} ?: position),style=MaterialTheme.typography.labelSmall);Text(if(total>0)clock(total)else"—:—",style=MaterialTheme.typography.labelSmall,color=c.onSurfaceVariant)}
    }
}
internal fun clock(ms: Long): String {val s=(ms.coerceAtLeast(0)/1000);return "${s/60}:${(s%60).toString().padStart(2,'0')}"}
@Composable private fun MeridianLyrics(lines: List<LyricLine>?,unavailable: Boolean,position: Long,offset: Int,reduce: Boolean,onSeek: (Long)->Unit) {
    val c=MaterialTheme.colorScheme
    if(lines.isNullOrEmpty()){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(if(unavailable)"The music speaks for itself.\nNo lyrics available."else"Finding the words…",style=MaterialTheme.typography.headlineSmall)};return}
    val state=rememberLazyListState()
    val active=lines.indexOfLast{it.timeMs<=skinLyricsPosition(position,offset)}.coerceAtLeast(0)
    LaunchedEffect(active){if(!state.isScrollInProgress){if(reduce)state.scrollToItem(active)else state.animateScrollToItem(active)}}
    LazyColumn(state=state,modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=24.dp,vertical=32.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
        itemsIndexed(lines){index,line->
            val color by animateColorAsState(if(index==active)c.onSurface else c.onSurfaceVariant.copy(alpha=.75f),tween(if(reduce)0 else 300),label="Lyric highlight")
            Row(Modifier.fillMaxWidth().clickable{onSeek(line.timeMs)}.padding(vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                Box(Modifier.padding(top=8.dp).width(3.dp).height(28.dp).background(if(index==active)c.primary else Color.Transparent,Dial))
                Text(line.text.ifBlank{"♪"},style=MaterialTheme.typography.headlineMedium,color=color,modifier=Modifier.weight(1f))
            }
        }
        item{Spacer(Modifier.height(100.dp))}
    }
}

private fun durationFromText(text: String?): Long {
    val parts=text?.split(':')?.map {it.toLongOrNull() ?: return 0L} ?: return 0L
    return if(parts.size in 2..3 && parts.all{it>=0}) parts.fold(0L){n,v->n*60+v}*1000L else 0L
}
