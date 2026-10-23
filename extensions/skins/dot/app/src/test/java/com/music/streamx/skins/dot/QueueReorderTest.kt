package com.music.streamx.skins.dot

import org.junit.Assert.*
import org.junit.Test

class QueueReorderTest {
    @Test
    fun targetClampsToUpcomingRange() {
        assertEquals(2, dotQueueTarget(from = 1, rows = 1, current = 0, size = 4))
        assertEquals(3, dotQueueTarget(from = 1, rows = 5, current = 0, size = 4))
        assertEquals(1, dotQueueTarget(from = 3, rows = -5, current = 0, size = 4))
        assertNull(dotQueueTarget(from = 0, rows = 1, current = 0, size = 4))
        assertNull(dotQueueTarget(from = 2, rows = 0, current = 0, size = 4))
    }
}
