package com.music.streamx.skins.common

import com.music.streamx.skins.common.icons.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import coil3.compose.AsyncImage
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.lyrics.LyricsSource
import com.music.streamx.data.model.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.*

/**
 * How a skin wants the shared chrome drawn. Everything a skin does not override
 * here comes out tidy and neutral, so a skin's effort goes into its player.
 */
data class SkinStyle(
    /** Shown in headers and the player eyebrow. */
    val brand: String,
    /** Corner radius for cards, artwork and buttons in the shared chrome. */
    val radius: Dp = 16.dp,
    /** The home greeting under the brand line. */
    val greeting: String = "What are we playing today?",
    /** How the home feed's cards are drawn. */
    val shelf: ShelfStyle = ShelfStyle.CARDS,
    /** Serif lyrics, for the paper and vinyl kinds of skin. */
    val serifLyrics: Boolean = false,
    /** Names this skin's saved appearance settings. */
    val id: String = brand.lowercase().replace(' ', '-'),
    /** The secondary colour before the listener picks one; null follows the accent. */
    val defaultSecondary: Color? = null,
    /** What the secondary colour paints in this skin, shown in its settings. */
    val secondaryHint: String = "Highlights, chips and the small decorations around covers.",
    /** What the signature font is used for in this skin, shown in its settings. */
    val signatureHint: String = "The skin's lettering: captions, readouts and headings in its own style.",
)

/**
 * Everything a skin's now-playing stage needs to draw, pulled from the forty
 * parameters the host hands [SkinUiRenderer.Player] into one object.
 */
