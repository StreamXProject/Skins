package com.music.streamx.skins.dot

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val Edge = RoundedCornerShape(0.dp)

// Terminal / TUI Color Palette from dmt
internal val TuiBg = Color(0xFF000000)
internal val TuiSurface = Color(0xFF131415)
internal val TuiRaised = Color(0xFF080808)
internal val TuiFg = Color(0xFFC6CBCC)
internal val TuiBright = Color(0xFFDFE4E5)
internal val TuiDim = Color(0xFF9BA2A5)
internal val TuiFaint = Color(0xFF3C4245)
internal val TuiLine = Color(0xFF1E2122)
internal val TuiRed = Color(0xFFB85C50)
internal val TuiGreen = Color(0xFF7FA05F)

enum class DotAccent(val label: String, val color: Color) {
    AMBER("amber", Color(0xFFE8A33D)),
    STEEL("steel", Color(0xFF7A9BB3)),
    MOSS("moss", Color(0xFF98A96F)),
    MAUVE("mauve", Color(0xFFAF87A3)),
    TEAL("teal", Color(0xFF74AB9E)),
    LAVENDER("lavender", Color(0xFF9389C4)),
    RUST("rust", Color(0xFFB97B62)),
    MATRIX("matrix", Color(0xFF55D466)),
}

internal object DotOptions {
    var accent by mutableStateOf(DotAccent.AMBER)
    var asciiArt by mutableStateOf(true)
    var waveSweep by mutableStateOf(true)
    var cols by mutableIntStateOf(64)
    var mode by mutableStateOf("dark")

    private var loaded = false

    fun load(context: Context) {
        if (!loaded) {
            val sp = context.getSharedPreferences("dot_appearance", 0)
            val accentName = sp.getString("accent", "amber") ?: "amber"
            accent = DotAccent.entries.firstOrNull { it.label == accentName } ?: DotAccent.AMBER
            if (!sp.contains("ascii_art_v3")) {
                sp.edit()
                    .putBoolean("ascii_art", true)
                    .putBoolean("ascii_art_v3", true)
                    .apply()
                asciiArt = true
            } else {
                asciiArt = sp.getBoolean("ascii_art", true)
            }
            waveSweep = sp.getBoolean("wave_sweep", true)
            cols = sp.getInt("cols", 64)
            mode = sp.getString("mode", "dark") ?: "dark"
            loaded = true
        }
    }

    fun save(context: Context) {
        context.getSharedPreferences("dot_appearance", 0).edit()
            .putString("accent", accent.label)
            .putBoolean("ascii_art", asciiArt)
            .putBoolean("ascii_art_v3", true)
            .putBoolean("wave_sweep", waveSweep)
            .putInt("cols", cols)
            .putString("mode", mode)
            .apply()
    }
}

@Composable
internal fun dotColors(dark: Boolean): ColorScheme {
    val context = LocalContext.current
    SideEffect { DotOptions.load(context) }
    val isDark = DotOptions.mode == "dark" || (DotOptions.mode == "auto" && dark)
    val acc = DotOptions.accent.color

    return if (isDark) {
        darkColorScheme(
            primary = acc,
            onPrimary = TuiBg,
            primaryContainer = TuiSurface,
            onPrimaryContainer = TuiBright,
            secondary = TuiFg,
            onSecondary = TuiBg,
            secondaryContainer = TuiRaised,
            onSecondaryContainer = TuiBright,
            background = TuiBg,
            onBackground = TuiBright,
            surface = TuiSurface,
            onSurface = TuiBright,
            surfaceVariant = TuiLine,
            onSurfaceVariant = TuiDim,
            outline = TuiLine,
            outlineVariant = TuiFaint,
            error = TuiRed,
            onError = TuiBright,
        )
    } else {
        lightColorScheme(
            primary = acc,
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE2E5E6),
            onPrimaryContainer = Color(0xFF131415),
            secondary = Color(0xFF565C5F),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF2F4F5),
            onSecondaryContainer = Color(0xFF131415),
            background = Color(0xFFF2F4F5),
            onBackground = Color(0xFF131415),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF131415),
            surfaceVariant = Color(0xFFE2E5E6),
            onSurfaceVariant = Color(0xFF565C5F),
            outline = Color(0xFFD0D4D6),
            outlineVariant = Color(0xFFBCC1C4),
            error = TuiRed,
            onError = Color(0xFFFFFFFF),
        )
    }
}

internal fun dotTypography() = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp),
)

internal fun dotShapes() = Shapes(extraSmall = Edge, small = Edge, medium = Edge, large = Edge, extraLarge = Edge)

fun Modifier.tuiClickable(onClick: () -> Unit): Modifier =
    this.clickable(interactionSource = null, indication = null, onClick = onClick)

private class PressFlash(private val scope: CoroutineScope) {
    private val flash = Animatable(0f)
    val value: Float get() = flash.value
    fun click() {
        scope.launch {
            flash.snapTo(1f)
            flash.animateTo(0f, tween(180))
        }
    }
}

@Composable
private fun rememberPressFlash(): PressFlash {
    val scope = rememberCoroutineScope()
    return remember { PressFlash(scope) }
}

private class TuiPressState(
    val interactionSource: MutableInteractionSource,
    val flash: PressFlash,
    val isPressed: State<Boolean>,
) {
    val fraction: Float get() = if (isPressed.value) 1f else flash.value
    fun click(action: () -> Unit) {
        flash.click()
        action()
    }
}

@Composable
private fun rememberTuiPressState(): TuiPressState {
    val interactionSource = remember { MutableInteractionSource() }
    return TuiPressState(
        interactionSource = interactionSource,
        flash = rememberPressFlash(),
        isPressed = interactionSource.collectIsPressedAsState(),
    )
}

