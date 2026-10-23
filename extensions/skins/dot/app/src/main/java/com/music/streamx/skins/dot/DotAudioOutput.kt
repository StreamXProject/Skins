package com.music.streamx.skins.dot

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.ui.player.PlayerPlatform

@Composable
internal fun DotAudioOutput(
    onDismiss: () -> Unit,
    onPipeline: () -> Unit,
    onCast: () -> Unit,
) {
    val host = PlayerPlatform.host
    val outputs = host.rememberAudioOutputs()
    val volume by host.volume.level.collectAsStateWithLifecycle()
    val format by host.outputFormat.collectAsStateWithLifecycle()
    var draft by remember { mutableFloatStateOf(volume) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(volume, dragging) {
        if (!dragging) draft = volume
    }

    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    AlertDialog(
        shape = Edge,
        containerColor = c.surface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "// AUDIO OUTPUT",
                style = MaterialTheme.typography.titleMedium,
                color = acc,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                outputs.forEach { output ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tuiClickable { host.selectAudioOutput(output.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (output.isActive) "[X] " else "[ ] ",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (output.isActive) acc else c.onSurfaceVariant,
                        )
                        Column {
                            Text(
                                text = output.name.ifBlank { output.kind.name },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (output.isActive) acc else c.onSurface,
                            )
                            if (output.isActive) {
                                Text(
                                    text = "routing here",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                TuiKey(
                    label = "[ DEFAULT SYSTEM OUTPUT ]",
                    modifier = Modifier.padding(top = 8.dp),
                    onClick = { host.selectAudioOutput(null) },
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "VOL: ${(draft * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.onSurfaceVariant,
                )
                Slider(
                    value = draft,
                    onValueChange = {
                        draft = it
                        dragging = true
                        host.volume.set(it)
                    },
                    onValueChangeFinished = {
                        host.volume.set(draft)
                        host.volume.refresh()
                        dragging = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = acc,
                        activeTrackColor = acc,
                        inactiveTrackColor = c.outline,
                    ),
                    modifier = Modifier.semantics { contentDescription = "Output volume" },
                )

                Text(
                    text = "CHAIN: ${format.summary}".lowercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TuiKey(label = "[ PIPELINE ]", modifier = Modifier.weight(1f), onClick = onPipeline)
                    TuiKey(label = "[ CAST ]", modifier = Modifier.weight(1f), onClick = onCast)
                }
            }
        },
        confirmButton = {
            TuiKey(label = "[ DONE ]", onClick = onDismiss)
        },
    )
}
