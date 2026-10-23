package com.music.streamx.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Uses the playback service's timer, shared with the regular player menu. */
@Composable
internal fun SpotuiSleepTimerDialog(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>,
    onStart: (Int) -> Unit, onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF282828),
        titleContentColor = Color.White, textContentColor = Color.White,
        title = { Text("Sleep timer") },
        text = {
            Column {
                presets.forEach { preset ->
                    Text("$preset minutes${if (minutes == preset) " ✓" else ""}",
                        Modifier.fillMaxWidth().clickable { onStart(preset); onDismiss() }.padding(vertical = 14.dp))
                }
                Text("After this song${if (afterTrack) " ✓" else ""}",
                    Modifier.fillMaxWidth().clickable { onAfterTrack(); onDismiss() }.padding(vertical = 14.dp))
                if (running) {
                    Text("Cancel timer", Modifier.fillMaxWidth().clickable { onCancel(); onDismiss() }.padding(vertical = 14.dp))
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Color.White) } })
}