@Composable
internal fun rememberCursorAlpha(periodMs: Int = 1060): Float {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = periodMs
                1f at 0
                1f at periodMs / 2
                0f at periodMs / 2 + 1
                0f at periodMs
            },
        ),
        label = "cursorAlpha",
    )
    return alpha
}

@Composable
internal fun CursorTitle(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val cursorAlpha = rememberCursorAlpha()
    val accent = DotOptions.accent.color
    Text(
        text = buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = accent.copy(alpha = cursorAlpha))) {
                append("_")
            }
        },
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        modifier = modifier.basicMarquee(iterations = Int.MAX_VALUE),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TuiKey(
    label: String,
    modifier: Modifier = Modifier,
    bright: Boolean = false,
    big: Boolean = false,
    fill: Boolean = false,
    accent: Boolean = false,
    enabled: Boolean = true,
    onHold: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val press = rememberTuiPressState()
    val scope = rememberCoroutineScope()
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    val restText = when {
        accent -> acc
        bright -> c.background
        else -> c.onSurface
    }
    val restBorder = if (bright) c.onSurface else c.outline
    val restBg = if (bright) c.onSurface else c.surfaceVariant
    val pressText = if (bright) c.onSurface else c.background
    val pressBorder = if (bright) c.outline else c.onSurface
    val pressBg = if (bright) c.surfaceVariant else c.onSurface

    val horizontalPadding = when {
        fill -> 4.dp
        big -> 12.dp
        else -> 10.dp
    }
    val style = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    BoxWithConstraints(
        modifier = modifier.heightIn(min = 48.dp)
            .border(1.dp, lerp(restBorder, pressBorder, press.fraction), Edge)
            .background(lerp(restBg, pressBg, press.fraction))
            .combinedClickable(
                enabled = enabled,
                interactionSource = press.interactionSource,
                indication = null,
                onLongClick = onHold?.let {
                    {
                        scope.launch {
                            while (press.isPressed.value) {
                                it()
                                delay(200L)
                            }
                        }
                    }
                },
                onClick = { press.click(onClick) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val measuredWidth = measurer.measure(label, style = style, softWrap = false).size.width
        val availableWidth = with(density) { (maxWidth - horizontalPadding * 2).coerceAtLeast(1.dp).toPx() }
        val scale = if (constraints.hasBoundedWidth && measuredWidth > 0) {
            (availableWidth / measuredWidth).coerceAtMost(1f)
        } else 1f
        Text(
            text = label,
            style = style.copy(fontSize = style.fontSize * scale),
            color = lerp(restText, pressText, press.fraction).let { if (enabled) it else it.copy(alpha = 0.38f) },
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 9.dp),
        )
    }
}

@Composable
internal fun TuiTab(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val c = MaterialTheme.colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (active) c.background else c.onSurfaceVariant,
        modifier = modifier
            .border(1.dp, if (active) c.onSurface else c.outline, Edge)
            .background(if (active) c.onSurface else c.surface)
            .tuiClickable(onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    )
}

@Composable
internal fun TuiChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, Edge)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TuiStatus(
    label: String,
    value: String,
    on: Boolean,
    busy: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val press = rememberTuiPressState()
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color
    val restText = if (on) c.onSurface else c.onSurfaceVariant
    val blink = if (busy) rememberCursorAlpha() else 1f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, lerp(c.outline, c.onSurface, press.fraction), Edge)
            .background(lerp(c.surface, c.onSurface, press.fraction))
            .combinedClickable(
                interactionSource = press.interactionSource,
                indication = null,
                onLongClick = onLongClick?.let { { press.click(it) } },
                onClick = { press.click(onClick) },
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(
                    when {
                        busy -> acc.copy(alpha = blink)
                        on -> acc
                        else -> c.outlineVariant
                    },
                ),
        )
        Text(
            text = " $label:$value",
            style = MaterialTheme.typography.labelMedium,
            color = lerp(restText, c.background, press.fraction),
        )
    }
}

@Composable
internal fun Hairline(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = DotOptions.accent.color,
) {
    LinearProgressIndicator(
        progress = { fraction.coerceIn(0f, 1f) },
        color = color,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        strokeCap = StrokeCap.Square,
        gapSize = 0.dp,
        drawStopIndicator = {},
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp),
    )
}

@Composable
internal fun ThinSlider(
    fraction: Float,
    onScrub: (Float?) -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val filledColor = c.onSurface
    val emptyColor = c.outlineVariant
    val acc = DotOptions.accent.color

    val paint = remember {
        Paint().apply {
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textSize = 24f
        }
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                var last = 0f
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        last = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrub(last)
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        last = (change.position.x / size.width).coerceIn(0f, 1f)
                        onScrub(last)
                    },
                    onDragEnd = { onSeek(last) },
                    onDragCancel = { onScrub(null) },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val advance = paint.measureText("█")
            if (advance <= 0) return@Canvas
            val cols = (size.width / advance).toInt().coerceAtLeast(1)
            val filledCols = (cols * fraction.coerceIn(0f, 1f)).toInt()
            val baseline = size.height / 2 + paint.textSize * 0.35f
            drawContext.canvas.nativeCanvas.apply {
                for (col in 0 until cols) {
                    val filled = col < filledCols
                    val glyph = if (filled) "█" else "░"
                    paint.color = when {
                        col == filledCols - 1 -> acc.toArgb()
                        filled -> filledColor.toArgb()
                        else -> emptyColor.toArgb()
                    }
                    drawText(glyph, col * advance, baseline, paint)
                }
            }
        }
    }
}

@Composable
internal fun TuiPanel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, c.outline, Edge)
            .background(c.surface)
            .let { if (onClick != null) it.tuiClickable(onClick) else it }
            .padding(12.dp),
        content = content,
    )
}
