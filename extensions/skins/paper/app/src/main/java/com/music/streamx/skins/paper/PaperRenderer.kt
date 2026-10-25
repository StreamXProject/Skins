package com.music.streamx.skins.paper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.model.Account
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.data.model.Song
import com.music.streamx.skins.common.*
import com.music.streamx.ui.components.BottomTab

/** Paper: shared chrome from skinCommon, with its own home header, navigation, mini player and player stage. */
class PaperRenderer : BaseSkinRenderer(SkinStyle(brand = "Paper", radius = 4.dp, greeting = "Quietly, in print.",
    shelf = ShelfStyle.TILES, serifLyrics = true)) {
    @Composable
    override fun NowPlaying(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) = PaperStage(model, actions, modifier)

    @Composable
    override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) =
        PaperHomeHeader(account, onAccount, onLocal, onHistory, onExplore)

    @Composable
    override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) =
        PaperBottomNavigation(tabs, selectedIndex, onTabSelected)

    @Composable
    override fun MiniPlayerContent(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float,
        onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) =
        PaperMiniPlayer(song, artwork, playing, loading, fraction, onPlayPause, onExpand, onNext, modifier)

    @Composable
    override fun Lyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) =
        PaperLyrics(model, lines, unavailable, offsetMs, onSeek, modifier)

    @Composable
    override fun Queue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
        onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) =
        PaperQueue(model, autoplay, onJump, onRemove, onMove, onClear, onPlayPause, onAutoplay, modifier)

    @Composable
    override fun LibraryHeading(onNewPlaylist: (() -> Unit)?) = PaperLibraryHeading(onNewPlaylist)

    @Composable
    override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) = PaperLibraryRow(item, onClick, onLongPress)
}
