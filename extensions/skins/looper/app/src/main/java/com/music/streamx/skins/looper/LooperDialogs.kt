package com.music.streamx.skins.looper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.data.lyrics.LyricsSource
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.player.PlayerPlatform
import com.music.streamx.ui.skins.skinPlayerSettings

@Composable internal fun LooperAudioOutput(onDismiss: ()->Unit,onPipeline: ()->Unit,onCast: ()->Unit){
    val host=PlayerPlatform.host;val outputs=host.rememberAudioOutputs();val volume by host.volume.level.collectAsStateWithLifecycle();val format by host.outputFormat.collectAsStateWithLifecycle()
    var draft by remember{mutableFloatStateOf(volume)};var dragging by remember{mutableStateOf(false)}
    LaunchedEffect(volume,dragging){if(!dragging)draft=volume}
    AlertDialog(shape=DialogShape,onDismissRequest=onDismiss,title={Text("Audio output")},text={Column(Modifier.heightIn(max=480.dp).verticalScroll(rememberScrollState())){
        outputs.forEach{output->LooperSettingsRow(null,null,output.name.ifBlank{output.kind.name},if(output.isActive)"Playing here"else null,null,null,null,true,{host.selectAudioOutput(output.id);onDismiss()}){RadioButton(output.isActive,null)}}
        TextButton(onClick={host.selectAudioOutput(null);onDismiss()}){Text("Use system output")}
        Text("Volume");Slider(draft,{dragging=true;draft=it;host.volume.set(it)},onValueChangeFinished={host.volume.set(draft);host.volume.refresh();dragging=false},modifier=Modifier.semantics{contentDescription="Output volume"})
        Text(format.summary,color=Dim,style=MaterialTheme.typography.bodyMedium)
        TextButton(onClick=onPipeline){Text("Audio pipeline")};TextButton(onClick=onCast){Text("Cast")}
    }},confirmButton={TextButton(onClick=onDismiss){Text("Done")}})
}
@Composable internal fun LooperLyricsProviders(source: LyricsSource?,states: Map<LyricsSource,LyricsProviderState>,onSelect: (LyricsSource)->Unit,onDismiss: ()->Unit){
    val order by skinPlayerSettings().lyricsSourceOrder.collectAsStateWithLifecycle()
    AlertDialog(shape=DialogShape,onDismissRequest=onDismiss,title={Text("Lyrics provider")},text={Column(Modifier.heightIn(max=440.dp).verticalScroll(rememberScrollState())){(order+LyricsSource.entries).distinct().filterNot{it.hidden}.forEach{provider->LooperSettingsRow(null,null,provider.label,states[provider]?.name,null,null,null,states[provider]!=LyricsProviderState.FETCHING,{onSelect(provider);onDismiss()}){RadioButton(provider==source,null)}}}},confirmButton={TextButton(onClick=onDismiss){Text("Done")}})
}
@Composable internal fun LooperLyricsOffset(onDismiss: ()->Unit){val settings=skinPlayerSettings();val offset by settings.lyricsOffsetMs.collectAsStateWithLifecycle();AlertDialog(shape=DialogShape,onDismissRequest=onDismiss,title={Text("Lyrics timing")},text={Column{Text("$offset ms");Slider(offset.toFloat().coerceIn(-5000f,5000f),{settings.setLyricsOffsetMs(it.toInt())},valueRange=-5000f..5000f);TextButton(onClick={settings.setLyricsOffsetMs(0)}){Text("Reset")}}},confirmButton={TextButton(onClick=onDismiss){Text("Done")}})}
@Composable internal fun LooperSleepTimerDialog(minutes: Int?,afterTrack: Boolean,running: Boolean,presets: List<Int>,onStart: (Int)->Unit,onAfterTrack: ()->Unit,onCancel: ()->Unit,onDismiss: ()->Unit){AlertDialog(shape=DialogShape,onDismissRequest=onDismiss,title={Text("Sleep timer")},text={Column{Text(if(afterTrack)"After this track"else if(running)"$minutes minutes remaining"else"Stop playback after");presets.forEach{n->TextButton(onClick={onStart(n);onDismiss()},shape=Capsule,modifier=Modifier.fillMaxWidth()){Text("$n minutes")}};TextButton(onClick={onAfterTrack();onDismiss()}){Text("End of track")}}},confirmButton={TextButton(onClick=onDismiss){Text("Done")}},dismissButton={if(running || afterTrack)TextButton(onClick={onCancel();onDismiss()}){Text("Cancel timer")}})}
