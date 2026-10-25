package com.music.streamx.skins.common

import android.content.Context
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.music.streamx.skins.common.icons.*

/**
 * Fonts a listener can pick for a skin. All of them ship with Android, so a
 * skin needs no font files and works offline; [SKIN] keeps whatever the skin
 * was designed with.
 */
enum class FontChoice(val label: String) {
    SKIN("Skin default"), SANS("Sans"), SERIF("Serif"), MONO("Mono"), HANDWRITTEN("Handwritten"),
    CASUAL("Casual"), CONDENSED("Condensed"), LIGHT("Light"), SMALL_CAPS("Small caps");

    /** Null for [SKIN]: leave the skin's own family in place. */
    val family: FontFamily? by lazy {
        when (this) {
            SKIN -> null
            SANS -> FontFamily.SansSerif
            SERIF -> FontFamily.Serif
            MONO -> FontFamily.Monospace
            HANDWRITTEN -> FontFamily.Cursive
            CASUAL -> FontFamily(Typeface.create("casual", Typeface.NORMAL))
            CONDENSED -> FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
            LIGHT -> FontFamily(Typeface.create("sans-serif-light", Typeface.NORMAL))
            SMALL_CAPS -> FontFamily(Typeface.create("sans-serif-smallcaps", Typeface.NORMAL))
        }
    }
}

/**
 * A skin's appearance settings: accent, secondary colour and fonts, saved per
 * skin on this device. Each skin APK carries its own copy of skinCommon, so
 * this object is already per skin; [load] binds it to the skin's own file.
 *
 * Everything is Compose state, so the open settings page, the player and the
 * feed all repaint the moment a value changes.
 */
object SkinAppearance {
    /** Null keeps the skin's own accent. */
    var accent by mutableStateOf<Color?>(null)
    /** Null keeps the skin's own secondary colour (its tape, stickers and highlights). */
    var secondary by mutableStateOf<Color?>(null)
    /** The family for all ordinary text. */
    var textFont by mutableStateOf(FontChoice.SKIN)
    /** The family for the skin's signature lettering — Polaroid's handwriting, Paper's serif, an LCD's mono. */
    var displayFont by mutableStateOf(FontChoice.SKIN)

    /** The skin's own accent as the host resolved it, before any override; shown as "Default". */
    internal var skinAccent: Color? = null

    private var file: String? = null

    fun load(context: Context, skinId: String) {
        val name = "skin_appearance_$skinId"
        if (file == name) return
        file = name
        val p = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        accent = if (p.contains("accent")) Color(p.getInt("accent", 0)) else null
        secondary = if (p.contains("secondary")) Color(p.getInt("secondary", 0)) else null
        textFont = FontChoice.entries.firstOrNull { it.name == p.getString("textFont", null) } ?: FontChoice.SKIN
        displayFont = FontChoice.entries.firstOrNull { it.name == p.getString("displayFont", null) } ?: FontChoice.SKIN
    }

    fun save(context: Context) {
        val name = file ?: return
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().apply {
            accent?.let { putInt("accent", it.toArgb()) } ?: remove("accent")
            secondary?.let { putInt("secondary", it.toArgb()) } ?: remove("secondary")
            putString("textFont", textFont.name)
            putString("displayFont", displayFont.name)
        }.apply()
    }

    fun reset(context: Context) {
        accent = null; secondary = null; textFont = FontChoice.SKIN; displayFont = FontChoice.SKIN
        save(context)
    }
}

/**
 * The skin's signature family, unless the listener picked another one in the
 * skin's settings. Plain (not @Composable) so it also works inside draw and
 * style code; reading it during composition still tracks the setting.
 */
fun skinFont(default: FontFamily): FontFamily = SkinAppearance.displayFont.family ?: default

/** Black or white, whichever reads better on [background]. */
fun readableOn(background: Color): Color = if (background.luminance() > 0.45f) Color(0xFF15120F) else Color.White

