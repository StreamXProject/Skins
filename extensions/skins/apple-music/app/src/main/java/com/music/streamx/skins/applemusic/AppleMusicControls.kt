package com.music.streamx.skins.applemusic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/*
 * UIKit's own controls, measured off iOS: the segmented control, the switch, the
 * slider and the alert. Greys are Apple's system fills, which are translucent so
 * they read on black, white and the artwork-coloured pages alike.
 */

@Composable private fun isDark() = MaterialTheme.colorScheme.background.luminance() < .5f

/** iOS's segmented control: a grey track, and a raised segment sliding to the choice. */
@Composable internal fun AppleSegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier) {
    val dark = isDark()
    val track = if (dark) Color(0x3D767680) else Color(0x1F767680)
    val thumb = if (dark) Color(0xFF636366) else Color.White
    BoxWithConstraints(modifier.fillMaxWidth().height(32.dp).alpha(if (enabled) 1f else .4f)
        .clip(RoundedCornerShape(9.dp)).background(track).padding(2.dp)) {
        val segment = maxWidth / options.size.coerceAtLeast(1)
        val offset by animateDpAsState(segment * selectedIndex, spring(dampingRatio = .82f, stiffness = 520f), label = "segment")
        Box(Modifier.offset(x = offset).width(segment).fillMaxHeight()
            .shadow(if (dark) 0.dp else 2.dp, RoundedCornerShape(7.dp)).clip(RoundedCornerShape(7.dp)).background(thumb))
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                // The hairline between two segments neither of which is chosen.
                if (index > 0 && index != selectedIndex && index - 1 != selectedIndex)
                    Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 7.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .18f)))
                Box(Modifier.weight(1f).fillMaxHeight()
                    .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(index) },
                    contentAlignment = Alignment.Center) {
                    Text(label, maxLines = 1, fontSize = 13.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/** iOS's switch: a 51×31 pill, the white knob springing across, the accent when on. */
@Composable internal fun AppleToggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean, modifier: Modifier) {
    val off = if (isDark()) Color(0x52787880) else Color(0x29787880)
    val track by animateColorAsState(if (checked) MaterialTheme.colorScheme.primary else off, label = "toggleTrack")
    val knob by animateDpAsState(if (checked) 20.dp else 0.dp, spring(dampingRatio = .72f, stiffness = 600f), label = "toggleKnob")
    Box(modifier.size(51.dp, 31.dp).alpha(if (enabled) 1f else .4f).clip(CircleShape).background(track)
        .then(if (onCheckedChange != null) Modifier.toggleable(checked, remember { MutableInteractionSource() }, null, enabled, Role.Switch, onCheckedChange) else Modifier)
        .padding(2.dp)) {
        Box(Modifier.offset(x = knob).size(27.dp).shadow(3.dp, CircleShape).background(Color.White, CircleShape))
    }
}

/** iOS's slider: a thin rounded track filled in the accent, and a white knob. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun AppleSlider(value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int, enabled: Boolean, modifier: Modifier) {
    val off = if (isDark()) Color(0x52787880) else Color(0x29787880)
    val fill = MaterialTheme.colorScheme.primary
    Slider(
        value = value, onValueChange = onValueChange, modifier = modifier, enabled = enabled,
        valueRange = valueRange, steps = steps,
        thumb = { Box(Modifier.size(28.dp).shadow(3.dp, CircleShape).background(Color.White, CircleShape)) },
        track = { state ->
            val span = (state.valueRange.endInclusive - state.valueRange.start).takeIf { it > 0f } ?: 1f
            val fraction = ((state.value - state.valueRange.start) / span).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(off)) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(fill))
            }
        },
    )
}

/** iOS's alert: a 270-wide rounded card, centred title and message, buttons split by hairlines. */
@Composable internal fun AppleAlertDialog(
    onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit, dismissButton: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?, text: (@Composable () -> Unit)?, modifier: Modifier,
) {
    val dark = isDark()
    val hairline = MaterialTheme.colorScheme.onSurface.copy(alpha = .18f)
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(modifier.width(270.dp), shape = RoundedCornerShape(14.dp), color = if (dark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)) {
            Column {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 19.dp, bottom = 17.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    title?.let { ProvideTextStyle(MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)) { it() } }
                    text?.let { ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, textAlign = TextAlign.Center)) { it() } }
                }
                HorizontalDivider(thickness = .5.dp, color = hairline)
                Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (dismissButton != null) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { dismissButton() }
                        Box(Modifier.width(.5.dp).height(44.dp).background(hairline))
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { confirmButton() }
                }
            }
        }
    }
}

/** iOS's page sheet: grabber, centred title with Done, the explanation, then an inset list with a checkmark. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun AppleOptionSheet(
    title: String, subtitle: String?, options: List<com.music.streamx.ui.skins.SkinOption>, selectedIndex: Int,
    onSelect: (Int) -> Unit, onDismissRequest: () -> Unit,
) {
    val dark = isDark()
    val page = if (dark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
    val cell = if (dark) Color(0xFF2C2C2E) else Color.White
    val accent = MaterialTheme.colorScheme.primary
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = page,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
        dragHandle = { Box(Modifier.padding(top = 6.dp).size(36.dp, 5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))) },
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 20.dp)) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(title, Modifier.align(Alignment.Center), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("Done", Modifier.align(Alignment.CenterEnd).clip(RoundedCornerShape(8.dp)).clickable(onClick = onDismissRequest).padding(horizontal = 6.dp, vertical = 4.dp),
                    fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = accent)
            }
            subtitle?.let {
                Text(it, Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, top = 4.dp, bottom = 18.dp),
                    fontSize = 13.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(22.dp), color = cell) {
                Column {
                    options.forEachIndexed { index, option ->
                        if (index > 0) HorizontalDivider(Modifier.padding(start = 20.dp), thickness = .5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .12f))
                        Row(Modifier.fillMaxWidth().clickable { onSelect(index) }.heightIn(min = 52.dp).padding(horizontal = 20.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(option.label, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                                option.detail?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            if (index == selectedIndex) Icon(LibraryGlyphs.Check, "Selected", Modifier.padding(start = 12.dp).size(20.dp), tint = accent)
                        }
                    }
                }
            }
        }
    }
}

/** iOS's text field in an alert: a compact rounded field, the hint as grey placeholder text. */
@Composable internal fun AppleTextField(value: String, onValueChange: (String) -> Unit, placeholder: String?, singleLine: Boolean, modifier: Modifier) {
    val dark = isDark()
    androidx.compose.foundation.text.BasicTextField(
        value = value, onValueChange = onValueChange, singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { field ->
            Box(Modifier.fillMaxWidth().heightIn(min = 34.dp).clip(RoundedCornerShape(8.dp))
                .background(if (dark) Color(0xFF1C1C1E) else Color.White)
                .padding(horizontal = 10.dp, vertical = 8.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty() && placeholder != null) Text(placeholder, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f))
                field()
            }
        },
    )
}
