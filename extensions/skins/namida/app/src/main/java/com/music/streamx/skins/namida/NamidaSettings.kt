package com.music.streamx.skins.namida

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.streamx.ui.skins.LocalSkinSettingsGroupKey
import com.music.streamx.ui.skins.SkinIcon

// ---------------------------------------------------------------- page

/** Namida's settings bar: back, the title, and a search that opens in place. */
@Composable
internal fun NamidaSettingsPage(query: String, onQuery: (String) -> Unit, padding: PaddingValues, onBack: () -> Unit, content: @Composable () -> Unit) {
    val t = LocalNamidaTones.current
    var searching by rememberSaveable { mutableStateOf(query.isNotEmpty()) }
    var headerHeight by remember { mutableIntStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    Box(Modifier.fillMaxSize().background(t.background)) {
        // Runs under the header rather than stopping at it, so cards slide
        // beneath the bar and fade out instead of being cut along its edge.
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(top = with(density) { headerHeight.toDp() } + 4.dp, bottom = padding.calculateBottomPadding() + 24.dp)) {
            content()
        }
        Column(Modifier.fillMaxWidth()
            .onSizeChanged { headerHeight = it.height }
            .drawBehind {
                val fade = 28.dp.toPx()
                val total = size.height + fade
                drawRect(Brush.verticalGradient(0f to t.background, size.height / total to t.background,
                    (size.height + fade * .5f) / total to t.background.copy(alpha = .7f), 1f to t.background.copy(alpha = 0f),
                    endY = total), size = androidx.compose.ui.geometry.Size(size.width, total))
            }) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconCircle(NamidaIcons.Back, "Back", onBack)
                Text("Settings", Modifier.weight(1f).padding(start = 6.dp), color = t.onBackground, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                IconCircle(if (searching) NamidaIcons.Close else NamidaIcons.Search, if (searching) "Close search" else "Search settings") {
                    if (searching) onQuery("")
                    searching = !searching
                }
            }
            AnimatedVisibility(searching, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                NamidaTextField(query, onQuery, "Search settings", true, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }
    }
}

@Composable
private fun IconCircle(icon: ImageVector, label: String, onClick: () -> Unit) {
    val t = LocalNamidaTones.current
    Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        SkinIcon(icon, label, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .85f))
    }
}

// ---------------------------------------------------------------- groups and rows

private fun groupIcon(key: String?, header: String?): ImageVector {
    val name = (key ?: header ?: "").lowercase()
    return when {
        "account" in name -> NamidaIcons.Account
        "quality" in name || "audio" in name -> NamidaIcons.Headphones
        "download" in name -> NamidaIcons.Download
        "playback" in name -> NamidaIcons.Play
        "appearance" in name || "theme" in name -> NamidaIcons.Edit
        "performance" in name -> NamidaIcons.Equalizer
        "local" in name || "storage" in name -> NamidaIcons.Folder
        "library" in name -> NamidaIcons.Library
        "data" in name -> NamidaIcons.History
        "language" in name -> NamidaIcons.Translate
        "advanced" in name -> NamidaIcons.Settings
        else -> NamidaIcons.Info
    }
}

/**
 * A group as Namida draws a settings page: one rounded card, headed by a
 * lighter band with the section's icon and name and the "⤷" line under it.
 */