/** The host's scheme for this skin with the listener's accent and secondary laid over it. */
internal fun androidx.compose.material3.ColorScheme.withAppearance(style: SkinStyle): androidx.compose.material3.ColorScheme {
    val accent = SkinAppearance.accent
    val secondary = SkinAppearance.secondary ?: style.defaultSecondary
    var scheme = this
    if (accent != null) scheme = scheme.copy(
        primary = accent, onPrimary = readableOn(accent), surfaceTint = accent,
        primaryContainer = accent.copy(alpha = .28f).compositeOver(surface), onPrimaryContainer = onSurface,
    )
    if (secondary != null) scheme = scheme.copy(
        secondary = secondary, onSecondary = readableOn(secondary),
        secondaryContainer = secondary.copy(alpha = .28f).compositeOver(surface), onSecondaryContainer = onSurface,
        tertiary = secondary, onTertiary = readableOn(secondary),
    ) else if (accent != null) scheme = scheme.copy(secondary = accent, onSecondary = readableOn(accent))
    return scheme
}

/** Applies [family] to every style in the scale. */
internal fun Typography.withFamily(family: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = family), displayMedium = displayMedium.copy(fontFamily = family),
    displaySmall = displaySmall.copy(fontFamily = family), headlineLarge = headlineLarge.copy(fontFamily = family),
    headlineMedium = headlineMedium.copy(fontFamily = family), headlineSmall = headlineSmall.copy(fontFamily = family),
    titleLarge = titleLarge.copy(fontFamily = family), titleMedium = titleMedium.copy(fontFamily = family),
    titleSmall = titleSmall.copy(fontFamily = family), bodyLarge = bodyLarge.copy(fontFamily = family),
    bodyMedium = bodyMedium.copy(fontFamily = family), bodySmall = bodySmall.copy(fontFamily = family),
    labelLarge = labelLarge.copy(fontFamily = family), labelMedium = labelMedium.copy(fontFamily = family),
    labelSmall = labelSmall.copy(fontFamily = family),
)

private val Swatches = listOf(
    0xFFF29BB5, 0xFFE5484D, 0xFFF76B15, 0xFFFFB224, 0xFFF4D98B, 0xFF8BC34A, 0xFF30A46C,
    0xFF12A594, 0xFF7CD3E8, 0xFF3E63DD, 0xFF6E56CF, 0xFFAB4ABA, 0xFFA18072, 0xFFE8E2D6, 0xFF2B2B2B,
).map { Color(it) }

/**
 * The settings page the host opens from Skins → this skin → Settings. Drawn in
 * the skin's own colours, and every change shows immediately because the page
 * itself sits inside the skin's theme.
 */
@Composable
fun AppearanceSettings(style: SkinStyle, onDismiss: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { SkinAppearance.load(context, style.id) }
    val c = MaterialTheme.colorScheme
    fun changed() = SkinAppearance.save(context)
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding().navigationBarsPadding()
        .verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onDismiss) { Icon(SkinIcons.ArrowBack, "Back", tint = c.onBackground) }
            Text("${style.brand} appearance", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = c.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            TextButton({ SkinAppearance.reset(context) }) { Text("Reset") }
        }
        PreviewStrip(style)
        SectionTitle("Accent", "Buttons, the playing track, progress and the selected tab.")
        SwatchRow(SkinAppearance.accent, skinDefault = null) { SkinAppearance.accent = it; changed() }
        SectionTitle("Secondary", style.secondaryHint)
        SwatchRow(SkinAppearance.secondary, skinDefault = style.defaultSecondary) { SkinAppearance.secondary = it; changed() }
        SectionTitle("Text font", "Everything you read: titles, rows, settings.")
        FontPicker(SkinAppearance.textFont) { SkinAppearance.textFont = it; changed() }
        SectionTitle("Signature font", style.signatureHint)
        FontPicker(SkinAppearance.displayFont) { SkinAppearance.displayFont = it; changed() }
        Text("Colours and fonts are saved for ${style.brand} on this device and follow the app's light or dark mode.",
            Modifier.padding(horizontal = 24.dp, vertical = 20.dp), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
    }
}

