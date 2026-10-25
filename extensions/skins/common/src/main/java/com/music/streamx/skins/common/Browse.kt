package com.music.streamx.skins.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.streamx.data.model.DetailPage
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.skins.common.icons.*
import com.music.streamx.ui.skins.LocalSkinLayout

/*
 * The browse pages — an artist, a shelf's "More" page, a "Show all" grid —
 * drawn with the same cards and headings as the skin's home feed, so leaving
 * the feed no longer drops you into the app's stock look.
 */

/** The heading voice a [ShelfStyle] uses on the feed, reused for browse pages. */
@Composable
internal fun styledHeadingText(style: SkinStyle): androidx.compose.ui.text.TextStyle {
    val t = MaterialTheme.typography.titleLarge
    return when (style.shelf) {
        ShelfStyle.LCD, ShelfStyle.STRIP, ShelfStyle.LIST -> MaterialTheme.typography.labelLarge.copy(fontFamily = skinFont(FontFamily.Monospace))
        ShelfStyle.TILES -> t.copy(fontFamily = skinFont(FontFamily.Serif))
        ShelfStyle.PRINTS -> t.copy(fontFamily = skinFont(FontFamily.Cursive), fontStyle = FontStyle.Italic)
        else -> t
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SkinBrowseCard(style: SkinStyle, item: ShelfItem, index: Int, fill: Boolean, onClick: () -> Unit, onLongPress: (() -> Unit)?, modifier: Modifier) {
    val layout = LocalSkinLayout.current
    val click = modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress)
    if (style.shelf == ShelfStyle.LIST) {
        // A list skin's feed is rows; on a grid page each cell is a small framed tile.
        val c = MaterialTheme.colorScheme
        Column(click.then(if (fill) Modifier.fillMaxWidth() else Modifier.width(layout.shelfWidthDp.dp))) {
            AsyncImage(item.thumbnailUrl, null, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(layout.artworkRadiusDp.dp)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            Text(item.title, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        return
    }
    // Leaning prints need room to lean inside a grid cell.
    val inset = if (fill && style.shelf == ShelfStyle.PRINTS) Modifier.padding(6.dp) else Modifier
    Box(inset) { ShelfCard(style, item, index, layout, click, fill) }
}

@Composable
internal fun SkinBrowseSectionHeading(style: SkinStyle, title: String, subtitle: String?, onShowAll: (() -> Unit)?) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = styledHeadingText(style), color = if (style.shelf == ShelfStyle.LCD || style.shelf == ShelfStyle.STRIP) c.primary else c.onBackground,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        onShowAll?.let { TextButton(it) { Text("More") } }
    }
}

@Composable
internal fun SkinBrowsePageTitle(style: SkinStyle, title: String, subtitle: String?, topPadding: Dp) {
    val c = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top = topPadding).padding(horizontal = 20.dp).padding(bottom = 14.dp)) {
        Text(style.brand.uppercase(), style = MaterialTheme.typography.labelMedium, color = c.primary)
        Text(title, style = styledHeadingText(style).copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize, lineHeight = MaterialTheme.typography.headlineMedium.lineHeight),
            color = c.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) Text(subtitle, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Box(Modifier.padding(top = 12.dp).width(48.dp).height(3.dp).clip(CircleShape).background(c.secondary))
    }
}

/** The artist: photo in the skin's frame, the name, then play / shuffle / follow. */
@Composable
internal fun SkinArtistHeader(style: SkinStyle, page: DetailPage, onPlay: (() -> Unit)?, onShuffle: (() -> Unit)?,
    onToggleSubscription: (() -> Unit)?, topPadding: Dp) {
    val c = MaterialTheme.colorScheme
    val photo = Modifier.size(196.dp)
    Column(Modifier.fillMaxWidth().padding(top = topPadding + 8.dp).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (style.shelf) {
            ShelfStyle.PRINTS -> Column(Modifier.rotate(-2f).shadow(10.dp, RoundedCornerShape(3.dp)).background(printPaper(), RoundedCornerShape(3.dp)).padding(10.dp, 10.dp, 10.dp, 34.dp)) {
                AsyncImage(page.thumbnailUrl, null, photo.background(c.surfaceVariant), contentScale = ContentScale.Crop)
            }
            ShelfStyle.DISCS -> AsyncImage(page.thumbnailUrl, null, photo.clip(CircleShape).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            else -> AsyncImage(page.thumbnailUrl, null, photo.shadow(12.dp, RoundedCornerShape(style.radius)).clip(RoundedCornerShape(style.radius)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
        }
        Text(page.title, Modifier.padding(top = 18.dp), style = styledHeadingText(style).copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            lineHeight = MaterialTheme.typography.headlineMedium.lineHeight), color = c.onBackground, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        val meta = listOfNotNull(page.subscriberCountText, page.subtitle.takeIf { it.isNotBlank() }).firstOrNull()
        if (meta != null) Text(meta, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Button({ onPlay?.invoke() }, Modifier.height(48.dp), enabled = onPlay != null, shape = RoundedCornerShape(style.radius)) {
                Icon(SkinIcons.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Play")
            }
            FilledTonalIconButton({ onShuffle?.invoke() }, Modifier.size(48.dp), enabled = onShuffle != null, shape = RoundedCornerShape(style.radius),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = c.secondary.copy(alpha = .22f), contentColor = c.onBackground)) {
                Icon(SkinIcons.Shuffle, "Shuffle")
            }
            if (onToggleSubscription != null) {
                val following = page.subscription?.subscribed == true
                OutlinedButton(onToggleSubscription, Modifier.height(48.dp), shape = RoundedCornerShape(style.radius)) {
                    if (following) { Icon(SkinIcons.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
                    Text(if (following) "Following" else "Follow")
                }
            }
        }
    }
}
