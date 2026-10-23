package com.music.streamx.skins.looper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import kotlinx.serialization.json.*
class LooperDataTest {
    private fun contrast(a: Color,b: Color): Float {val x=a.luminance();val y=b.luminance();return(maxOf(x,y)+.05f)/(minOf(x,y)+.05f)}
    @Test fun durationsRejectInvalidValuesAndOverflow(){
        assertEquals(209000L,durationFromText("3:29"));assertEquals(3723000L,durationFromText("1:02:03"))
        listOf(null,"","Live","3:99","-1:30","999999999999999999:00","9223372036854775807:00").forEach{assertEquals(it,0L,durationFromText(it))}
    }
    @Test fun artworkChangesBothBackgroundStops(){
        val a=playerColors(0xffff0000.toInt());val b=playerColors(0xff0000ff.toInt())
        assertNotEquals(a.top,b.top);assertNotEquals(a.bottom,b.bottom)
        listOf(null,0xffffffff.toInt(),0xff000000.toInt(),0xff00ff00.toInt(),0xffff0000.toInt()).forEach{seed->val p=playerColors(seed);assertTrue(contrast(Color.White,p.top)>=4.5f);assertTrue(contrast(Color.White,p.bottom)>=4.5f)}
    }
    @Test fun queueMovesCannotDisplaceTheCurrentTrack(){
        assertNull(queueTarget(1,2,1,5));assertNull(queueTarget(-1,2,1,5));assertNull(queueTarget(5,1,1,5))
        assertEquals(2,queueTarget(4,-100,1,5));assertEquals(4,queueTarget(2,100,1,5));assertEquals(-1,queueShift(3,2,4));assertEquals(1,queueShift(3,4,2))
    }
    @Test fun packagedPalettesPassInstallationContrast(){
        val spec=Json.parseToJsonElement(File("src/main/assets/streamx/skin.json").readText()).jsonObject
        fun color(p: JsonObject,k: String)=Color(0xff000000.toInt() or p.getValue(k).jsonPrimitive.content.removePrefix("#").toInt(16))
        listOf("light","dark").forEach{mode->val p=spec.getValue(mode).jsonObject
            listOf("background","surface","surfaceVariant").forEach{surface->listOf("text","secondary").forEach{text->assertTrue("$mode $text $surface",contrast(color(p,text),color(p,surface))>=4.5f)}}
            assertTrue(contrast(if(mode=="dark")Color.White else Color.Black,color(p,"background"))>=4.5f)
        }
    }
    @Test fun fontAssetsAndLicenseAreIncluded(){
        val spec=Json.parseToJsonElement(File("src/main/assets/streamx/skin.json").readText()).jsonObject
        spec.getValue("fonts").jsonObject.values.forEach{val path=it.jsonPrimitive.content;assertTrue(path.matches(Regex("streamx/fonts/[a-zA-Z0-9_-]+\\.(otf|ttf)")));assertTrue(File("src/main/assets/$path").length() in 1..2097152)}
        assertTrue(File("src/main/assets/streamx/fonts/OFL.txt").isFile);assertTrue(File("src/main/assets/LOOPER_LICENSE.md").isFile)
    }
}