@Composable private fun SectionTitle(title: String, hint: String) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.onBackground)
        Text(hint, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
    }
}

@Composable private fun PreviewStrip(style: SkinStyle) {
    val c = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), shape = RoundedCornerShape(style.radius), color = c.surfaceVariant) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(style.radius / 2)).background(c.secondary), contentAlignment = Alignment.Center) {
                Icon(SkinIcons.MusicNote, null, tint = c.onSecondary)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text("Now playing", style = MaterialTheme.typography.titleMedium.copy(fontFamily = skinFont(MaterialTheme.typography.titleMedium.fontFamily ?: FontFamily.Default)), color = c.onSurface, maxLines = 1)
                Text("Your accent, secondary and fonts", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1)
                Box(Modifier.padding(top = 8.dp).fillMaxWidth().height(4.dp).clip(CircleShape).background(c.outlineVariant)) {
                    Box(Modifier.fillMaxWidth(.6f).fillMaxHeight().background(c.primary))
                }
            }
            Surface(shape = CircleShape, color = c.primary, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.PlayArrow, null, tint = c.onPrimary) }
            }
        }
    }
}

@Composable private fun SwatchRow(selected: Color?, skinDefault: Color?, onPick: (Color?) -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // "Skin default" first, drawn as the colour it resolves to with a ring.
        Swatch(skinDefault ?: SkinAppearance.skinAccent ?: c.primary, selected == null, "Default") { onPick(null) }
        Swatches.forEach { colour -> Swatch(colour, selected?.toArgb() == colour.toArgb(), null) { onPick(colour) } }
    }
    HueSlider(selected) { onPick(it) }
}

@Composable private fun Swatch(colour: Color, selected: Boolean, label: String?, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(colour)
            .border(if (selected) 3.dp else 1.dp, if (selected) c.onBackground else c.outlineVariant, CircleShape)
            .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            if (selected) Icon(SkinIcons.Check, "Selected", Modifier.size(20.dp), tint = readableOn(colour))
        }
        Text(label ?: "", style = MaterialTheme.typography.labelSmall, color = c.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}

/** Any colour at all, by hue, at a saturation and lightness that reads on both palettes. */
@Composable private fun HueSlider(selected: Color?, onPick: (Color) -> Unit) {
    val hue = remember(selected) {
        selected?.let { val hsv = FloatArray(3); android.graphics.Color.colorToHSV(it.toArgb(), hsv); hsv[0] } ?: 0f
    }
    var draft by remember(selected) { mutableFloatStateOf(hue) }
    fun colourAt(h: Float) = Color(android.graphics.Color.HSVToColor(floatArrayOf(h, .55f, .92f)))
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Custom", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp))
        Slider(draft, { draft = it }, Modifier.weight(1f), valueRange = 0f..360f, onValueChangeFinished = { onPick(colourAt(draft)) },
            colors = SliderDefaults.colors(thumbColor = colourAt(draft), activeTrackColor = colourAt(draft)))
    }
}

@Composable private fun FontPicker(selected: FontChoice, onPick: (FontChoice) -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FontChoice.entries.forEach { choice ->
            val on = choice == selected
            Surface(onClick = { onPick(choice) }, shape = RoundedCornerShape(14.dp),
                color = if (on) c.primary else c.surfaceVariant, contentColor = if (on) c.onPrimary else c.onSurface,
                border = BorderStroke(1.dp, if (on) c.primary else c.outlineVariant)) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aa", style = MaterialTheme.typography.titleLarge.copy(fontFamily = choice.family ?: MaterialTheme.typography.titleLarge.fontFamily))
                    Text(choice.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}

/**
 * The white of a print, a note or a label. On a dark page pure ivory glares and
 * reads as a different app from the player, so it is toned down to parchment there.
 */
@Composable
@ReadOnlyComposable
fun printPaper(): Color = if (MaterialTheme.colorScheme.background.luminance() < .4f) Color(0xFFDCD4C4) else Color(0xFFFFFDF8)
