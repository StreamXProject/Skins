package com.music.streamx.skins.dot

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun DotSettingsGroup(
    header: String?,
    footer: String?,
    topSpacing: Dp,
    content: @Composable () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Column(Modifier.fillMaxWidth().padding(top = topSpacing)) {
        header?.let {
            Text(
                text = "// ${it.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = acc,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
            )
        }
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .border(1.dp, c.outline, Edge),
            shape = Edge,
            color = c.surface,
        ) {
            Column { content() }
        }
        footer?.let {
            Text(
                text = it.lowercase(),
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
internal fun DotSettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
internal fun DotSettingsRow(
    icon: ImageVector?,
    iconPainter: Painter?,
    title: String,
    subtitle: String?,
    subtitleContent: (@Composable () -> Unit)?,
    value: String?,
    badge: String?,
    enabled: Boolean,
    onClick: (() -> Unit)?,
    trailing: (@Composable () -> Unit)?,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(18.dp), tint = acc)
        } else if (iconPainter != null) {
            Icon(iconPainter, null, Modifier.size(18.dp), tint = acc)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) c.onSurface else c.onSurfaceVariant.copy(alpha = 0.5f),
            )
            if (subtitleContent != null) {
                subtitleContent()
            } else if (subtitle != null) {
                Text(
                    text = subtitle.lowercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            badge?.let {
                TuiChip(text = it.lowercase(), modifier = Modifier.padding(top = 4.dp))
            }
        }

        if (trailing != null) {
            trailing()
        } else {
            value?.let {
                Text(
                    text = "[$it]".lowercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = acc,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp),
                )
            }
            if (onClick != null) {
                Text(
                    text = ">",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun DotSettingsPage(
    query: String,
    onQuery: (String) -> Unit,
    padding: PaddingValues,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(padding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TuiKey(label = "[ < BACK ]", onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "// DOT / CONFIGURATION",
                style = MaterialTheme.typography.labelMedium,
                color = acc,
            )
        }
        HorizontalDivider(color = c.outline)

        CursorTitle(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            placeholder = { Text("/ search settings...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Text("/ ", style = MaterialTheme.typography.bodyLarge, color = acc) },
            singleLine = true,
            shape = Edge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = acc,
                unfocusedBorderColor = c.outline,
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun DotSleepTimerDialog(
    minutes: Int?,
    afterTrack: Boolean,
    running: Boolean,
    presets: List<Int>,
    onStart: (Int) -> Unit,
    onAfterTrack: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    AlertDialog(
        shape = Edge,
        containerColor = c.surface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "// SLEEP TIMER",
                style = MaterialTheme.typography.titleMedium,
                color = acc,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = when {
                        afterTrack -> "status: stopping after this track"
                        running -> "status: $minutes minutes remaining"
                        else -> "status: choose duration"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                presets.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pair.forEach { n ->
                            TuiKey(
                                label = "[ $n MIN ]",
                                modifier = Modifier.weight(1f),
                                onClick = { onStart(n); onDismiss() },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                TuiKey(
                    label = "[ STOP AFTER THIS TRACK ]",
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    onClick = { onAfterTrack(); onDismiss() },
                )
                if (running) {
                    TuiKey(
                        label = "[ CANCEL ACTIVE TIMER ]",
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        onClick = { onCancel(); onDismiss() },
                    )
                }
            }
        },
        confirmButton = {
            TuiKey(label = "[ DONE ]", onClick = onDismiss)
        },
    )
}

@Composable
internal fun DotAppearanceConfiguration(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TuiKey(label = "[ < BACK ]", onClick = onDismiss)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "// DOT APPEARANCE",
                style = MaterialTheme.typography.labelMedium,
                color = acc,
            )
        }

        CursorTitle(
            text = "TERMINAL CONFIG",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        // Accent Color Selection
        Text(
            text = "// ACCENT COLOR",
            style = MaterialTheme.typography.labelSmall,
            color = acc,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        DotAccent.entries.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { item ->
                    val selected = DotOptions.accent == item
                    TuiKey(
                        label = "[ ${item.label.uppercase()} ]",
                        accent = selected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            DotOptions.accent = item
                            DotOptions.save(context)
                        },
                    )
                }
            }
        }

        // Cover Art Options
        Spacer(Modifier.height(14.dp))
        Text(
            text = "// COVER ART PRESENTATION",
            style = MaterialTheme.typography.labelSmall,
            color = acc,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TuiKey(
                label = "[ ASCII ART ]",
                accent = DotOptions.asciiArt,
                modifier = Modifier.weight(1f),
                onClick = {
                    DotOptions.asciiArt = true
                    DotOptions.save(context)
                },
            )
            TuiKey(
                label = "[ RAW ART ]",
                accent = !DotOptions.asciiArt,
                modifier = Modifier.weight(1f),
                onClick = {
                    DotOptions.asciiArt = false
                    DotOptions.save(context)
                },
            )
        }

        // Wave Sweep Animation
        if (DotOptions.asciiArt) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TuiKey(
                    label = if (DotOptions.waveSweep) "[ WAVE SWEEP: ON ]" else "[ WAVE SWEEP: OFF ]",
                    accent = DotOptions.waveSweep,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        DotOptions.waveSweep = !DotOptions.waveSweep
                        DotOptions.save(context)
                    },
                )
            }
        }

        // Theme mode
        Spacer(Modifier.height(14.dp))
        Text(
            text = "// THEME MODE",
            style = MaterialTheme.typography.labelSmall,
            color = acc,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        listOf("dark" to "DARK TUI", "light" to "LIGHT TUI", "auto" to "FOLLOW SYSTEM").forEach { (id, label) ->
            TuiKey(
                label = "[ $label ]",
                accent = DotOptions.mode == id,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                onClick = {
                    DotOptions.mode = id
                    DotOptions.save(context)
                },
            )
        }

        // Info notes
        Spacer(Modifier.height(16.dp))
        Text(
            text = "IBM Plex Mono fonts bundled for offline rendering under the SIL Open Font License.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant,
        )
        Text(
            text = "dot is inspired by dmt ('dear music, thanks') by Jyotiraditya.",
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
