package com.music.streamx.skins.vinyl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.model.Account
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.data.model.Song
import com.music.streamx.skins.common.*
import com.music.streamx.ui.components.BottomTab

/** Vinyl: shared chrome from skinCommon, with its own home header, navigation, mini player and player stage. */
class VinylRenderer : BaseSkinRenderer(SkinStyle(brand = "Vinyl", radius = 18.dp, greeting = "Drop the needle.",
    shelf = ShelfStyle.DISCS, serifLyrics = true)) {
    @Composable
    override fun NowPlaying(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) = VinylStage(model, actions, modifier)

    @Composable
    override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) =
        VinylHomeHeader(account, onAccount, onLocal, onHistory, onExplore)

    @Composable
    override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) =
        VinylBottomNavigation(tabs, selectedIndex, onTabSelected)

    @Composable
    override fun MiniPlayerContent(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float,
        onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) =
        VinylMiniPlayer(song, artwork, playing, loading, fraction, onPlayPause, onExpand, onNext, modifier)

    @Composable
    override fun Lyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) =
        VinylLyrics(model, lines, unavailable, offsetMs, onSeek, modifier)

    @Composable
    override fun Queue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
        onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) =
        VinylQueue(model, autoplay, onJump, onRemove, onMove, onClear, onPlayPause, onAutoplay, modifier)

    @Composable
    override fun LibraryHeading(onNewPlaylist: (() -> Unit)?) = VinylLibraryHeading(onNewPlaylist)

    @Composable
    override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) = VinylLibraryRow(item, onClick, onLongPress)
}