@Composable
internal fun NamidaSettingsGroup(header: String?, footer: String?, topSpacing: Dp, content: @Composable () -> Unit) {
    val t = LocalNamidaTones.current
    val key = LocalSkinSettingsGroupKey.current
    Column(Modifier.padding(start = 12.dp, end = 12.dp, top = (topSpacing.value * .55f).dp).fillMaxWidth()
        .clip(RoundedCornerShape(28.dp)).background(t.card)) {
        if (header != null) {
            Row(Modifier.fillMaxWidth().background(t.cardRaised.copy(alpha = .55f)).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                SkinIcon(groupIcon(key, header), null, Modifier.size(28.dp), tint = t.onBackground.copy(alpha = .8f))
                Column(Modifier.padding(start = 16.dp)) {
                    Text(header, color = t.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    footer?.let { Text("⤷ $it", color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2) }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { content() }
        if (header == null && footer != null) Text(footer, Modifier.padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
            color = t.tertiaryText, fontSize = 12.sp)
    }
}

@Composable
internal fun NamidaSettingsRow(icon: ImageVector?, iconPainter: Painter?, title: String, subtitle: String?, subtitleContent: (@Composable () -> Unit)?,
    value: String?, badge: String?, enabled: Boolean, onClick: (() -> Unit)?, trailing: (@Composable () -> Unit)?) {
    val t = LocalNamidaTones.current
    val alpha = if (enabled) 1f else .45f
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
        .padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        when {
            icon != null -> SkinIcon(namidaIcon(icon), null, Modifier.size(26.dp), tint = t.onBackground.copy(alpha = .8f * alpha))
            iconPainter != null -> SkinIcon(iconPainter, null, Modifier.size(26.dp), tint = t.onBackground.copy(alpha = .8f * alpha))
        }
        Column(Modifier.weight(1f).padding(start = if (icon != null || iconPainter != null) 18.dp else 0.dp, end = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = t.onBackground.copy(alpha = alpha), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                badge?.let {
                    Text(it, Modifier.padding(start = 8.dp).clip(RoundedCornerShape(6.dp)).background(t.indicator).padding(horizontal = 6.dp, vertical = 1.dp),
                        color = t.onBackground, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            subtitle?.let { Text(it, color = t.secondaryText.copy(alpha = alpha), fontSize = 14.sp, fontWeight = FontWeight.Normal) }
            subtitleContent?.invoke()
        }
        value?.let { Text(it, Modifier.padding(end = if (trailing == null && onClick != null) 6.dp else 0.dp), color = t.onBackground.copy(alpha = .85f * alpha),
            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
        when {
            trailing != null -> trailing()
            onClick != null -> SkinIcon(NamidaIcons.ChevronRight, null, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .7f * alpha))
        }
    }
}

// ---------------------------------------------------------------- controls

/** Namida's switch: a dark pill whose white knob slides over, the track glowing in the colour when on. */
@Composable
internal fun NamidaToggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean, modifier: Modifier) {
    val t = LocalNamidaTones.current
    val on by animateFloatAsState(if (checked) 1f else 0f, spring(dampingRatio = .7f, stiffness = 600f), label = "namidaToggle")
    val track by animateColorAsState(if (checked) t.main.copy(alpha = .55f) else t.onBackground.copy(alpha = .08f), tween(200), label = "namidaToggleTrack")
    Box(modifier.size(52.dp, 30.dp)
        .then(if (onCheckedChange != null) Modifier.clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null) {
            onCheckedChange(!checked) } else Modifier)
        .drawBehind {
            val alpha = if (enabled) 1f else .45f
            if (on > 0f) drawRoundRect(t.main.copy(alpha = .35f * on * alpha), Offset(-3.dp.toPx(), -1.dp.toPx()),
                androidx.compose.ui.geometry.Size(size.width + 6.dp.toPx(), size.height + 4.dp.toPx()), CornerRadius(size.height))
            drawRoundRect(track.copy(alpha = track.alpha * alpha), cornerRadius = CornerRadius(size.height / 2))
            val r = size.height * .34f
            val x = size.height / 2 + (size.width - size.height) * on
            drawCircle(Color.White.copy(alpha = .92f * alpha), r, Offset(x, size.height / 2))
        })
}

/** Namida's segmented control: a light plate, the chosen segment a dark block on it. */
@Composable
internal fun NamidaSegmented(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier) {
    val t = LocalNamidaTones.current
    BoxWithConstraints(modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(16.dp))
        .background(t.onBackground.copy(alpha = if (t.dark) .8f else .12f)).padding(5.dp)) {
        val count = options.size.coerceAtLeast(1)
        val width = maxWidth / count
        val at by animateFloatAsState(selectedIndex.toFloat(), spring(dampingRatio = .8f, stiffness = 500f), label = "namidaSegment")
        Box(Modifier.offset(x = width * at).size(width, maxHeight).clip(RoundedCornerShape(12.dp)).background(if (t.dark) t.background else Color.White))
        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { index, label ->
                val on = index == selectedIndex
                Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { onSelect(index) }, contentAlignment = Alignment.Center) {
                    Text(label, color = when {
                        on -> t.onBackground
                        t.dark -> t.background.copy(alpha = .75f)
                        else -> t.secondaryText
                    }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
internal fun NamidaSlider(value: Float, onValueChange: (Float) -> Unit, range: ClosedFloatingPointRange<Float>, steps: Int, enabled: Boolean, modifier: Modifier) {
    val t = LocalNamidaTones.current
    fun snap(fraction: Float): Float {
        val raw = range.start + (range.endInclusive - range.start) * fraction.coerceIn(0f, 1f)
        if (steps <= 0) return raw
        val step = (range.endInclusive - range.start) / (steps + 1)
        return (range.start + Math.round((raw - range.start) / step) * step).coerceIn(range.start, range.endInclusive)
    }
    val fraction = if (range.endInclusive > range.start) ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f) else 0f
    Box(modifier.fillMaxWidth().height(36.dp)
        .pointerInput(enabled, range, steps) {
            if (!enabled) return@pointerInput
            detectTapGestures { onValueChange(snap(it.x / size.width)) }
        }
        .pointerInput(enabled, range, steps) {
            if (!enabled) return@pointerInput
            detectDragGestures { change, _ -> change.consume(); onValueChange(snap(change.position.x / size.width)) }
        }
        .drawBehind {
            val y = size.height / 2
            val h = 6.dp.toPx()
            drawRoundRect(t.onBackground.copy(alpha = .12f), Offset(0f, y - h / 2), androidx.compose.ui.geometry.Size(size.width, h), CornerRadius(h))
            drawRoundRect(t.accent.copy(alpha = if (enabled) .85f else .4f), Offset(0f, y - h / 2), androidx.compose.ui.geometry.Size(size.width * fraction, h), CornerRadius(h))
            drawRoundRect(t.accent, Offset(size.width * fraction - 3.dp.toPx(), y - 11.dp.toPx()), androidx.compose.ui.geometry.Size(6.dp.toPx(), 22.dp.toPx()), CornerRadius(3.dp.toPx()))
        })
}

@Composable
internal fun NamidaTextField(value: String, onValueChange: (String) -> Unit, placeholder: String?, singleLine: Boolean, modifier: Modifier) {
    val t = LocalNamidaTones.current
    val family = androidx.compose.material3.MaterialTheme.typography.bodyLarge.fontFamily
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(t.card)
        .border(1.dp, t.onBackground.copy(alpha = .1f), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 13.dp)) {
        if (value.isEmpty() && placeholder != null) Text(placeholder, color = t.tertiaryText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        BasicTextField(value, onValueChange, Modifier.fillMaxWidth(), singleLine = singleLine,
            textStyle = TextStyle(color = t.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium, fontFamily = family),
            cursorBrush = SolidColor(t.accent))
    }
}
