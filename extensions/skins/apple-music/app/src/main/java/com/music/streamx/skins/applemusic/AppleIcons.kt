package com.music.streamx.skins.applemusic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
internal object AppleIcons {
 val Previous get()=Icons.Rounded.FastRewind
 val Next get()=Icons.Rounded.FastForward
 val Play get()=Icons.Rounded.PlayArrow
 val Pause get()=Icons.Rounded.Pause
 /**
  * Apple Music's Library tab glyph (SF Symbol `music.note.square.stack.fill`):
  * a filled rounded square with the music note knocked out, two shorter cards
  * stacked behind it. Drawn here so it ships with the skin, not the host.
  */
 val Library: ImageVector by lazy {
  ImageVector.Builder("AppleLibrary", 24.dp, 24.dp, 24f, 24f).apply {
   path(fill = SolidColor(Color.Black)) { roundRect(7.2f, 2.0f, 9.6f, 1.3f, .65f) }
   path(fill = SolidColor(Color.Black)) { roundRect(5.2f, 4.2f, 13.6f, 1.5f, .75f) }
   // The front card, with the note cut out of it (even-odd: the note is a hole).
   path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
    roundRect(3.0f, 6.6f, 18.0f, 15.4f, 3.4f)
    // Stem and flag up to the beam, then round the note head back to the stem: one
    // outline, so nothing overlaps and the hole stays a single clean shape.
    moveTo(13.4f, 10.6f); lineTo(13.4f, 10.0f); lineTo(17.9f, 8.8f); lineTo(17.9f, 10.6f)
    lineTo(14.9f, 11.4f); lineTo(14.9f, 17.4f)
    arcTo(2.45f, 1.95f, 0f, true, true, 13.4f, 15.6f)
    close()
   }
  }.build()
 }
 fun verify(){check(listOf(Previous,Next,Play,Pause,Library).all{it.root.size>0})}
}

/** A rounded rectangle as one closed subpath. */
private fun PathBuilder.roundRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
 moveTo(x + r, y); lineTo(x + w - r, y); arcTo(r, r, 0f, false, true, x + w, y + r)
 lineTo(x + w, y + h - r); arcTo(r, r, 0f, false, true, x + w - r, y + h)
 lineTo(x + r, y + h); arcTo(r, r, 0f, false, true, x, y + h - r)
 lineTo(x, y + r); arcTo(r, r, 0f, false, true, x + r, y); close()
}
