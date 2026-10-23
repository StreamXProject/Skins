package com.music.streamx.skins.expressive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable internal fun ExpressiveLyricsProviders(source: LyricsSource?,states: Map<LyricsSource,LyricsProviderState>,onSelect: (LyricsSource)->Unit,onDismiss: ()->Unit) {
    val order by skinPlayerSettings().lyricsSourceOrder.collectAsStateWithLifecycle()
    val choices=(order+LyricsSource.entries).distinct().filterNot{it.hidden}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Lyrics provider")},text={Column(Modifier.heightIn(max=440.dp).verticalScroll(rememberScrollState())) {
        choices.forEach {provider->
            val fetching=states[provider]==LyricsProviderState.FETCHING
            ListItem(headlineContent={Text(provider.label)},supportingContent={Text(if(provider==source)"Current provider" else when(states[provider]){LyricsProviderState.FETCHING->"Loading…";LyricsProviderState.FOUND->"Lyrics available";LyricsProviderState.NOT_FOUND->"No lyrics found · Tap to retry";else->"Tap to search"})},leadingContent={if(fetching)CircularProgressIndicator(Modifier.size(24.dp),strokeWidth=2.dp) else RadioButton(selected=provider==source,onClick=null)},modifier=Modifier.clickable(enabled=!fetching){onSelect(provider);onDismiss()},colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerHigh))
        }
    }},confirmButton={TextButton(onClick=onDismiss){Text("Done")}})
}
@Composable internal fun ExpressiveAudioOutput(onDismiss: ()->Unit,onPipeline: ()->Unit,onCast: ()->Unit) {
    val host=PlayerPlatform.host
    val outputs=host.rememberAudioOutputs()
    val format by host.outputFormat.collectAsStateWithLifecycle()
    val volume by host.volume.level.collectAsStateWithLifecycle()
    // Android reports discrete hardware steps; retain pointer precision until release.
    var volumeDraft by remember { mutableFloatStateOf(volume.coerceIn(0f,1f)) }
    var adjustingVolume by remember { mutableStateOf(false) }
    LaunchedEffect(volume,adjustingVolume) { if(!adjustingVolume) volumeDraft=volume.coerceIn(0f,1f) }
    AlertDialog(onDismissRequest=onDismiss,icon={Icon(ExpressiveSymbols.Devices,null)},title={Text("Audio output")},text={Column(Modifier.heightIn(max=500.dp).verticalScroll(rememberScrollState())) {
        outputs.forEach {output->ListItem(headlineContent={Text(output.name.ifBlank {output.kind.name.lowercase().replaceFirstChar{it.uppercase()}})},supportingContent=if(output.isActive)({Text("Playing here")}) else null,leadingContent={RadioButton(selected=output.isActive,onClick=null)},modifier=Modifier.clickable {host.selectAudioOutput(output.id);onDismiss()},colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainerHigh))}
        if(outputs.isEmpty())Text("System audio output",style=MaterialTheme.typography.bodyLarge)
        TextButton(onClick={host.selectAudioOutput(null);onDismiss()}){Text("Use system output")}
        Text("Volume",style=MaterialTheme.typography.labelLarge)
        Slider(value=volumeDraft,onValueChange={adjustingVolume=true;volumeDraft=it;host.volume.set(it)},onValueChangeFinished={host.volume.set(volumeDraft);host.volume.refresh();adjustingVolume=false},modifier=Modifier.semantics{contentDescription="Output volume"})
        if(format.summary.isNotBlank())Text(format.summary,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick=onPipeline){Text("Audio pipeline")}
        TextButton(onClick=onCast){Icon(ExpressiveSymbols.Cast,null);Spacer(Modifier.width(8.dp));Text("Cast")}
    }},confirmButton={TextButton(onClick=onDismiss){Text("Done")}})
}
@Composable internal fun ExpressiveLyricsOffset(onDismiss: ()->Unit) {
    val settings=skinPlayerSettings()
    val offset by settings.lyricsOffsetMs.collectAsStateWithLifecycle()
    AlertDialog(onDismissRequest=onDismiss,title={Text("Lyrics timing")},text={Column {
        Text(if(offset==0)"No timing adjustment" else "${offset} ms",style=MaterialTheme.typography.titleLarge)
        Text("Negative values show lyrics earlier; positive values show them later.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(value=offset.toFloat().coerceIn(-5000f,5000f),onValueChange={settings.setLyricsOffsetMs(it.toInt())},valueRange=-5000f..5000f,modifier=Modifier.semantics{contentDescription="Lyrics offset in milliseconds"})
    }},confirmButton={TextButton(onClick=onDismiss){Text("Done")}},dismissButton={TextButton(onClick={settings.setLyricsOffsetMs(0)}){Text("Reset")}})
}
