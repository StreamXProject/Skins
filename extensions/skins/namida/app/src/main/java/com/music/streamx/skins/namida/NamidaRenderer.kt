package com.music.streamx.skins.namida

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.lyrics.LyricsSource
import com.music.streamx.data.model.Account
import com.music.streamx.data.model.DetailPage
import com.music.streamx.data.model.HomeShelf
import com.music.streamx.data.model.LikeStatus
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.data.model.Song
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.SKIN_UI_API_VERSION
import com.music.streamx.ui.skins.SkinBrowseHeaderProvider
import com.music.streamx.ui.skins.SkinCompositionProvider
import com.music.streamx.ui.skins.SkinLayout
import com.music.streamx.ui.skins.SkinThemeProvider
import com.music.streamx.ui.skins.SkinUiRenderer

/** A skin in the manner of Namida: its colours, type, bars, player and queue. Playback stays the host's. */
class NamidaRenderer : SkinUiRenderer, SkinThemeProvider, SkinCompositionProvider, SkinBrowseHeaderProvider,
    com.music.streamx.ui.skins.SkinSongMenuProvider, com.music.streamx.ui.skins.SkinSettingsProvider, com.music.streamx.ui.skins.SkinSettingsPageProvider,
    com.music.streamx.ui.skins.SkinToggle, com.music.streamx.ui.skins.SkinSegmentedControl, com.music.streamx.ui.skins.SkinSlider,
    com.music.streamx.ui.skins.SkinAlertDialog, com.music.streamx.ui.skins.SkinOptionSheet, com.music.streamx.ui.skins.SkinTextField {
    override val apiVersion = SKIN_UI_API_VERSION
    override fun verifyCompatibility() {
        NamidaIcons.Home; NamidaIcons.Play; namidaTones(null, true)
    }
    override fun mapIcon(vector: ImageVector) = namidaIcon(vector)
    override fun icon(name: String): ImageVector? = when (name) {
        "save" -> NamidaIcons.Heart
        "saved" -> NamidaIcons.HeartFilled
        "next" -> NamidaIcons.Next
        "previous" -> NamidaIcons.Previous
        "play" -> NamidaIcons.Play
        "pause" -> NamidaIcons.Pause
        "shuffle" -> NamidaIcons.Shuffle
        else -> null
    }

    // ---- theme ----
    @Composable override fun Colors(darkTheme: Boolean): ColorScheme = namidaColors(darkTheme)
    override fun typography(): Typography = namidaTypography(androidx.compose.ui.text.font.FontFamily.Default)
    override fun shapes(): Shapes = NamidaShapes
    @Composable override fun Configuration(onDismiss: () -> Unit) { onDismiss() }
    @Composable override fun Theme(colors: ColorScheme, typography: Typography, shapes: Shapes, content: @Composable () -> Unit) =
        NamidaThemeScope(colors, content)

    // ---- track menu ----
    @Composable override fun SongMenu(song: Song, onDismiss: () -> Unit, onOpenTrack: (() -> Unit)?, primary: List<com.music.streamx.ui.skins.SkinMenuAction>,
        rows: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) = NamidaSongMenu(song, onDismiss, onOpenTrack, primary, rows)
    @Composable override fun SongMenuRow(icon: ImageVector, label: String, detail: String?, value: String?, tint: Color?, enabled: Boolean,
        loading: Boolean, onClick: (() -> Unit)?) = NamidaMenuRow(icon, label, detail, value, tint, enabled, loading, onClick)
    @Composable override fun SongMenuSeparator() = NamidaMenuSeparator()

    // ---- settings ----
    @Composable override fun SettingsPage(query: String, onQuery: (String) -> Unit, padding: androidx.compose.foundation.layout.PaddingValues,
        onBack: () -> Unit, content: @Composable () -> Unit) = NamidaSettingsPage(query, onQuery, padding, onBack, content)
    @Composable override fun SettingsGroup(header: String?, footer: String?, topSpacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) =
        NamidaSettingsGroup(header, footer, topSpacing, content)
    @Composable override fun SettingsDivider() {}
    @Composable override fun SettingsRow(icon: ImageVector?, iconPainter: androidx.compose.ui.graphics.painter.Painter?, title: String,
        subtitle: String?, subtitleContent: (@Composable () -> Unit)?, value: String?, badge: String?, enabled: Boolean,
        onClick: (() -> Unit)?, trailing: (@Composable () -> Unit)?) =
        NamidaSettingsRow(icon, iconPainter, title, subtitle, subtitleContent, value, badge, enabled, onClick, trailing)
    @Composable override fun Toggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean, modifier: Modifier) =
        NamidaToggle(checked, onCheckedChange, enabled, modifier)
    @Composable override fun SegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier) =
        NamidaSegmented(options, selectedIndex, onSelect, enabled, modifier)
    @Composable override fun Slider(value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int, enabled: Boolean, modifier: Modifier) =
        NamidaSlider(value, onValueChange, valueRange, steps, enabled, modifier)
    @Composable override fun AlertDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit, dismissButton: (@Composable () -> Unit)?,
        title: (@Composable () -> Unit)?, text: (@Composable () -> Unit)?, modifier: Modifier) =
        NamidaAlert(onDismissRequest, confirmButton, dismissButton, title, text)
    @Composable override fun OptionSheet(title: String, subtitle: String?, options: List<com.music.streamx.ui.skins.SkinOption>, selectedIndex: Int,
        onSelect: (Int) -> Unit, onDismissRequest: () -> Unit) = NamidaOptions(title, subtitle, options, selectedIndex, onSelect, onDismissRequest)
    @Composable override fun TextField(value: String, onValueChange: (String) -> Unit, placeholder: String?, singleLine: Boolean, modifier: Modifier) =
        NamidaTextField(value, onValueChange, placeholder, singleLine, modifier)

    // ---- player ----
    @Composable override fun Player(song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long, queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean, repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus, accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit, onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit, onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit, onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit, onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>, onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean, onDismissLyricsOffset: () -> Unit, modifier: Modifier) =
        NamidaPlayer(song, isPlaying, isLoading, position, durationMs, queue, queueIndex, hasPrevious, hasNext, repeatMode, shuffleEnabled,
            autoplayEnabled, likeStatus, accountName, onToggleLike, onPlayPause, onNext, onPrevious, onSeekFraction, onSeek, onToggleShuffle,
            onCycleRepeat, onToggleAutoplay, onJumpTo, onRemove, onMove, onDragActive, onClear, onOpenMenu, onOpenArtist, onOpenPlaybackSource,
            onCollapse, onOpenTimer, onBlocked, lyrics, lyricsSource, lyricsUnavailable, lyricsProviderStates, onSelectLyricsProvider,
            lyricsOffsetOpen, onDismissLyricsOffset, modifier)
    @Composable override fun MiniPlayer(song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long, saved: Boolean, onSave: () -> Unit, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, locked: Boolean, onBlocked: () -> Unit, modifier: Modifier) =
        NamidaMiniPlayer(song, playing, loading, position, durationMs, onPlayPause, onExpand, onNext, onPrevious, locked, onBlocked, modifier)

    // ---- pages ----
    @Composable override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) =
        NamidaHomeHeader(account, onAccount, onLocal, onHistory)
    @Composable override fun BrowseHeader(account: Account?, onSearch: () -> Unit, onSettings: () -> Unit, onExplore: () -> Unit) =
        NamidaBrowseHeader(account, onSearch, onSettings, onExplore)
    @Composable override fun ReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit, onSearch: () -> Unit, onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: androidx.compose.ui.unit.Dp) =
        NamidaReleaseHeader(page, songs, onPlay, onShuffle, onSearch, onMore, onArtist, onToggleLibrary, topPadding)
    @Composable override fun SleepTimer(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>, onStart: (Int) -> Unit, onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) =
        NamidaSleepTimer(minutes, afterTrack, running, presets, onStart, onAfterTrack, onCancel, onDismiss)
    @Composable override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) =
        NamidaBottomNavigation(tabs, selectedIndex, onTabSelected)
    @Composable override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) = NamidaLibraryRow(item, onClick, onLongPress)
    @Composable override fun BottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) = NamidaBottomFade(modifier, withMiniPlayer, pageColor)
    @Composable override fun LibraryStats(onOpen: () -> Unit) = NamidaLibraryStats(onOpen)
    override fun LazyListScope.FeedShelves(shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
        onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) =
        namidaFeedShelves(shelves, layout, onClick, onLongPress, shelfBrowseId)
}

