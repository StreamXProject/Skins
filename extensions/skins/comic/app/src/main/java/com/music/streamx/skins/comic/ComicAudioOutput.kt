package com.music.streamx.skins.comic

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.ui.player.PlayerPlatform

@Composable internal fun ComicAudioOutput(onDismiss: ()->Unit,onPipeline: ()->Unit,onCast: ()->Unit) {
    val host=PlayerPlatform.host
    val outputs=host.rememberAudioOutputs()
    val volume by host.volume.level.collectAsStateWithLifecycle()
    val format by host.outputFormat.collectAsStateWithLifecycle()
    var draft by remember{mutableFloatStateOf(volume)}
    var dragging by remember{mutableStateOf(false)}
    LaunchedEffect(volume,dragging){if(!dragging)draft=volume}
    AlertDialog(shape=RoundedCornerShape(24.dp),onDismissRequest=onDismiss,title={Text("Where it plays.",style=MaterialTheme.typography.headlineMedium)},text={Column(Modifier.heightIn(max=480.dp).verticalScroll(rememberScrollState())) {
        outputs.forEach { output->Row(Modifier.fillMaxWidth().clickable{host.selectAudioOutput(output.id)}.padding(vertical=8.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){RadioButton(output.isActive,{host.selectAudioOutput(output.id)});Column{Text(output.name.ifBlank{output.kind.name});if(output.isActive)Text("Playing here",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.primary)}}}
        TextButton({host.selectAudioOutput(null)}){Text("System output")}
        Eyebrow("Volume",Modifier.padding(top=16.dp))
        Slider(draft,{draft=it;dragging=true;host.volume.set(it)},onValueChangeFinished={host.volume.set(draft);host.volume.refresh();dragging=false},modifier=Modifier.semantics{contentDescription="Output volume"})
        Text(format.summary,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onPipeline){Text("Audio pipeline")};TextButton(onCast){Text("Cast")}
    }},confirmButton={TextButton(onDismiss){Text("Done")}})
}
