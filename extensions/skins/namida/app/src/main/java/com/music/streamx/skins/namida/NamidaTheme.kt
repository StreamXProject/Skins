package com.music.streamx.skins.namida

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.ui.skins.rememberSkinArtworkColor
import com.music.streamx.ui.skins.skinPlayerSettings
import kotlin.math.abs

/**
 * The colour of whatever is playing. Namida tints the whole app by it, not just
 * the player, and the theme can't see the player — so the mini player and the
 * player, which can, leave it here for [namidaColors] to read.
 */
internal object NamidaNowPlaying {
    var seed by mutableStateOf<Color?>(null)
}

/** Reports [url]'s colour as the app's, once it is known. */
@Composable
internal fun PublishArtworkSeed(url: String?) {
    val seed = rememberSkinArtworkColor(url)
    LaunchedEffect(seed) { if (seed != null) NamidaNowPlaying.seed = seed }
}

// Namida's own defaults when nothing has played: warm tan, deep brown at night.
private val DefaultLight = Color(0xFFB89D7D)
private val DefaultDark = Color(0xFF43362B)
private val PitchGrey = Color(0xFF232323)

/** The tones every Namida surface is mixed from, worked out once per colour. */
internal data class NamidaTones(
    val dark: Boolean,
    /** The track colour as Namida uses it: settled to a mid lightness, part transparent. */
    val main: Color,
    val background: Color,
    /** Cards, the floating bars, tiles. */
    val card: Color,
    /** The raised card and the selected chip. */
    val cardRaised: Color,
    val onBackground: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    /** The pill behind a selected tab. */
    val indicator: Color,
    /** Accents that sit on [background]: active icons, the playing row. */
    val accent: Color,
) {
    /** The mini player's wash, top to bottom, laid over [background]. */
    val panelTop: Color get() = onBackground.copy(alpha = .39f).compositeOver(main.copy(alpha = 1f)).copy(alpha = main.alpha).compositeOver(background)
    val panelBottom: Color get() = onBackground.copy(alpha = .16f).compositeOver(main.copy(alpha = 1f)).copy(alpha = main.alpha).compositeOver(background)
    /** Namida's dialog card: a shade off the page, in the light theme a touch whiter. */
    val dialog: Color get() = if (dark) main.copy(alpha = .1f).compositeOver(Color(0xFF1B1A1A)) else main.copy(alpha = .1f).compositeOver(Color(0xFFFBF8F6))
    /** The top of the open player: the page, with the track colour washed in. */
    val playerTop: Color get() = main.copy(alpha = if (dark) .42f else .38f).compositeOver(background)
}

internal fun namidaTones(seed: Color?, dark: Boolean): NamidaTones {
    val base = seed ?: if (dark) DefaultDark else DefaultLight
    // Namida settles every track colour to 40% lightness, then lets the page
    // show through it — more in the light theme, where it would shout.
    val main = settled(base, .4f).copy(alpha = if (dark) .78f else .47f)
    fun mainAt(opacity: Float) = main.copy(alpha = opacity * .8f)
    val solid = main.copy(alpha = 1f)
    return if (dark) {
        val background = solid.copy(alpha = .1f).compositeOver(Color(0xFF121212))
        NamidaTones(true, main, background,
            card = mainAt(.14f).compositeOver(PitchGrey),
            cardRaised = mainAt(.22f).compositeOver(PitchGrey),
            onBackground = Color.White.copy(alpha = .9f),
            secondaryText = Color.White.copy(alpha = .71f),
            tertiaryText = Color.White.copy(alpha = .55f),
            indicator = solid.copy(alpha = .47f).compositeOver(Color(0xFF1A1A1A)),
            accent = settled(base, .7f))
    } else {
        NamidaTones(false, main,
            background = base.copy(alpha = .24f * .8f).compositeOver(Color.White),
            card = mainAt(.14f).compositeOver(Color.White),
            cardRaised = mainAt(.28f).compositeOver(Color.White),
            onBackground = Color.Black.copy(alpha = .8f),
            secondaryText = Color.Black.copy(alpha = .59f),
            tertiaryText = Color.Black.copy(alpha = .45f),
            indicator = solid.copy(alpha = .3f).compositeOver(Color.White),
            accent = settled(base, .32f))
    }
}

/** [color] at [lightness] in HSL, unless it's near black or white, which Namida leaves alone. */
internal fun settled(color: Color, lightness: Float): Color {
    val l = color.luminance()
    if (l <= .1f && lightness >= .4f && color.saturation() < .2f) return color
    if (l >= .9f) return color
    val (h, s, _) = color.hsl()
    return hslColor(h, s, lightness)
}

private fun Color.saturation() = hsl().second

private fun Color.hsl(): Triple<Float, Float, Float> {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val l = (max + min) / 2f
    if (max == min) return Triple(0f, 0f, l)
    val d = max - min
    val s = if (l > .5f) d / (2f - max - min) else d / (max + min)
    val h = when (max) {
        red -> ((green - blue) / d + (if (green < blue) 6f else 0f))
        green -> ((blue - red) / d + 2f)
        else -> ((red - green) / d + 4f)
    } * 60f
    return Triple(h, s, l)
}

private fun hslColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
}

internal val LocalNamidaTones = staticCompositionLocalOf { namidaTones(null, true) }