/** The host's icon, swapped for Namida's line drawing of the same thing where there is one. */
internal fun namidaIcon(vector: ImageVector): ImageVector = when (vector.name.substringAfterLast('.')) {
    "Home" -> NamidaIcons.Home
    "Search" -> NamidaIcons.Search
    "Settings" -> NamidaIcons.Settings
    "LibraryMusic", "QueueMusic" -> NamidaIcons.Library
    "Explore" -> NamidaIcons.Explore
    "Favorite", "FavoriteBorder" -> NamidaIcons.Heart
    "History" -> NamidaIcons.History
    "Shuffle" -> NamidaIcons.Shuffle
    "Repeat" -> NamidaIcons.Repeat
    "RepeatOne" -> NamidaIcons.RepeatOne
    "PlayArrow" -> NamidaIcons.Play
    "Pause" -> NamidaIcons.Pause
    "SkipNext" -> NamidaIcons.Next
    "SkipPrevious" -> NamidaIcons.Previous
    "Lyrics" -> NamidaIcons.Lyrics
    "MoreVert" -> NamidaIcons.MoreVertical
    "MoreHoriz" -> NamidaIcons.More
    "Close" -> NamidaIcons.Close
    "Add" -> NamidaIcons.Add
    "Check" -> NamidaIcons.Check
    "Download", "FileDownload" -> NamidaIcons.Download
    "DragHandle" -> NamidaIcons.DragHandle
    "Timer", "Bedtime" -> NamidaIcons.Timer
    "MusicNote", "bc_music_note" -> NamidaIcons.Tracks
    "Folder" -> NamidaIcons.Folder
    "Album" -> NamidaIcons.Albums
    "Person", "AccountCircle" -> NamidaIcons.Account
    "Group", "Groups" -> NamidaIcons.Group
    "ChevronRight", "KeyboardArrowRight" -> NamidaIcons.ChevronRight
    "KeyboardArrowDown", "ExpandMore" -> NamidaIcons.ArrowDown
    "ArrowBack" -> NamidaIcons.Back
    "Share", "IosShare" -> NamidaIcons.Share
    "Edit" -> NamidaIcons.Edit
    "Delete", "DeleteForever" -> NamidaIcons.Delete
    "Info" -> NamidaIcons.Info
    "Translate" -> NamidaIcons.Translate
    "Headphones" -> NamidaIcons.Headphones
    "GraphicEq", "Equalizer", "Tune" -> NamidaIcons.Equalizer
    "Radio", "Sensors" -> NamidaIcons.Radio
    "VideoLibrary", "OndemandVideo", "Videocam", "SmartDisplay" -> NamidaIcons.Video
    "Wifi" -> NamidaIcons.Wifi
    "SignalCellularAlt" -> NamidaIcons.Signal
    "VolumeUp", "SurroundSound", "Waves" -> NamidaIcons.Volume
    "VolumeOff", "MusicOff" -> NamidaIcons.VolumeOff
    "Extension" -> NamidaIcons.Plugin
    "Storage", "Dns" -> NamidaIcons.Storage
    "Language" -> NamidaIcons.Globe
    "BarChart" -> NamidaIcons.Chart
    "AutoAwesome", "Animation" -> NamidaIcons.Sparkle
    "Apps" -> NamidaIcons.Grid
    "Cached", "Refresh", "Sync" -> NamidaIcons.Refresh
    "VisibilityOff", "WebAssetOff", "MotionPhotosOff", "BlurOff" -> NamidaIcons.Eye
    "Brightness4", "Gradient", "BlurOn", "Palette" -> NamidaIcons.Theme
    "LocalOffer", "Label" -> NamidaIcons.Tag
    "Fullscreen" -> NamidaIcons.Fullscreen
    "FilterAlt" -> NamidaIcons.Filter
    "Draw" -> NamidaIcons.Edit
    "Notes" -> NamidaIcons.Lyrics
    "PlaylistAdd" -> NamidaIcons.Playlists
    "PlaylistRemove", "DeleteSweep" -> NamidaIcons.Delete
    "PlaylistPlay" -> NamidaIcons.Next
    "Undo" -> NamidaIcons.History
    "HighQuality" -> NamidaIcons.Equalizer
    "BugReport", "ErrorOutline" -> NamidaIcons.Info
    "DownloadDone" -> NamidaIcons.Check
    "Downloading", "FileUpload" -> NamidaIcons.Download
    else -> vector
}
