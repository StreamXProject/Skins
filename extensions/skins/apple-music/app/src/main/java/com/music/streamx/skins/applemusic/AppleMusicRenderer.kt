package com.music.streamx.skins.applemusic

import androidx.compose.runtime.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyListScope
import com.music.streamx.data.model.*
import com.music.streamx.data.lyrics.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.*

class AppleMusicRenderer : SkinUiRenderer, com.music.streamx.ui.skins.SkinArtworkStatsProvider, SkinAccentNavigation, SkinPluginLibraryProvider, SkinLibraryChrome, SkinFeedHeaderProvider, SkinDefaultPlayerProvider, SkinSolidNavigation, SkinNavigationProvider, SkinThemeProvider, SkinUseHostTheme, com.music.streamx.ui.skins.SkinArtworkPages, com.music.streamx.ui.skins.SkinSegmentedControl, com.music.streamx.ui.skins.SkinToggle, com.music.streamx.ui.skins.SkinSlider, com.music.streamx.ui.skins.SkinAlertDialog, com.music.streamx.ui.skins.SkinOptionSheet, com.music.streamx.ui.skins.SkinTextField, com.music.streamx.ui.skins.SkinRadioTab, com.music.streamx.ui.skins.SkinNavigationLabels, SkinSettingsProvider {
    @Composable override fun SettingsGroup(header: String?, footer: String?, topSpacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) = AppleSettingsGroup(header, footer, topSpacing, content)
    @Composable override fun SettingsDivider() = AppleSettingsDivider()
    @Composable override fun SettingsRow(icon: ImageVector?, iconPainter: androidx.compose.ui.graphics.painter.Painter?, title: String,
        subtitle: String?, subtitleContent: (@Composable () -> Unit)?, value: String?, badge: String?, enabled: Boolean,
        onClick: (() -> Unit)?, trailing: (@Composable () -> Unit)?) = AppleSettingsRow(title, subtitle, subtitleContent, value, badge, enabled, onClick, trailing)
    @Composable override fun FeedHeader(title: String,actions: SkinLibraryActions) = AppleMusicFeedHeader(title,actions)
    @Composable override fun PlayerStyle() = applePlayerStyle()
    // By tab id: Home, New (the host's grid glyph), Library, Search, Radio.
    override fun navigationIcon(index: Int) = listOf(com.music.streamx.ui.icons.StreamXIcons.Home,null,AppleIcons.Library,androidx.compose.material.icons.Icons.Rounded.Search,androidx.compose.material.icons.Icons.Rounded.Sensors).getOrNull(index)
    override fun navigationLabel(index: Int) = if (index == 1) "New" else null
    @Composable override fun Colors(darkTheme: Boolean) = appleColors(darkTheme)
    override fun typography() = androidx.compose.material3.Typography()
    override fun shapes() = androidx.compose.material3.Shapes()
    @Composable override fun Configuration(onDismiss: ()->Unit) = ApplePlayerConfiguration(onDismiss)
    @Composable override fun Library(shelves: List<HomeShelf>,links: List<ShelfItem>,deviceItems: List<ShelfItem>,contentPadding: androidx.compose.foundation.layout.PaddingValues,onItem: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,onShowAll: (HomeShelf)->Unit,onNewPlaylist: (()->Unit)?) = AppleMusicLibrary(shelves,links,deviceItems,contentPadding,onItem,onLongPress,onShowAll,onNewPlaylist)
    override val apiVersion = SKIN_UI_API_VERSION
    override fun verifyCompatibility() {AppleIcons.verify();(0..3).forEach{navigationIcon(it)}}
    override fun mapIcon(vector: ImageVector) = vector
    override fun icon(name: String): ImageVector? = null
    @Composable override fun Player(song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long, queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean, repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus, accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit, onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit, onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit, onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit, onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>, onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean, onDismissLyricsOffset: () -> Unit, modifier: Modifier) { /* The complete host player handles this IMMERSIVE skin. */ }
    @Composable override fun MiniPlayer(song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long, saved: Boolean, onSave: () -> Unit, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, locked: Boolean, onBlocked: () -> Unit, modifier: Modifier) { AppleMusicMiniPlayer(song, playing, loading, position, durationMs, saved, onSave, onPlayPause, onExpand, onNext, onPrevious, locked, onBlocked, modifier) }
    @Composable override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) { AppleMusicHomeHeader(account, onAccount, onLocal, onHistory, onExplore) }
    @Composable override fun ReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit, onSearch: () -> Unit, onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: androidx.compose.ui.unit.Dp) { AppleMusicReleaseHeader(page, songs, onPlay, onShuffle, onSearch, onMore, onArtist, onToggleLibrary, topPadding) }
    @Composable override fun SleepTimer(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>, onStart: (Int) -> Unit, onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) { AppleMusicSleepTimerDialog(minutes, afterTrack, running, presets, onStart, onAfterTrack, onCancel, onDismiss) }
    @Composable override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) { AppleMusicBottomNavigation(tabs, selectedIndex, onTabSelected) }
    @Composable override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) { AppleMusicLibraryRow(item, onClick, onLongPress) }
    @Composable override fun BottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) = AppleMusicBottomFade(modifier, withMiniPlayer, pageColor)
    @Composable override fun LibraryStats(onOpen: () -> Unit) = AppleMusicLibraryStats(onOpen)
    @Composable override fun SegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier) = AppleSegmentedControl(options, selectedIndex, onSelect, enabled, modifier)
    @Composable override fun Toggle(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, enabled: Boolean, modifier: Modifier) = AppleToggle(checked, onCheckedChange, enabled, modifier)
    @Composable override fun Slider(value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, steps: Int, enabled: Boolean, modifier: Modifier) = AppleSlider(value, onValueChange, valueRange, steps, enabled, modifier)
    @Composable override fun AlertDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit, dismissButton: (@Composable () -> Unit)?, title: (@Composable () -> Unit)?, text: (@Composable () -> Unit)?, modifier: Modifier) =
        AppleAlertDialog(onDismissRequest, confirmButton, dismissButton, title, text, modifier)
    @Composable override fun OptionSheet(title: String, subtitle: String?, options: List<com.music.streamx.ui.skins.SkinOption>, selectedIndex: Int, onSelect: (Int) -> Unit, onDismissRequest: () -> Unit) =
        AppleOptionSheet(title, subtitle, options, selectedIndex, onSelect, onDismissRequest)
    @Composable override fun TextField(value: String, onValueChange: (String) -> Unit, placeholder: String?, singleLine: Boolean, modifier: Modifier) =
        AppleTextField(value, onValueChange, placeholder, singleLine, modifier)
    override fun LazyListScope.FeedShelves(shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
        onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) {
        applemusicFeedShelves(shelves, layout, onClick, onLongPress, shelfBrowseId)
    }
}
