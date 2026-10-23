package com.music.streamx.skins.dot

import kotlinx.serialization.json.*
import java.io.File
import kotlin.math.pow
import org.junit.Assert.*
import org.junit.Test

/** The host rejects the whole APK if either palette fails its accessibility contract. */
class SkinContractTest {
    @Test
    fun shippedPalettesMeetHostContrastRequirements() {
        val asset = Json.parseToJsonElement(File("src/main/assets/streamx/skin.json").readText()).jsonObject
        for (mode in listOf("dark", "light")) {
            val palette = asset.getValue(mode).jsonObject
            fun lum(key: String): Double {
                val hex = palette.getValue(key).jsonPrimitive.content.removePrefix("#").toInt(16)
                fun linear(v: Int): Double {
                    val s = v / 255.0
                    return if (s <= .04045) s / 12.92 else ((s + .055) / 1.055).pow(2.4)
                }
                return linear((hex shr 16) and 255) * .2126 + linear((hex shr 8) and 255) * .7152 + linear(hex and 255) * .0722
            }
            for (ink in listOf("text", "secondary")) {
                for (surface in listOf("background", "surface", "surfaceVariant")) {
                    val a = lum(ink)
                    val b = lum(surface)
                    assertTrue("$mode $ink on $surface", (maxOf(a, b) + .05) / (minOf(a, b) + .05) >= 4.5)
                }
            }
        }
    }

    @Test
    fun fontsAreBundledWithTheirLicenses() {
        val asset = Json.parseToJsonElement(File("src/main/assets/streamx/skin.json").readText()).jsonObject
        val fonts = asset.getValue("fonts").jsonObject
        for (key in listOf("regular", "bold")) {
            val font = File("src/main/assets/" + fonts.getValue(key).jsonPrimitive.content)
            assertTrue(font.name, font.length() in 1..2097152)
            assertArrayEquals(byteArrayOf(0, 1, 0, 0), font.readBytes().take(4).toByteArray())
        }
        assertTrue(File("src/main/assets/licenses/plex-mono-OFL.txt").readText().contains("SIL OPEN FONT LICENSE"))
    }

    @Test
    fun rendererImplementsTheCurrentHostCapabilities() {
        com.music.streamx.ui.skins.validateSkinThemeImplementation(DotRenderer::class.java)
        assertEquals(com.music.streamx.ui.skins.SKIN_UI_API_VERSION, DotRenderer().apiVersion)
    }
}
