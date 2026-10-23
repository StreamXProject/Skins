package com.music.streamx.skins.comic

import org.junit.Assert.*
import org.junit.Test

class QueueReorderTest {
    @Test fun currentAndPlayedTracksCannotBeReordered() {
        assertNull(comicQueueTarget(2,1,2,8))
        assertNull(comicQueueTarget(1,4,2,8))
        assertNull(comicQueueTarget(-1,1,2,8))
    }
    @Test fun movementIsClampedToUpcomingTracks() {
        assertEquals(3,comicQueueTarget(5,-100,2,8))
        assertEquals(7,comicQueueTarget(5,100,2,8))
        assertEquals(4,comicQueueTarget(5,-1,2,8))
        assertEquals(6,comicQueueTarget(5,1,2,8))
        assertNull(comicQueueTarget(5,0,2,8))
    }
    @Test fun latePlaybackAdvanceInvalidatesTheOldDrag() {
        assertNull(comicQueueTarget(3,2,3,8))
        assertNull(comicQueueTarget(7,1,2,7))
        assertNull(comicQueueTarget(0,1,0,1))
    }
}
