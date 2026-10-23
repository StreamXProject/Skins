package com.music.streamx.skins.dot

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp

/** Self-contained terminal/TUI geometric icons; no calls into the host's shrinkable icon getters. */
internal object DotIcons {
    private fun glyph(name: String, draw: PathBuilder.() -> Unit) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.75f,
                strokeLineCap = StrokeCap.Square,
                strokeLineJoin = StrokeJoin.Miter,
                pathBuilder = draw,
            )
        }.build()

    val ArrowBack by lazy {
        glyph("Dot.Back") {
            moveTo(19f, 12f); lineTo(5f, 12f)
            moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
        }
    }

    val ArrowForward by lazy {
        glyph("Dot.Forward") {
            moveTo(5f, 12f); lineTo(19f, 12f)
            moveTo(13f, 6f); lineTo(19f, 12f); lineTo(13f, 18f)
        }
    }

    val KeyboardArrowRight by lazy {
        glyph("Dot.ChevronRight") {
            moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f)
        }
    }

    val KeyboardArrowDown by lazy {
        glyph("Dot.Down") {
            moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f)
        }
    }

    val Search by lazy {
        glyph("Dot.Search") {
            moveTo(15f, 9.5f)
            lineTo(15f, 15f); lineTo(9.5f, 15f); lineTo(5f, 10.5f); lineTo(5f, 5f); lineTo(10.5f, 5f); close()
            moveTo(14f, 14f); lineTo(20f, 20f)
        }
    }

    val PersonOutline by lazy {
        glyph("Dot.Person") {
            moveTo(8f, 5f); lineTo(16f, 5f); lineTo(16f, 11f); lineTo(8f, 11f); close()
            moveTo(4f, 20f); lineTo(4f, 16f); lineTo(8f, 14f); lineTo(16f, 14f); lineTo(20f, 16f); lineTo(20f, 20f)
        }
    }

    val History by lazy {
        glyph("Dot.History") {
            moveTo(12f, 4f); lineTo(20f, 4f); lineTo(20f, 20f); lineTo(4f, 20f); lineTo(4f, 8f); close()
            moveTo(12f, 8f); lineTo(12f, 13f); lineTo(16f, 13f)
        }
    }

    val FolderOpen by lazy {
        glyph("Dot.Folder") {
            moveTo(3f, 19f); lineTo(3f, 5f); lineTo(10f, 5f); lineTo(12f, 8f); lineTo(21f, 8f); lineTo(21f, 19f); close()
            moveTo(3f, 11f); lineTo(21f, 11f)
        }
    }

    val Settings by lazy {
        glyph("Dot.Settings") {
            moveTo(3f, 6f); lineTo(21f, 6f)
            moveTo(3f, 12f); lineTo(21f, 12f)
            moveTo(3f, 18f); lineTo(21f, 18f)
            moveTo(8f, 4f); lineTo(8f, 8f)
            moveTo(16f, 10f); lineTo(16f, 14f)
            moveTo(10f, 16f); lineTo(10f, 20f)
        }
    }

    val Explore by lazy {
        glyph("Dot.Explore") {
            moveTo(3f, 3f); lineTo(21f, 3f); lineTo(21f, 21f); lineTo(3f, 21f); close()
            moveTo(16f, 8f); lineTo(13.5f, 13.5f); lineTo(8f, 16f); lineTo(10.5f, 10.5f); close()
        }
    }

    val Pause by lazy {
        glyph("Dot.Pause") {
            moveTo(8f, 5f); lineTo(8f, 19f)
            moveTo(16f, 5f); lineTo(16f, 19f)
        }
    }

    val PlayArrow by lazy {
        glyph("Dot.Play") {
            moveTo(7f, 4f); lineTo(19f, 12f); lineTo(7f, 20f); close()
        }
    }

    val SkipNext by lazy {
        glyph("Dot.Next") {
            moveTo(5f, 5f); lineTo(15f, 12f); lineTo(5f, 19f); close()
            moveTo(19f, 5f); lineTo(19f, 19f)
        }
    }

    val SkipPrevious by lazy {
        glyph("Dot.Previous") {
            moveTo(19f, 5f); lineTo(9f, 12f); lineTo(19f, 19f); close()
            moveTo(5f, 5f); lineTo(5f, 19f)
        }
    }

    val Add by lazy {
        glyph("Dot.Add") {
            moveTo(12f, 4f); lineTo(12f, 20f)
            moveTo(4f, 12f); lineTo(20f, 12f)
        }
    }

    val LibraryAdd by lazy {
        glyph("Dot.LibraryAdd") {
            moveTo(4f, 4f); lineTo(16f, 4f); lineTo(16f, 18f); lineTo(4f, 18f); close()
            moveTo(10f, 8f); lineTo(10f, 14f)
            moveTo(7f, 11f); lineTo(13f, 11f)
            moveTo(19f, 7f); lineTo(20f, 7f); lineTo(20f, 21f); lineTo(7f, 21f)
        }
    }

    val MoreHoriz by lazy {
        glyph("Dot.More") {
            moveTo(5f, 12f); lineTo(7f, 12f)
            moveTo(11f, 12f); lineTo(13f, 12f)
            moveTo(17f, 12f); lineTo(19f, 12f)
        }
    }

    val Shuffle by lazy {
        glyph("Dot.Shuffle") {
            moveTo(3f, 6f); lineTo(8f, 6f); lineTo(16f, 18f); lineTo(21f, 18f)
            moveTo(3f, 18f); lineTo(8f, 18f); lineTo(16f, 6f); lineTo(21f, 6f)
            moveTo(18f, 3f); lineTo(21f, 6f); lineTo(18f, 9f)
            moveTo(18f, 15f); lineTo(21f, 18f); lineTo(18f, 21f)
        }
    }

    val Repeat by lazy {
        glyph("Dot.Repeat") {
            moveTo(4f, 11f); lineTo(4f, 7f); lineTo(20f, 7f); lineTo(20f, 13f)
            moveTo(17f, 4f); lineTo(20f, 7f); lineTo(17f, 10f)
            moveTo(20f, 13f); lineTo(20f, 17f); lineTo(4f, 17f); lineTo(4f, 11f)
            moveTo(7f, 14f); lineTo(4f, 17f); lineTo(7f, 20f)
        }
    }

    val RepeatOne get() = Repeat

    val FavoriteBorder by lazy {
        glyph("Dot.FavBorder") {
            moveTo(12f, 20f); lineTo(4f, 12f); lineTo(4f, 6f); lineTo(9f, 4f); lineTo(12f, 7f); lineTo(15f, 4f); lineTo(20f, 6f); lineTo(20f, 12f); close()
        }
    }

    val Favorite by lazy {
        ImageVector.Builder("Dot.FavSolid", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 20f); lineTo(4f, 12f); lineTo(4f, 6f); lineTo(9f, 4f); lineTo(12f, 7f); lineTo(15f, 4f); lineTo(20f, 6f); lineTo(20f, 12f); close()
            }
        }.build()
    }

    val SpeakerGroup by lazy {
        glyph("Dot.AudioOutput") {
            moveTo(4f, 9f); lineTo(8f, 9f); lineTo(13f, 4f); lineTo(13f, 20f); lineTo(8f, 15f); lineTo(4f, 15f); close()
            moveTo(17f, 8f); lineTo(17f, 16f)
            moveTo(20f, 5f); lineTo(20f, 19f)
        }
    }

    val ClearAll by lazy {
        glyph("Dot.Clear") {
            moveTo(4f, 6f); lineTo(20f, 6f)
            moveTo(4f, 12f); lineTo(16f, 12f)
            moveTo(4f, 18f); lineTo(12f, 18f)
        }
    }

    val GraphicEq by lazy {
        glyph("Dot.Eq") {
            moveTo(5f, 8f); lineTo(5f, 16f)
            moveTo(10f, 4f); lineTo(10f, 20f)
            moveTo(15f, 7f); lineTo(15f, 17f)
            moveTo(20f, 10f); lineTo(20f, 14f)
        }
    }

    val Home by lazy {
        glyph("Dot.Home") {
            moveTo(3f, 11f); lineTo(12f, 3f); lineTo(21f, 11f)
            moveTo(6f, 10f); lineTo(6f, 21f); lineTo(18f, 21f); lineTo(18f, 10f)
            moveTo(10f, 21f); lineTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 21f)
        }
    }

    val Library by lazy {
        glyph("Dot.Library") {
            moveTo(4f, 5f); lineTo(20f, 5f)
            moveTo(4f, 11f); lineTo(20f, 11f)
            moveTo(4f, 17f); lineTo(20f, 17f)
            moveTo(2f, 3f); lineTo(2f, 21f)
        }
    }

    fun verify() {
        listOf(ArrowBack, ArrowForward, KeyboardArrowRight, Search, PlayArrow, Pause, FavoriteBorder, SpeakerGroup)
            .forEach { require(it.defaultWidth.value > 0) }
    }
}
