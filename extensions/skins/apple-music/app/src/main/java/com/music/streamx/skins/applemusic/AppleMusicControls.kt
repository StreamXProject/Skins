package com.music.streamx.skins.applemusic

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.BoxWithConstraints
import com.music.streamx.ui.skins.LocalSkinGlassFactory
import kotlin.math.roundToInt
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.layout.layout
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
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import kotlinx.coroutines.launch
import kotlin.math.abs

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

/**
 * iOS 26's switch. A white knob on a track that fills with the accent. Touched,
 * the knob swells into a clear glass lens riding over the track; dragged, it
 * slides with the finger, leaning into the way it's going, and the track's
 * colour follows it across; let go, it settles to the nearer side and turns
 * solid again. Without liquid glass the knob stays white but moves the same way.
 */
@Composable internal fun AppleToggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean, modifier: Modifier) {
    val off = if (isDark()) Color(0x52787880) else Color(0x29787880)
    val accent = MaterialTheme.colorScheme.primary
    val glass = com.music.streamx.ui.skins.LocalSkinLiquidGlass.current
    val position = remember { Animatable(if (checked) 1f else 0f) }
    val lift = remember { Animatable(0f) }
    val lean = remember { Animatable(0f) }
    var held by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val current by rememberUpdatedState(checked)
    val change by rememberUpdatedState(onCheckedChange)
    LaunchedEffect(checked) {
        if (!held) position.animateTo(if (checked) 1f else 0f, spring(dampingRatio = .68f, stiffness = 500f))
    }
    val interactive = onCheckedChange != null && enabled
    Box(modifier.size(62.dp, 26.dp).alpha(if (enabled) 1f else .4f)
        .semantics {
            role = Role.Switch
            toggleableState = ToggleableState(checked)
            if (interactive) onClick { change?.invoke(!current); true }
        }
        .then(if (interactive) Modifier.pointerInput(Unit) {
            val travel = 21.dp.toPx()
            awaitEachGesture {
                val down = awaitFirstDown()
                // The switch owns its touches; a row around it doesn't toggle too.
                down.consume()
                held = true
                scope.launch { lift.animateTo(1f, spring(dampingRatio = .6f, stiffness = 700f)) }
                val start = position.value
                var moved = false
                while (true) {
                    val c = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (!c.pressed) { c.consume(); break }
                    val dx = c.position.x - down.position.x
                    if (!moved && abs(dx) > viewConfiguration.touchSlop) moved = true
                    if (moved) {
                        c.consume()
                        val step = c.position.x - c.previousPosition.x
                        scope.launch {
                            position.snapTo(rubberBand(start + dx / travel))
                            lean.snapTo((step / 6.dp.toPx()).coerceIn(-1f, 1f))
                        }
                    }
                }
                val target = if (moved) position.value > .5f else !current
                held = false
                scope.launch { lean.animateTo(0f, spring(dampingRatio = .4f, stiffness = 400f)) }
                scope.launch { lift.animateTo(0f, spring(dampingRatio = .55f, stiffness = 380f)) }
                scope.launch { position.animateTo(if (target) 1f else 0f, spring(dampingRatio = .62f, stiffness = 420f)) }
                if (target != current) change?.invoke(target)
            }
        } else Modifier)
    ) {
        // Three layers: the track, which the lens refracts; the white knob and its
        // shadow; and, while held, the lens itself — real glass where the platform
        // can bend light, drawn glass where it can't.
        val glassBackdrop = if (glass) LocalSkinGlassFactory.current() else null
        val lensModifier = glassBackdrop?.lens(RoundedCornerShape(percent = 50))
        val card = MaterialTheme.colorScheme.surface
        Box(Modifier.matchParentSize().then(glassBackdrop?.source ?: Modifier).drawBehind {
            // The row's own colour round the pill, so what the lens bends past the
            // track's ends is the row behind it, not the page's black.
            if (glassBackdrop != null) drawRect(card)
            val p = position.value
            val track = lerp(off, accent, ((p - .38f) / .24f).coerceIn(0f, 1f))
            drawRoundRect(track, cornerRadius = CornerRadius(size.height / 2))
        })
        Box(Modifier.matchParentSize().drawBehind {
            val realLens = lensModifier != null
            val p = position.value
            val l = lift.value
            val tilt = lean.value
            // The track keeps its colour until the knob is well past halfway, then turns
            // quickly, as iOS's does, rather than passing through a muddy blend.
            val track = lerp(off, accent, ((p - .38f) / .24f).coerceIn(0f, 1f))
            // iOS 26's knob is a capsule, not a disc: 37 by 22 in a 62 by 26 track.
            val pad = 2.dp.toPx()
            val knobW = 37.dp.toPx()
            val knobH = 22.dp.toPx()
            // The knob grows past the track when lifted, as the iOS lens does,
            // and stretches the way it's moving while it moves.
            val w = knobW * (1f + .54f * l) * (1f + .1f * abs(tilt))
            val h = knobH * (1f + .5f * l) * (1f - .05f * abs(tilt))
            val cx = pad + knobW / 2 + p * (size.width - 2 * pad - knobW) + tilt * 2.dp.toPx()
            val cy = size.height / 2
            val topLeft = Offset(cx - w / 2, cy - h / 2)
            val knobSize = Size(w, h)
            val corner = CornerRadius(h / 2)
            // At rest a small dark shadow under the white knob; lifted, that gives
            // way to a soft glow of the track's own colour round the lens.
            drawRoundRect(Color.Black.copy(alpha = .16f * (1f - l)), topLeft + Offset(0f, 1.dp.toPx()), knobSize, corner)
            // Only for the drawn knob: the real lens carries its own soft shadow, and
            // a glow round it read as stray light, which iOS's switch has none of.
            if (l > 0f && !realLens) for (i in 1..3) {
                val g = i * 2.dp.toPx()
                drawRoundRect(track.copy(alpha = .08f * l), topLeft - Offset(g, g), Size(w + g * 2, h + g * 2), CornerRadius(h / 2 + g))
            }
            if (!glass) {
                drawRoundRect(Color.White, topLeft, knobSize, corner)
                return@drawBehind
            }
            // The real lens is its own layer above; here only the white knob fading out under it.
            if (realLens) {
                drawRoundRect(Color.White.copy(alpha = 1f - l), topLeft, knobSize, corner)
                return@drawBehind
            }
            // Solid white at rest. Held, iOS 26's clear lens: the track seen through
            // it a shade brighter, a thin dark line just inside the edge where the
            // glass bends the light, a white rim brightest at two opposite corners,
            // and a sheen across the top.
            drawRoundRect(Color.White.copy(alpha = 1f - l), topLeft, knobSize, corner)
            if (l > 0f) {
                drawRoundRect(lerp(track, Color.White, .1f).copy(alpha = l), topLeft, knobSize, corner)
                val inset = 2.5.dp.toPx()
                drawRoundRect(lerp(track, Color.Black, .35f).copy(alpha = .55f * l), topLeft + Offset(inset, inset),
                    Size(w - inset * 2, h - inset * 2), CornerRadius(h / 2 - inset), style = Stroke(1.dp.toPx()))
                drawRoundRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = .38f * l), Color.Transparent), topLeft.y, topLeft.y + h * .45f),
                    topLeft + Offset(h * .18f, h * .08f), Size(w - h * .36f, h * .32f), CornerRadius(h * .16f))
                drawRoundRect(
                    Brush.linearGradient(
                        0f to Color.White.copy(alpha = .95f * l), .35f to Color.White.copy(alpha = .35f * l),
                        .65f to Color.White.copy(alpha = .35f * l), 1f to Color.White.copy(alpha = .95f * l),
                        start = topLeft, end = topLeft + Offset(w, h)),
                    topLeft, knobSize, corner, style = Stroke(1.8.dp.toPx()))
            }
        })
        if (lensModifier != null) Box(
            Modifier
                .layout { measurable, constraints ->
                    // Sized once at its largest; moved and shrunk per frame without relayout.
                    val w = (37.dp.toPx() * 1.54f).roundToInt()
                    val h = (22.dp.toPx() * 1.5f).roundToInt()
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.placeWithLayer(0, 0) {
                            val p = position.value
                            val l = lift.value
                            val tilt = lean.value
                            val pad = 2.dp.toPx()
                            val knobW = 37.dp.toPx()
                            val cx = pad + knobW / 2 + p * (constraints.maxWidth - 2 * pad - knobW) + tilt * 2.dp.toPx()
                            val cy = constraints.maxHeight / 2f
                            val sw = (1f + .54f * l) / 1.54f * (1f + .1f * abs(tilt))
                            val sh = (1f + .5f * l) / 1.5f * (1f - .05f * abs(tilt))
                            translationX = cx - w / 2f
                            translationY = cy - h / 2f
                            scaleX = sw
                            scaleY = sh
                            alpha = (l * 1.6f).coerceIn(0f, 1f)
                        }
                    }
                }
                .then(lensModifier),
        )
    }
}