class NowPlayingModel(
    val song: Song,
    val artwork: String?,
    val isPlaying: Boolean,
    val isLoading: Boolean,
    val position: PlaybackPosition,
    /** Total length in ms; falls back to the catalogue's text when the player has not reported one. */
    val durationMs: Long,
    val queue: List<Song>,
    val queueIndex: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val repeatMode: Int,
    val shuffleEnabled: Boolean,
    val liked: Boolean,
    val locked: Boolean,
    val trackFormat: String,
    val reduceMotion: Boolean,
    val compact: Boolean,
) {
    val fraction: Float get() = if (durationMs > 0) (position.positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

/** The actions a stage may fire. Locked controls are already routed to the host's "blocked" notice. */
class NowPlayingActions(
    val playPause: () -> Unit,
    val next: () -> Unit,
    val previous: () -> Unit,
    val seekFraction: (Float) -> Unit,
    val toggleLike: () -> Unit,
    val toggleShuffle: () -> Unit,
    val cycleRepeat: () -> Unit,
    val openMenu: () -> Unit,
    val openOutput: () -> Unit,
    val openArtist: () -> Unit,
)

/**
 * A complete [SkinUiRenderer] whose only abstract part is the now-playing stage.
 *
 * Lyrics, queue, the top bar, sheets and dialogs, the mini player, home and
 * release headers, navigation, library rows and feed shelves are all here and
 * follow the palette the skin's descriptor declares. A skin subclasses this,
 * names its [style] and draws [NowPlaying].
 */
abstract class BaseSkinRenderer(val style: SkinStyle) : SkinUiRenderer, SkinLibraryProvider, SkinLibraryChrome, SkinSettingsProvider, SkinHomeHeaderChrome,
    SkinThemeProvider, SkinUseHostTheme, SkinCompositionProvider, SkinBrowsePagesProvider {

    override val apiVersion = SKIN_UI_API_VERSION

    override fun verifyCompatibility() {
        check(listOf(SkinIcons.PlayArrow, SkinIcons.Pause, SkinIcons.SkipNext, SkinIcons.SkipPrevious,
            SkinIcons.Favorite, SkinIcons.Shuffle, SkinIcons.Repeat).all { it.root.size > 0 })
    }

    override fun mapIcon(vector: ImageVector): ImageVector = vector
    override fun icon(name: String): ImageVector? = null

    // ── Appearance. The host builds the scheme and type from skin.json (SkinUseHostTheme);
    //    Theme lays the listener's accent, secondary and fonts over them. ──

    @Composable override fun Colors(darkTheme: Boolean): ColorScheme = MaterialTheme.colorScheme
    override fun typography(): Typography = Typography()
    override fun shapes(): Shapes = Shapes()

    /** Opened from Skins → this skin → Settings. */
    @Composable override fun Configuration(onDismiss: () -> Unit) = AppearanceSettings(style, onDismiss)

    @Composable
    override fun Theme(colors: ColorScheme, typography: Typography, shapes: Shapes, content: @Composable () -> Unit) {
        val context = LocalContext.current
        remember(style.id) { SkinAppearance.load(context.applicationContext, style.id); true }
        SkinAppearance.skinAccent = colors.primary
        val type = SkinAppearance.textFont.family?.let { typography.withFamily(it) } ?: typography
        MaterialTheme(colorScheme = colors.withAppearance(style), shapes = shapes, typography = type, content = content)
    }

    // ── Browse pages: artist, "More" and "Show all", in the feed's own cards. ──

    @Composable override fun BrowseCard(item: ShelfItem, index: Int, fill: Boolean, onClick: () -> Unit, onLongPress: (() -> Unit)?, modifier: Modifier) =
        SkinBrowseCard(style, item, index, fill, onClick, onLongPress, modifier)

    @Composable override fun BrowseSectionHeading(title: String, subtitle: String?, onShowAll: (() -> Unit)?) =
        SkinBrowseSectionHeading(style, title, subtitle, onShowAll)

    @Composable override fun BrowsePageTitle(title: String, subtitle: String?, topPadding: Dp) =
        SkinBrowsePageTitle(style, title, subtitle, topPadding)

    @Composable override fun ArtistHeader(page: DetailPage, onPlay: (() -> Unit)?, onShuffle: (() -> Unit)?, onToggleSubscription: (() -> Unit)?, topPadding: Dp) =
        SkinArtistHeader(style, page, onPlay, onShuffle, onToggleSubscription, topPadding)

    /** The one surface each skin draws itself: artwork, transport and seek for the current track. */
    @Composable
    abstract fun NowPlaying(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier)

    /** The lyrics page. Each skin draws its own; the shell supplies playhead, offset and seek. */
    @Composable
    abstract fun Lyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier)

    /** The queue page. Each skin draws its own on top of [QueueList]. */
    @Composable
    abstract fun Queue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
        onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier)

    /** The heading of the library tab: the skin owns it (see [SkinLibraryChrome]). */
    @Composable
    abstract fun LibraryHeading(onNewPlaylist: (() -> Unit)?)

    /** Optional: the mini player strip. The default is a quiet card with artwork, title and play/next. */
    @Composable
    open fun MiniPlayerContent(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float,
        onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) =
        DefaultMiniPlayer(song, artwork, playing, loading, fraction, onPlayPause, onExpand, onNext, style, modifier)

    @Composable
    override fun Player(
        song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long,
        queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean,
        repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus,
        accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit,
        onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit,
        onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit,
        onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
        onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit,
        onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit,
        onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: Dp, onBlocked: () -> Unit,
        lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean,
        lyricsProviderStates: Map<LyricsSource, LyricsProviderState>,
        onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean,
        onDismissLyricsOffset: () -> Unit, modifier: Modifier,
    ) = androidx.compose.runtime.CompositionLocalProvider(LocalQueueDragActive provides onDragActive) { PlayerShell(
        renderer = this, song = song, isPlaying = isPlaying, isLoading = isLoading, position = position, durationMs = durationMs,
        queue = queue, queueIndex = queueIndex, hasPrevious = hasPrevious, hasNext = hasNext, repeatMode = repeatMode,
        shuffleEnabled = shuffleEnabled, autoplayEnabled = autoplayEnabled, likeStatus = likeStatus, accountName = accountName,
        onToggleLike = onToggleLike, onPlayPause = onPlayPause, onNext = onNext, onPrevious = onPrevious,
        onSeekFraction = onSeekFraction, onSeek = onSeek, onToggleShuffle = onToggleShuffle, onCycleRepeat = onCycleRepeat,
        onToggleAutoplay = onToggleAutoplay, onJumpTo = onJumpTo, onRemove = onRemove, onMove = onMove, onClear = onClear,
        onOpenMenu = onOpenMenu, onOpenArtist = onOpenArtist, onListenTogether = onListenTogether, onCollapse = onCollapse,
        onOpenTimer = onOpenTimer, onBlocked = onBlocked, lyrics = lyrics, lyricsSource = lyricsSource,
        lyricsUnavailable = lyricsUnavailable, lyricsProviderStates = lyricsProviderStates,
        onSelectLyricsProvider = onSelectLyricsProvider, lyricsOffsetOpen = lyricsOffsetOpen,
        onDismissLyricsOffset = onDismissLyricsOffset, modifier = modifier,
    ) }

    @Composable
    override fun MiniPlayer(
        song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long,
        saved: Boolean, onSave: () -> Unit, onPlayPause: () -> Unit, onExpand: () -> Unit,
        onNext: () -> Unit, onPrevious: () -> Unit, locked: Boolean, onBlocked: () -> Unit, modifier: Modifier,
    ) {
        val art = rememberSkinRemoteArtwork(song)
        val total = durationMs.takeIf { it > 0 } ?: durationFromText(song.durationText)
        val fraction = if (total > 0) (position.positionMs.toFloat() / total).coerceIn(0f, 1f) else 0f
        MiniPlayerContent(song, art, playing, loading, fraction,
            { if (locked) onBlocked() else onPlayPause() }, onExpand, { if (locked) onBlocked() else onNext() }, modifier)
    }

    @Composable
    override fun Library(shelves: List<HomeShelf>, links: List<ShelfItem>, deviceItems: List<ShelfItem>, contentPadding: PaddingValues,
        onItem: (ShelfItem, String) -> Unit, onLongPress: ((ShelfItem) -> Unit)?, onShowAll: (HomeShelf) -> Unit, onNewPlaylist: (() -> Unit)?) {
        androidx.compose.foundation.lazy.LazyColumn(contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
            item(key = "library-heading") { Column(Modifier.statusBarsPadding()) { LibraryHeading(onNewPlaylist) } }
            items(links, key = { "link-${it.browseId ?: it.title}" }) { item -> LibraryRow(item, { onItem(item, "Library") }, { onLongPress?.invoke(item) }) }
            shelves.forEachIndexed { index, shelf ->
                item(key = "shelf-head-$index") { LibrarySectionHeading(shelf.title) { onShowAll(shelf) } }
                items(shelf.items.take(6), key = { "shelf-$index-${it.browseId ?: it.videoId ?: it.title}" }) { item -> LibraryRow(item, { onItem(item, shelf.title) }, { onLongPress?.invoke(item) }) }
            }
            if (deviceItems.isNotEmpty()) {
                item(key = "device-head") { LibrarySectionHeading("On this device", null) }
                items(deviceItems, key = { "device-${it.browseId ?: it.title}" }) { item -> LibraryRow(item, { onItem(item, "On device") }, { onLongPress?.invoke(item) }) }
            }
            item(key = "library-stats") { LibraryStats {} }
        }
    }

    /** A section title inside the library, in the skin's heading voice. Override for a different look. */
    @Composable
    open fun LibrarySectionHeading(title: String, onShowAll: (() -> Unit)?) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            onShowAll?.let { TextButton(it) { Text("All") } }
        }
    }

    // ── Settings, in the skin's voice; values, callbacks and persistence stay with the host. ──

    @Composable
    override fun SettingsGroup(header: String?, footer: String?, topSpacing: Dp, content: @Composable () -> Unit) {
        val c = MaterialTheme.colorScheme
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            if (header != null) SettingsHeading(header) else Spacer(Modifier.height(topSpacing))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(style.radius), color = c.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant.copy(alpha = settingsBorderAlpha()))) { Column { content() } }
            if (footer != null) Text(footer, Modifier.padding(horizontal = 6.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        }
    }

    /** The group title. Open so a skin can set it in its own type. */
    @Composable
    open fun SettingsHeading(text: String) {
        Text(text.uppercase(), Modifier.padding(start = 6.dp, top = 22.dp, bottom = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }

    /** 0 for skins whose groups float without an outline, 1 for bordered panels. */
    open fun settingsBorderAlpha(): Float = 1f

    @Composable
    override fun SettingsDivider() {
        HorizontalDivider(Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }

    @Composable
    override fun SettingsRow(icon: ImageVector?, iconPainter: androidx.compose.ui.graphics.painter.Painter?, title: String, subtitle: String?,
        subtitleContent: (@Composable () -> Unit)?, value: String?, badge: String?, enabled: Boolean, onClick: (() -> Unit)?, trailing: (@Composable () -> Unit)?) {
        val c = MaterialTheme.colorScheme
        Row(Modifier.fillMaxWidth().then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(if (enabled) 1f else .45f).heightIn(min = 52.dp).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (iconPainter != null) Icon(iconPainter, null, Modifier.size(22.dp), tint = c.primary)
            else if (icon != null) Icon(icon, null, Modifier.size(22.dp), tint = c.primary)
            Spacer(Modifier.width(if (icon != null || iconPainter != null) 14.dp else 0.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (badge != null) Text(badge.uppercase(), Modifier.padding(start = 8.dp).clip(RoundedCornerShape(5.dp)).background(c.primary.copy(alpha = .16f)).padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = c.primary)
                }
                if (subtitleContent != null) subtitleContent()
                else if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (trailing != null) trailing()
            else {
                if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant, maxLines = 1)
                if (onClick != null) Icon(SkinIcons.ChevronRight, null, Modifier.size(20.dp), tint = c.onSurfaceVariant.copy(alpha = .7f))
            }
        }
    }

    @Composable
    override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
        val c = MaterialTheme.colorScheme
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(style.brand.uppercase(), style = MaterialTheme.typography.labelMedium, color = c.primary)
                    Text(account?.name?.let { "Hi, ${it.substringBefore(' ')}" } ?: style.greeting,
                        style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Surface(onClick = onAccount, shape = RoundedCornerShape(50), color = c.surfaceVariant, modifier = Modifier.size(44.dp)) {
                    if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Box(contentAlignment = Alignment.Center) { Icon(SkinIcons.Person, "Account") }
                }
            }
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("History", SkinIcons.History, onHistory, style.radius)
                Chip("On device", SkinIcons.FolderOpen, onLocal, style.radius)
                Chip("Explore", SkinIcons.Explore, onExplore, style.radius)
            }
        }
    }

    @Composable
    override fun ReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit, onSearch: () -> Unit,
        onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: Dp) {
        val c = MaterialTheme.colorScheme
        Column(Modifier.fillMaxWidth().padding(top = topPadding).padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(page.thumbnailUrl, null, Modifier.widthIn(max = 280.dp).fillMaxWidth().aspectRatio(1f)
                .clip(RoundedCornerShape(style.radius)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            Text(page.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 18.dp),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(page.subtitle, color = c.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${songs.size} tracks", color = c.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 2.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onPlay, Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(style.radius)) {
                    Icon(SkinIcons.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Play")
                }
                FilledTonalIconButton(onShuffle, Modifier.size(48.dp), shape = RoundedCornerShape(style.radius)) { Icon(SkinIcons.Shuffle, "Shuffle") }
                FilledTonalIconButton(onSearch, Modifier.size(48.dp), shape = RoundedCornerShape(style.radius)) { Icon(SkinIcons.Search, "Search tracks") }
                onToggleLibrary?.let { FilledTonalIconButton(it, Modifier.size(48.dp), shape = RoundedCornerShape(style.radius)) {
                    Icon(if (page.library?.saved == true) SkinIcons.Check else SkinIcons.Add, "Library") } }
                onMore?.let { FilledTonalIconButton({ it(songs) }, Modifier.size(48.dp), shape = RoundedCornerShape(style.radius)) { Icon(SkinIcons.MoreHoriz, "More") } }
            }
        }
    }

    @Composable
    override fun SleepTimer(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>,
        onStart: (Int) -> Unit, onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(style.radius + 8.dp),
            title = { Text("Sleep timer") },
            text = {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text(when { afterTrack -> "Stopping after this track"; running -> "$minutes minutes remaining"; else -> "Choose when playback stops" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    presets.chunked(3).forEach { row ->
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { n -> FilledTonalButton({ onStart(n); onDismiss() }, Modifier.weight(1f), shape = RoundedCornerShape(style.radius)) { Text("$n m") } }
                        }
                    }
                    TextButton({ onAfterTrack(); onDismiss() }, Modifier.padding(top = 8.dp)) { Text("After this track") }
                    if (running) TextButton({ onCancel(); onDismiss() }) { Text("Cancel timer") }
                }
            },
            confirmButton = { TextButton(onDismiss) { Text("Done") } })
    }

    @Composable
    override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
        val c = MaterialTheme.colorScheme
        SkinDock(shape = RoundedCornerShape(style.radius + 10.dp), color = c.surface, border = androidx.compose.foundation.BorderStroke(1.dp, c.outlineVariant)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tabs.forEachIndexed { index, tab ->
                    val selected = index == selectedIndex
                    Surface(onClick = { onTabSelected(index) }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(style.radius),
                        color = if (selected) c.primary.copy(alpha = .16f) else Color.Transparent,
                        contentColor = if (selected) c.primary else c.onSurfaceVariant) {
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(tab.icon, tab.label, Modifier.size(22.dp))
                            Text(tab.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) {
        val c = MaterialTheme.colorScheme
        Row(Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongPress).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(item.thumbnailUrl, null, Modifier.size(56.dp).clip(RoundedCornerShape(style.radius / 2)).background(c.surfaceVariant), contentScale = ContentScale.Crop)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.subtitle.isNotBlank()) Text(item.subtitle, color = c.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(SkinIcons.ChevronRight, null, tint = c.onSurfaceVariant)
        }
    }

    @Composable
    override fun BottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) {
        // The dock floats, so the feed shows around it; this only softens what passes
        // under the dock and the gesture bar rather than walling it off.
        val inset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(modifier.fillMaxWidth().height(inset + if (withMiniPlayer) 150.dp else 84.dp).background(Brush.verticalGradient(
            0f to pageColor.copy(alpha = 0f), .45f to pageColor.copy(alpha = .35f), 1f to pageColor.copy(alpha = .85f))))
    }

    @Composable
    override fun LibraryStats(onOpen: () -> Unit) {
        TextButton(onOpen, Modifier.padding(horizontal = 12.dp)) { Icon(SkinIcons.Insights, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Listening stats") }
    }

    override fun LazyListScope.FeedShelves(shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
        onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) =
        styledShelves(style, shelves, layout, onClick, onLongPress, shelfBrowseId)
}

@Composable
private fun Chip(label: String, icon: ImageVector, onClick: () -> Unit, radius: Dp) {
    val c = MaterialTheme.colorScheme
    Surface(onClick = onClick, shape = RoundedCornerShape(radius), color = c.surfaceVariant, contentColor = c.onSurface) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp), tint = c.primary); Spacer(Modifier.width(6.dp)); Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun DefaultMiniPlayer(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float,
    onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, style: SkinStyle, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    Surface(modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(), color = c.surfaceVariant, contentColor = c.onSurface,
        shape = RoundedCornerShape(style.radius), tonalElevation = 3.dp) {
        Column {
            Row(Modifier.height(64.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).fillMaxHeight().clickable(onClick = onExpand).padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(artwork, null, Modifier.size(48.dp).clip(RoundedCornerShape(style.radius / 2)).background(c.surface), contentScale = ContentScale.Crop)
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onPlayPause) {
                    if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(if (playing) SkinIcons.Pause else SkinIcons.PlayArrow, if (playing) "Pause" else "Play", tint = c.primary)
                }
                IconButton(onNext) { Icon(SkinIcons.SkipNext, "Next") }
            }
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(2.dp), color = c.primary, trackColor = c.outlineVariant, drawStopIndicator = {})
        }
    }
}

/** "3:45" from the catalogue's duration text, for players that have not reported a length yet. */
fun durationFromText(text: String?): Long {
    val parts = text?.split(':')?.map { it.trim().toLongOrNull() ?: return 0L } ?: return 0L
    return if (parts.size in 2..3 && parts.all { it >= 0 }) parts.fold(0L) { n, v -> n * 60 + v } * 1000L else 0L
}

/** "m:ss" for a position or a length. */
fun clock(ms: Long): String { val s = ms.coerceAtLeast(0) / 1000; return "${s / 60}:${(s % 60).toString().padStart(2, '0')}" }
