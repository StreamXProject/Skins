package com.music.streamx.skins.material

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.*
import org.junit.Test

class ExpressiveColorTest {
    private fun contrast(a: Color,b: Color): Float {val x=a.luminance();val y=b.luminance();return (maxOf(x,y)+.05f)/(minOf(x,y)+.05f)}
    @Test fun restoredMetadataHasAUsefulDurationWithoutCallingItLive() {
        assertEquals(347000L,durationFromText("5:47"));assertEquals(3723000L,durationFromText("1:02:03"))
        assertEquals(0L,durationFromText("Live"));assertEquals(0L,durationFromText("3:99"))
    }
    @Test fun manualInputRejectsInvalidAndTransparentSeeds() {
        for(value in listOf("","abc","#12345678","GG0000","12 3456","-123456"))assertFalse(value,validSeed(value))
        assertEquals(0xff006a60.toInt(),seedColor("#006A60"))
        assertEquals(0xff6750a4.toInt(),seedColor("6750a4"))
    }
    @Test fun manualSeedsProduceAccessibleRolesInBothModes() {
        for(seed in listOf("000000","FFFFFF","FF0000","00FF00","0000FF","6750A4","006A60","984061"))for(dark in listOf(false,true)) {
            val s=tonalScheme(seedColor(seed),dark)
            for((foreground,background) in listOf(s.onSurface to s.surface,s.onSurfaceVariant to s.surfaceContainerHighest,s.onPrimary to s.primary,s.onPrimaryContainer to s.primaryContainer,s.onSecondaryContainer to s.secondaryContainer,s.onErrorContainer to s.errorContainer)) {
                assertTrue("$seed dark=$dark ratio=${contrast(foreground,background)}",contrast(foreground,background)>=4.49f)
            }
        }
    }
    @Test fun differentSeedsAffectEntirePalette() {
        val a=tonalScheme(seedColor("6750A4"),false);val b=tonalScheme(seedColor("006A60"),false)
        assertNotEquals(a.primary,b.primary);assertNotEquals(a.surfaceContainer,b.surfaceContainer);assertNotEquals(a.tertiary,b.tertiary)
        assertTrue(tonalScheme(seedColor(DEFAULT_SEED),true).surface.luminance()<a.surface.luminance())
    }
    @Test fun queueReorderingCannotMoveCurrentOrPreviousTracks() {
        assertNull(expressiveQueueDropTarget(1,2,1,5))
        assertNull(expressiveQueueDropTarget(0,2,1,5))
        assertEquals(2,expressiveQueueDropTarget(4,-100,1,5))
        assertEquals(4,expressiveQueueDropTarget(2,100,1,5))
        assertEquals(-1,expressiveQueuePreviewShift(3,2,4))
    }
}
