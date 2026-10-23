package com.music.streamx.skins.comic

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyListScope
import com.music.streamx.data.model.*
import com.music.streamx.data.lyrics.*
import com.music.streamx.playback.PlaybackPosition
import com.music.streamx.ui.LyricsProviderState
import com.music.streamx.ui.components.BottomTab
import com.music.streamx.ui.skins.*

class ComicRenderer : SkinUiRenderer, SkinThemeProvider, SkinLibraryProvider, SkinSettingsProvider, SkinSettingsPageProvider, SkinBrowseHeaderProvider, SkinCompositionProvider {
    @Composable override fun Theme(colors: androidx.compose.material3.ColorScheme, typography: androidx.compose.material3.Typography, shapes: androidx.compose.material3.Shapes, content: @Composable ()->Unit) {
        androidx.compose.material3.MaterialTheme(colorScheme=colors,typography=typography,shapes=shapes) {
            com.music.streamx.ui.theme.StatusBarIcons(colors.background.luminance() > .5f)
            content()
        }
    }
    @Composable override fun BrowseHeader(account: Account?,onSearch: ()->Unit,onSettings: ()->Unit,onExplore: ()->Unit) = ComicBrowseHeader(onSearch,onSettings,onExplore)
    @Composable override fun SettingsPage(query: String,onQuery: (String)->Unit,padding: androidx.compose.foundation.layout.PaddingValues,onBack: ()->Unit,content: @Composable ()->Unit) = ComicSettingsPage(query,onQuery,padding,onBack,content)
    @Composable override fun Library(shelves: List<HomeShelf>,links: List<ShelfItem>,deviceItems: List<ShelfItem>,contentPadding: androidx.compose.foundation.layout.PaddingValues,onItem: (ShelfItem,String)->Unit,onLongPress: ((ShelfItem)->Unit)?,onShowAll: (HomeShelf)->Unit,onNewPlaylist: (()->Unit)?) = ComicLibrary(shelves,links,deviceItems,contentPadding,onItem,onLongPress,onShowAll,onNewPlaylist)
    @Composable override fun SettingsGroup(header: String?,footer: String?,topSpacing: androidx.compose.ui.unit.Dp,content: @Composable ()->Unit) = ComicSettingsGroup(header,footer,topSpacing,content)
    @Composable override fun SettingsDivider() = ComicSettingsDivider()
    @Composable override fun SettingsRow(icon: ImageVector?,iconPainter: androidx.compose.ui.graphics.painter.Painter?,title: String,subtitle: String?,subtitleContent: (@Composable ()->Unit)?,value: String?,badge: String?,enabled: Boolean,onClick: (()->Unit)?,trailing: (@Composable ()->Unit)?) = ComicSettingsRow(icon,iconPainter,title,subtitle,subtitleContent,value,badge,enabled,onClick,trailing)
    @Composable override fun Colors(darkTheme: Boolean) = comicColors(darkTheme)
    override fun typography() = comicTypography()
    override fun shapes() = comicShapes()
    @Composable override fun Configuration(onDismiss: ()->Unit) = AppearanceConfiguration(onDismiss)
    override val apiVersion = SKIN_UI_API_VERSION
    override fun verifyCompatibility() = ComicIcons.verify()
    override fun mapIcon(vector: ImageVector): ImageVector = when(vector.name.substringAfterLast('.')) {
        "Home", "HomeFilled" -> ComicIcons.Home
        "Search" -> ComicIcons.Search
        "LibraryMusic", "LibraryBooks", "QueueMusic" -> ComicIcons.Library
        "Explore", "ExploreFilled" -> ComicIcons.Explore
        "ArrowBack" -> ComicIcons.ArrowBack
        "MoreVert", "MoreHoriz" -> ComicIcons.MoreHoriz
        "PlayArrow", "PlayCircle" -> ComicIcons.PlayArrow
        "Pause" -> ComicIcons.Pause
        else -> vector
    }
    override fun icon(name: String): ImageVector? = null
    @Composable override fun Player(song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long, queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean, repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus, accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit, onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit, onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit, onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit, onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>, onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean, onDismissLyricsOffset: () -> Unit, modifier: Modifier) { ComicPlayer(song, isPlaying, isLoading, position, durationMs, queue, queueIndex, hasPrevious, hasNext, repeatMode, shuffleEnabled, autoplayEnabled, likeStatus, accountName, onToggleLike, onPlayPause, onNext, onPrevious, onSeekFraction, onSeek, onToggleShuffle, onCycleRepeat, onToggleAutoplay, onJumpTo, onRemove, onMove, onDragActive, onClear, onOpenMenu, onOpenArtist, onOpenPlaybackSource, onListenTogether, onCollapse, onOpenTimer, windowHeight, onBlocked, lyrics, lyricsSource, lyricsUnavailable, lyricsProviderStates, onSelectLyricsProvider, lyricsOffsetOpen, onDismissLyricsOffset, modifier) }
    @Composable override fun MiniPlayer(song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long, saved: Boolean, onSave: () -> Unit, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, locked: Boolean, onBlocked: () -> Unit, modifier: Modifier) { ComicMiniPlayer(song, playing, loading, position, durationMs, saved, onSave, onPlayPause, onExpand, onNext, onPrevious, locked, onBlocked, modifier) }
    @Composable override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) { ComicHomeHeader(account, onAccount, onLocal, onHistory, onExplore) }
    @Composable override fun ReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit, onSearch: () -> Unit, onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: androidx.compose.ui.unit.Dp) { ComicReleaseHeader(page, songs, onPlay, onShuffle, onSearch, onMore, onArtist, onToggleLibrary, topPadding) }
    @Composable override fun SleepTimer(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>, onStart: (Int) -> Unit, onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) { ComicSleepTimerDialog(minutes, afterTrack, running, presets, onStart, onAfterTrack, onCancel, onDismiss) }
    @Composable override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) { ComicBottomNavigation(tabs, selectedIndex, onTabSelected) }
    @Composable override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) { ComicLibraryRow(item, onClick, onLongPress) }
    @Composable override fun BottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) = ComicBottomFade(modifier, withMiniPlayer, pageColor)
    @Composable override fun LibraryStats(onOpen: () -> Unit) = ComicLibraryStats(onOpen)
    override fun LazyListScope.FeedShelves(shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
        onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) {
        comicFeedShelves(shelves, layout, onClick, onLongPress, shelfBrowseId)
    }
}
