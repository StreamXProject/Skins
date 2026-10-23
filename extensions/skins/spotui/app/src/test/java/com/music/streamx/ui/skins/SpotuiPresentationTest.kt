package com.music.streamx.ui.skins

import com.music.streamx.data.lyrics.LyricLine
import com.music.streamx.ui.player.spotuiActiveLyric
import com.music.streamx.ui.player.spotuiQueueMoveTarget
import com.music.streamx.ui.player.spotuiTime
import com.music.streamx.ui.player.spotuiQueueDropTarget
import com.music.streamx.ui.player.spotuiQueuePreviewShift
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import androidx.compose.ui.graphics.luminance

class SpotuiPresentationTest {
    @Test fun `plain lyrics stay at the beginning rather than following the clock`() {
        assertEquals(0, spotuiActiveLyric(listOf(LyricLine(-1, "First"), LyricLine(-1, "Last")), 60000))
        assertEquals(0, spotuiActiveLyric(listOf(LyricLine(0, "First"), LyricLine(0, "Last")), 60000))
        assertEquals(0, spotuiActiveLyric(emptyList(), 100))
    }
    @Test fun `synced lyrics follow seeks in both directions`() {
        val lines = listOf(LyricLine(1000, "First"), LyricLine(5000, "Second"), LyricLine(10000, "Last"))
        assertEquals(0, spotuiActiveLyric(lines, 0))
        assertEquals(1, spotuiActiveLyric(lines, 5000))
        assertEquals(2, spotuiActiveLyric(lines, 90000))
        assertEquals(0, spotuiActiveLyric(lines, 2000))
    }
    @Test fun `queue drag cannot move past the current track or beyond the queue`() {
        assertNull(spotuiQueueMoveTarget(2, -1, 1, 5))
        assertNull(spotuiQueueMoveTarget(1, 1, 1, 5))
        assertNull(spotuiQueueMoveTarget(4, 1, 1, 5))
        assertNull(spotuiQueueMoveTarget(3, 0, 1, 5))
        assertNull(spotuiQueueMoveTarget(9, -1, 1, 5))
        assertEquals(3, spotuiQueueMoveTarget(2, 1, 1, 5))
        assertEquals(2, spotuiQueueMoveTarget(3, -1, 1, 5))
    }
    @Test fun `queue drop commits one bounded move and protects history`() {
        assertEquals(4, spotuiQueueDropTarget(2, 20, 1, 5))
        assertEquals(2, spotuiQueueDropTarget(4, -20, 1, 5))
        assertEquals(3, spotuiQueueDropTarget(2, 1, 1, 5))
        assertNull(spotuiQueueDropTarget(1, 2, 1, 5))
        assertNull(spotuiQueueDropTarget(2, 0, 1, 5))
        assertNull(spotuiQueueDropTarget(-1, 1, 0, 5))
        assertNull(spotuiQueueDropTarget(5, -1, 0, 5))
    }
    @Test fun `drag preview shifts only intervening rows in both directions`() {
        assertEquals(listOf(0, 0, -1, -1, -1, 0), (0..5).map { spotuiQueuePreviewShift(it, 1, 4) })
        assertEquals(listOf(0, 1, 1, 1, 0, 0), (0..5).map { spotuiQueuePreviewShift(it, 4, 1) })
        assertEquals(listOf(0, 0, 0), (0..2).map { spotuiQueuePreviewShift(it, 1, 1) })
    }
    @Test fun `clock handles unknown and long durations`() {
        assertEquals("0:00", spotuiTime(-1))
        assertEquals("3:04", spotuiTime(184000))
        assertEquals("61:01", spotuiTime(3661000))
    }
    @Test fun `existing skins keep their player and icon pack`() {
        val layout = Json.decodeFromString<SkinLayout>("""{"player":"CLASSIC"}""")
        assertEquals(PlayerLayout.CLASSIC, layout.player)
        assertEquals(SkinIconPack.DEFAULT, layout.icons)
    }
    @Test fun `spotui opts into full player and icon pack together`() {
        val layout = Json.decodeFromString<SkinLayout>("""{"player":"EXTENSION","icons":"EXTENSION"}""")
        layout.validate()
        assertEquals(PlayerLayout.EXTENSION, layout.player)
        assertEquals(SkinIconPack.EXTENSION, layout.icons)
    }

    @Test fun `artwork tint picks color rather than a black border`() {
        val tint = spotuiArtworkTint(listOf(androidx.compose.ui.graphics.Color.Black, androidx.compose.ui.graphics.Color(0xFFAA5522)))
        assertTrue(tint.red > tint.blue)
        assertTrue(tint.red > .2f)
    }
    @Test fun `artwork tint keeps light controls readable on bright covers`() {
        val tint = spotuiArtworkTint(listOf(androidx.compose.ui.graphics.Color.White, androidx.compose.ui.graphics.Color.Yellow))
        assertTrue(tint.luminance() <= .16f)
        assertEquals(1f, tint.alpha, 0f)
    }
}