/** Past either end the knob still gives a little, a quarter as much. */
private fun rubberBand(v: Float) = when {
    v < 0f -> v * .25f
    v > 1f -> 1f + (v - 1f) * .25f
    else -> v
}

/**
 * iOS 26's slider: a thin track filled in the accent and a white capsule thumb
 * that, held, turns to clear glass bending the track beneath it, as the Liquid
 * Glass setting's own does. Without glass the thumb stays white but swells.
 */
@Composable internal fun AppleSlider(value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int, enabled: Boolean, modifier: Modifier) {
    val off = if (isDark()) Color(0x52787880) else Color(0x29787880)
    val fill = MaterialTheme.colorScheme.primary
    val rowColor = MaterialTheme.colorScheme.surface
    val glass = com.music.streamx.ui.skins.LocalSkinLiquidGlass.current
    val glassBackdrop = if (glass) LocalSkinGlassFactory.current() else null
    val lensModifier = glassBackdrop?.lens(RoundedCornerShape(percent = 50))
    val lift = remember { Animatable(0f) }
    // How hard the thumb is being pulled sideways: stretches it along the pull and
    // thins it across, then wobbles it back to round when the finger stops or lifts.
    val lean = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val set by rememberUpdatedState(onValueChange)
    val span = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    // The thumb chases the value on a lively spring, so it trails a fast drag and
    // settles with a small bounce, rather than stretching to keep up.
    val shown = remember { Animatable(fraction) }
    var dragging by remember { mutableStateOf(false) }
    // While held, the finger moves the thumb directly; the setting only follows.
    // Waiting for each value to come back through the app is what made it stick.
    LaunchedEffect(fraction, dragging) { if (!dragging) shown.animateTo(fraction, spring(dampingRatio = .52f, stiffness = 650f)) }
    fun snap(f: Float): Float {
        val raw = valueRange.start + span * f.coerceIn(0f, 1f)
        if (steps <= 0) return raw
        val step = span / (steps + 1)
        return (valueRange.start + Math.round((raw - valueRange.start) / step) * step).coerceIn(valueRange.start, valueRange.endInclusive)
    }
    val thumbW = 40.dp
    val thumbH = 24.dp
    BoxWithConstraints(modifier.fillMaxWidth().height(36.dp).alpha(if (enabled) 1f else .4f)
        .then(if (enabled) Modifier.pointerInput(steps, valueRange) {
            val tw = thumbW.toPx()
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consume()
                scope.launch { lift.animateTo(1f, spring(dampingRatio = .6f, stiffness = 600f)) }
                val usable = (size.width - tw).coerceAtLeast(1f)
                dragging = true
                var last = snap((down.position.x - tw / 2) / usable)
                // Snappy enough to stay under the finger, soft enough to carry it smoothly.
                val follow = spring<Float>(dampingRatio = .82f, stiffness = 1400f)
                fun moveTo(x: Float) {
                    val raw = ((x - tw / 2) / usable).coerceIn(0f, 1f)
                    val value = snap(raw)
                    // A stepped slider's thumb lands on the steps; a smooth one on the finger.
                    val target = if (steps > 0) (value - valueRange.start) / span else raw
                    scope.launch { shown.animateTo(target, follow) }
                    if (value != last) { last = value; set(value) }
                }
                moveTo(down.position.x)
                set(last)
                try {
                    while (true) {
                        val e = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!e.pressed) break
                        e.consume()
                        moveTo(e.position.x)
                        val step = e.position.x - e.previousPosition.x
                        scope.launch { lean.snapTo((lean.value * .6f + step / 6.dp.toPx() * .4f).coerceIn(-1f, 1f)) }
                    }
                } finally {
                    dragging = false
                    // Let go, it bounces into place, as iOS's does.
                    scope.launch { shown.animateTo((last - valueRange.start) / span, spring(dampingRatio = .45f, stiffness = 500f)) }
                    scope.launch { lift.animateTo(0f, spring(dampingRatio = .55f, stiffness = 420f)) }
                    scope.launch { lean.animateTo(0f, spring(dampingRatio = .4f, stiffness = 420f)) }
                }
            }
        } else Modifier)) {
        val widthPx = constraints.maxWidth.toFloat()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val thumbWPx = with(density) { thumbW.toPx() }
        // The track, which the lens refracts.
        Box(Modifier.matchParentSize().then(glassBackdrop?.source ?: Modifier).drawBehind {
            // The row's own colour under the track: the backdrop otherwise records the
            // page's black there, and it shows as a dark band round the slider.
            if (glassBackdrop != null) drawRect(rowColor)
            val y = size.height / 2
            val h = 4.dp.toPx()
            val inset = thumbWPx / 2
            val usable = size.width - thumbWPx
            drawRoundRect(off, Offset(inset, y - h / 2), Size(usable, h), CornerRadius(h / 2))
            drawRoundRect(fill, Offset(inset, y - h / 2), Size(usable * shown.value, h), CornerRadius(h / 2))
            if (steps in 1..10) for (i in 0..steps + 1) drawCircle(off, 1.5.dp.toPx(), Offset(inset + usable * i / (steps + 1), y + 9.dp.toPx()))
        })
        Box(Modifier
            .offset { IntOffset(((widthPx - thumbWPx) * shown.value).roundToInt(), 0) }
            .align(Alignment.CenterStart)
            .size(thumbW, thumbH)
            .graphicsLayer { val s = 1f + .25f * lift.value; val k = abs(lean.value); scaleX = s * (1f + .04f * k); scaleY = s * (1f - .02f * k) }
            .then(if (lensModifier != null) lensModifier.drawBehind {
                drawRoundRect(Color.White.copy(alpha = 1f - lift.value), cornerRadius = CornerRadius(size.height / 2))
            } else Modifier.shadow(3.dp, RoundedCornerShape(percent = 50)).background(Color.White, RoundedCornerShape(percent = 50))))
    }
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
