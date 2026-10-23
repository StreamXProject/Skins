package com.music.streamx.skins.material

import androidx.compose.runtime.*
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

class ExpressiveRenderer : SkinUiRenderer, SkinThemeProvider {
    @Composable override fun Colors(darkTheme: Boolean) = expressiveColors(darkTheme)
    override fun typography() = expressiveTypography()
    override fun shapes() = expressiveShapes()
    @Composable override fun Configuration(onDismiss: ()->Unit) = AppearanceConfiguration(onDismiss)
    override val apiVersion = SKIN_UI_API_VERSION
    override fun verifyCompatibility() = run { tonalScheme(seedColor(DEFAULT_SEED), false); verifyExpressiveSymbols(); Unit }
    override fun mapIcon(vector: ImageVector) = expressiveIcon(vector)
    override fun icon(name: String): ImageVector? = expressiveResourceIcon(name)
    @Composable override fun Player(song: Song, isPlaying: Boolean, isLoading: Boolean, position: PlaybackPosition, durationMs: Long, queue: List<Song>, queueIndex: Int, hasPrevious: Boolean, hasNext: Boolean, repeatMode: Int, shuffleEnabled: Boolean, autoplayEnabled: Boolean, likeStatus: LikeStatus, accountName: String?, onToggleLike: () -> Unit, onPlayPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, onSeekFraction: (Float) -> Unit, onSeek: (Long) -> Unit, onToggleShuffle: () -> Unit, onCycleRepeat: () -> Unit, onToggleAutoplay: () -> Unit, onJumpTo: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit, onDragActive: (Boolean) -> Unit, onClear: () -> Unit, onOpenMenu: () -> Unit, onOpenArtist: (String?, String) -> Unit, onOpenPlaybackSource: () -> Unit, onListenTogether: () -> Unit, onCollapse: () -> Unit, onOpenTimer: () -> Unit, windowHeight: androidx.compose.ui.unit.Dp, onBlocked: () -> Unit, lyrics: List<LyricLine>?, lyricsSource: LyricsSource?, lyricsUnavailable: Boolean, lyricsProviderStates: Map<LyricsSource, LyricsProviderState>, onSelectLyricsProvider: (LyricsSource) -> Unit, lyricsOffsetOpen: Boolean, onDismissLyricsOffset: () -> Unit, modifier: Modifier) { ExpressivePlayer(song, isPlaying, isLoading, position, durationMs, queue, queueIndex, hasPrevious, hasNext, repeatMode, shuffleEnabled, autoplayEnabled, likeStatus, accountName, onToggleLike, onPlayPause, onNext, onPrevious, onSeekFraction, onSeek, onToggleShuffle, onCycleRepeat, onToggleAutoplay, onJumpTo, onRemove, onMove, onDragActive, onClear, onOpenMenu, onOpenArtist, onOpenPlaybackSource, onListenTogether, onCollapse, onOpenTimer, windowHeight, onBlocked, lyrics, lyricsSource, lyricsUnavailable, lyricsProviderStates, onSelectLyricsProvider, lyricsOffsetOpen, onDismissLyricsOffset, modifier) }
    @Composable override fun MiniPlayer(song: Song, playing: Boolean, loading: Boolean, position: PlaybackPosition, durationMs: Long, saved: Boolean, onSave: () -> Unit, onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit, locked: Boolean, onBlocked: () -> Unit, modifier: Modifier) { ExpressiveMiniPlayer(song, playing, loading, position, durationMs, saved, onSave, onPlayPause, onExpand, onNext, onPrevious, locked, onBlocked, modifier) }
    @Composable override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) { ExpressiveHomeHeader(account, onAccount, onLocal, onHistory, onExplore) }
    @Composable override fun ReleaseHeader(page: DetailPage, songs: List<Song>, onPlay: () -> Unit, onShuffle: () -> Unit, onSearch: () -> Unit, onMore: ((List<Song>) -> Unit)?, onArtist: (String, String) -> Unit, onToggleLibrary: (() -> Unit)?, topPadding: androidx.compose.ui.unit.Dp) { ExpressiveReleaseHeader(page, songs, onPlay, onShuffle, onSearch, onMore, onArtist, onToggleLibrary, topPadding) }
    @Composable override fun SleepTimer(minutes: Int?, afterTrack: Boolean, running: Boolean, presets: List<Int>, onStart: (Int) -> Unit, onAfterTrack: () -> Unit, onCancel: () -> Unit, onDismiss: () -> Unit) { ExpressiveSleepTimerDialog(minutes, afterTrack, running, presets, onStart, onAfterTrack, onCancel, onDismiss) }
    @Composable override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) { ExpressiveBottomNavigation(tabs, selectedIndex, onTabSelected) }
    @Composable override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) { ExpressiveLibraryRow(item, onClick, onLongPress) }
    @Composable override fun BottomFade(modifier: Modifier, withMiniPlayer: Boolean, pageColor: Color) = ExpressiveBottomFade(modifier, withMiniPlayer, pageColor)
    @Composable override fun LibraryStats(onOpen: () -> Unit) = ExpressiveLibraryStats(onOpen)
    override fun LazyListScope.FeedShelves(shelves: List<HomeShelf>, layout: SkinLayout, onClick: (ShelfItem, String) -> Unit,
        onLongPress: ((ShelfItem) -> Unit)?, shelfBrowseId: (HomeShelf) -> String?) {
        expressiveFeedShelves(shelves, layout, onClick, onLongPress, shelfBrowseId)
    }
}