/** The theme's colours, eased over to each new track's the way Namida's theme animates. */
@Composable
internal fun namidaColors(dark: Boolean): ColorScheme {
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val target = NamidaNowPlaying.seed ?: if (dark) DefaultDark else DefaultLight
    val seed by animateColorAsState(target, tween(if (reduce) 0 else 400), label = "namidaTheme")
    return scheme(namidaTones(seed, dark))
}

internal fun scheme(t: NamidaTones): ColorScheme = if (t.dark) darkColorScheme(
    primary = t.accent, onPrimary = Color(0xFF141414),
    primaryContainer = t.indicator, onPrimaryContainer = t.onBackground,
    secondary = t.accent, onSecondary = Color(0xFF141414),
    secondaryContainer = t.indicator, onSecondaryContainer = t.onBackground,
    tertiary = t.accent,
    background = t.background, onBackground = t.onBackground,
    surface = t.background, onSurface = t.onBackground,
    surfaceVariant = t.card, onSurfaceVariant = t.secondaryText,
    surfaceContainerLowest = t.background, surfaceContainerLow = t.card, surfaceContainer = t.card,
    surfaceContainerHigh = t.cardRaised, surfaceContainerHighest = t.cardRaised,
    surfaceTint = Color.Transparent,
    outline = t.tertiaryText, outlineVariant = t.onBackground.copy(alpha = .12f),
) else lightColorScheme(
    primary = t.accent, onPrimary = Color.White,
    primaryContainer = t.indicator, onPrimaryContainer = t.onBackground,
    secondary = t.accent, onSecondary = Color.White,
    secondaryContainer = t.indicator, onSecondaryContainer = t.onBackground,
    tertiary = t.accent,
    background = t.background, onBackground = t.onBackground,
    surface = t.background, onSurface = t.onBackground,
    surfaceVariant = t.card, onSurfaceVariant = t.secondaryText,
    surfaceContainerLowest = Color.White, surfaceContainerLow = t.card, surfaceContainer = t.card,
    surfaceContainerHigh = t.cardRaised, surfaceContainerHighest = t.cardRaised,
    surfaceTint = Color.Transparent,
    outline = t.tertiaryText, outlineVariant = t.onBackground.copy(alpha = .12f),
)

/** Lexend Deca in all four of the weights Namida draws with, read from this skin's own APK. */
internal object NamidaFonts {
    private var family: FontFamily? = null

    fun family(context: Context): FontFamily = family ?: runCatching {
        val assets = context.createPackageContext("com.music.streamx.skins.namida", 0).assets
        FontFamily(
            Font("streamx/fonts/LexendDeca-Regular.ttf", assets, FontWeight.Normal),
            Font("streamx/fonts/LexendDeca-Medium.ttf", assets, FontWeight.Medium),
            Font("streamx/fonts/LexendDeca-SemiBold.ttf", assets, FontWeight.SemiBold),
            Font("streamx/fonts/LexendDeca-Bold.ttf", assets, FontWeight.Bold),
        )
    }.getOrDefault(FontFamily.Default).also { family = it }
}

internal fun namidaTypography(family: FontFamily): Typography {
    fun style(size: Int, weight: FontWeight, line: Float = 1.3f) =
        TextStyle(fontFamily = family, fontWeight = weight, fontSize = size.sp, lineHeight = (size * line).sp, letterSpacing = 0.sp)
    return Typography(
        displayLarge = style(34, FontWeight.SemiBold, 1.2f), displayMedium = style(30, FontWeight.SemiBold, 1.2f),
        displaySmall = style(26, FontWeight.SemiBold, 1.2f),
        headlineLarge = style(26, FontWeight.SemiBold, 1.2f), headlineMedium = style(22, FontWeight.SemiBold, 1.25f),
        headlineSmall = style(20, FontWeight.SemiBold, 1.25f),
        titleLarge = style(20, FontWeight.SemiBold), titleMedium = style(16, FontWeight.SemiBold), titleSmall = style(14, FontWeight.SemiBold),
        bodyLarge = style(15, FontWeight.Medium, 1.4f), bodyMedium = style(14, FontWeight.Normal, 1.4f), bodySmall = style(12, FontWeight.Normal, 1.35f),
        labelLarge = style(14, FontWeight.Medium), labelMedium = style(12, FontWeight.Medium), labelSmall = style(11, FontWeight.Medium),
    )
}

internal val NamidaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(28.dp),
)

/** Everything the skin composes sits under this: Namida's type, shapes, and its tones as a local. */
@Composable
internal fun NamidaThemeScope(colors: ColorScheme, content: @Composable () -> Unit) {
    val dark = colors.background.luminance() < .5f
    val family = NamidaFonts.family(LocalContext.current)
    val tones = tonesFor(colors, dark)
    MaterialTheme(colorScheme = colors, typography = namidaTypography(family), shapes = NamidaShapes) {
        androidx.compose.runtime.CompositionLocalProvider(LocalNamidaTones provides tones, content = content)
    }
}

/** The tones behind [colors], re-derived from the same seed rather than guessed back from the scheme. */
@Composable
private fun tonesFor(colors: ColorScheme, dark: Boolean): NamidaTones {
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val target = NamidaNowPlaying.seed ?: if (dark) DefaultDark else DefaultLight
    val seed by animateColorAsState(target, tween(if (reduce) 0 else 400), label = "namidaTones")
    return namidaTones(seed, dark)
}
