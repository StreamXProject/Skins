package com.music.streamx.skins.namida

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Song
import com.music.streamx.data.model.artworkAt
import com.music.streamx.ui.skins.SkinIcon
import com.music.streamx.ui.skins.SkinMenuAction
import com.music.streamx.ui.skins.skinPlayerSettings

/**
 * Namida's dialogs: a card in the middle of the screen with everything behind
 * it blurred and dimmed, popping in with a little overshoot. Android only blurs
 * behind a window from 12 on; earlier, the dim alone has to push the page back.
 */
@Composable
internal fun NamidaDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        val blur = with(LocalDensity.current) { 22.dp.roundToPx() }
        DisposableEffect(window) {
            window?.let {
                it.setDimAmount(if (Build.VERSION.SDK_INT >= 31) .38f else .6f)
                if (Build.VERSION.SDK_INT >= 31) {
                    it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    it.attributes = it.attributes.apply { blurBehindRadius = blur }
                }
            }
            onDispose { }
        }
        val pop = remember { Animatable(if (reduce) 1f else 0f) }
        LaunchedEffect(Unit) { pop.animateTo(1f, if (reduce) tween(0) else spring(dampingRatio = .72f, stiffness = 520f)) }
        val t = LocalNamidaTones.current
        Box(Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.Center) {
            Column(Modifier.padding(horizontal = 26.dp).widthIn(max = 460.dp).fillMaxWidth()
                .heightIn(max = (LocalConfiguration.current.screenHeightDp * .82f).dp)
                .graphicsLayer {
                    val p = pop.value
                    alpha = p.coerceIn(0f, 1f)
                    scaleX = .9f + .1f * p
                    scaleY = .9f + .1f * p
                }
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(alpha = .4f))
                .clip(RoundedCornerShape(28.dp))
                .background(t.dialog)
                // Taps on the card stay on the card.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                content = content)
        }
    }
}

/** Namida's track menu: the track at the head, the rows, then Play Next and Play Last side by side. */
@Composable
internal fun NamidaSongMenu(song: Song, onDismiss: () -> Unit, onOpenTrack: (() -> Unit)?, primary: List<SkinMenuAction>,
    rows: @Composable ColumnScope.() -> Unit) {
    val t = LocalNamidaTones.current
    NamidaDialog(onDismiss) {
        Row(Modifier.fillMaxWidth().then(if (onOpenTrack != null) Modifier.clickable { onDismiss(); onOpenTrack() } else Modifier)
            .padding(start = 22.dp, end = 16.dp, top = 22.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(84.dp).shadow(8.dp, RoundedCornerShape(14.dp)).clip(RoundedCornerShape(14.dp)).background(t.card)) {
                AsyncImage(song.thumbnailUrl.artworkAt(240), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Column(Modifier.weight(1f).padding(start = 16.dp, end = 8.dp)) {
                Text(song.artist, color = t.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.title, color = t.secondaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                song.albumName?.let { Text(it, color = t.tertiaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
            if (onOpenTrack != null) SkinIcon(NamidaIcons.ChevronRight, "Open album", Modifier.size(22.dp), tint = t.secondaryText)
        }
        Hairline()
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(vertical = 8.dp), content = rows)
        if (primary.isNotEmpty()) {
            Hairline()
            Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                primary.forEachIndexed { index, action ->
                    if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical = 14.dp).background(t.onBackground.copy(alpha = .12f)))
                    Row(Modifier.weight(1f).fillMaxHeight().clickable { action.onClick(); onDismiss() }.padding(horizontal = 22.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        SkinIcon(if (index == 0) NamidaIcons.Next else NamidaIcons.Queue, null, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .8f))
                        Text(if (index == 0) "Play Next" else "Play Last", Modifier.padding(start = 14.dp), color = t.onBackground,
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
internal fun NamidaMenuRow(icon: ImageVector, label: String, detail: String?, value: String?, tint: Color?, enabled: Boolean,
    loading: Boolean, onClick: (() -> Unit)?) {
    val t = LocalNamidaTones.current
    val alpha = if (enabled) 1f else .4f
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled && onClick != null) { onClick?.invoke() }
        .padding(horizontal = 22.dp, vertical = if (detail != null) 10.dp else 14.dp), verticalAlignment = Alignment.CenterVertically) {
        SkinIcon(namidaIcon(icon), null, Modifier.size(24.dp), tint = (tint ?: t.onBackground.copy(alpha = .8f)).copy(alpha = alpha))
        Column(Modifier.weight(1f).padding(start = 18.dp)) {
            Text(label, color = t.onBackground.copy(alpha = alpha), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            detail?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = t.secondaryText.copy(alpha = alpha), fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        when {
            loading -> CircularProgressIndicator(Modifier.size(16.dp), color = t.secondaryText, strokeWidth = 2.dp)
            value != null -> Text(value, color = t.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun NamidaMenuSeparator() {
    Spacer(Modifier.height(6.dp))
}

@Composable
internal fun Hairline() {
    val t = LocalNamidaTones.current
    Box(Modifier.fillMaxWidth().height(1.dp).background(t.onBackground.copy(alpha = .08f)))
}

/** Namida's confirmation: the same card, the host's buttons laid out along its foot. */
@Composable
internal fun NamidaAlert(onDismiss: () -> Unit, confirm: @Composable () -> Unit, dismiss: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?, text: (@Composable () -> Unit)?) {
    val t = LocalNamidaTones.current
    NamidaDialog(onDismiss) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides t.onBackground,
            androidx.compose.material3.LocalTextStyle provides androidx.compose.ui.text.TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium,
                fontFamily = androidx.compose.material3.MaterialTheme.typography.bodyLarge.fontFamily)) {
            title?.let {
                Row(Modifier.fillMaxWidth().background(t.card).padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    SkinIcon(NamidaIcons.Info, null, Modifier.size(24.dp), tint = t.onBackground.copy(alpha = .8f))
                    Box(Modifier.padding(start = 14.dp)) {
                        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalTextStyle provides
                            androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)) { it() }
                    }
                }
            }
            text?.let { Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp)) { it() } }
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp), horizontalArrangement =
                androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                dismiss?.invoke()
                confirm()
            }
        }
    }
}

/** Namida's pick-one dialog: each choice a row, the chosen one on a tinted pill with a check. */
@Composable
internal fun NamidaOptions(title: String, subtitle: String?, options: List<com.music.streamx.ui.skins.SkinOption>, selected: Int,
    onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    val t = LocalNamidaTones.current
    NamidaDialog(onDismiss) {
        Column(Modifier.fillMaxWidth().background(t.card).padding(horizontal = 22.dp, vertical = 18.dp)) {
            Text(title, color = t.onBackground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text("⤷ $it", color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
        }
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(10.dp)) {
            options.forEachIndexed { index, option ->
                val on = index == selected
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (on) t.indicator.copy(alpha = .6f) else Color.Transparent)
                    .clickable { onSelect(index); onDismiss() }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(option.label, color = t.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        option.detail?.let { Text(it, color = t.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                    }
                    if (on) SkinIcon(NamidaIcons.Check, "Selected", Modifier.size(22.dp), tint = t.accent)
                }
            }
        }
    }
}
