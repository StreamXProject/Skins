package com.music.streamx.skins.polaroid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.data.model.Account
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.data.model.Song
import com.music.streamx.skins.common.*
import com.music.streamx.ui.components.BottomTab

/** Polaroid: shared chrome from skinCommon, with its own home header, navigation, mini player and player stage. */
class PolaroidRenderer : BaseSkinRenderer(SkinStyle(brand = "Polaroid", radius = 14.dp, greeting = "Shake it like a…",
    shelf = ShelfStyle.PRINTS, serifLyrics = false,
    // Butter-yellow washi so the accent is not the only colour on screen; both are changeable in Settings.
    defaultSecondary = Color(0xFFF4D98B),
    secondaryHint = "Washi tape, stickers and the lyric notepad's header.",
    signatureHint = "The handwriting on prints, notes, captions and tabs.")) {
    @Composable
    override fun NowPlaying(model: NowPlayingModel, actions: NowPlayingActions, modifier: Modifier) = PolaroidStage(model, actions, modifier)

    @Composable
    override fun HomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) =
        PolaroidHomeHeader(account, onAccount, onLocal, onHistory, onExplore)

    @Composable
    override fun BottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) =
        PolaroidBottomNavigation(tabs, selectedIndex, onTabSelected)

    @Composable
    override fun MiniPlayerContent(song: Song, artwork: String?, playing: Boolean, loading: Boolean, fraction: Float,
        onPlayPause: () -> Unit, onExpand: () -> Unit, onNext: () -> Unit, modifier: Modifier) =
        PolaroidMiniPlayer(song, artwork, playing, loading, fraction, onPlayPause, onExpand, onNext, modifier)

    @Composable
    override fun Lyrics(model: NowPlayingModel, lines: List<LyricLine>?, unavailable: Boolean, offsetMs: Int, onSeek: (Long) -> Unit, modifier: Modifier) =
        PolaroidLyrics(model, lines, unavailable, offsetMs, onSeek, modifier)

    @Composable
    override fun Queue(model: NowPlayingModel, autoplay: Boolean, onJump: (Int) -> Unit, onRemove: (Int) -> Unit, onMove: (Int, Int) -> Unit,
        onClear: () -> Unit, onPlayPause: () -> Unit, onAutoplay: () -> Unit, modifier: Modifier) =
        PolaroidQueue(model, autoplay, onJump, onRemove, onMove, onClear, onPlayPause, onAutoplay, modifier)

    @Composable
    override fun LibraryHeading(onNewPlaylist: (() -> Unit)?) = PolaroidLibraryHeading(onNewPlaylist)

    @Composable
    override fun LibraryRow(item: ShelfItem, onClick: () -> Unit, onLongPress: () -> Unit) = PolaroidLibraryRow(item, onClick, onLongPress)
}
