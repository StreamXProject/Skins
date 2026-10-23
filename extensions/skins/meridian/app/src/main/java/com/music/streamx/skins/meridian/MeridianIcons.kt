package com.music.streamx.skins.meridian

import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp

/** Self-contained line icons; no calls into the host's shrinkable icon getters. */
internal object MeridianIcons {
    private fun glyph(name: String,draw: PathBuilder.()->Unit)=ImageVector.Builder(name,24.dp,24.dp,24f,24f).apply {
        path(fill=null,stroke=SolidColor(Color.Black),strokeLineWidth=1.75f,strokeLineCap=StrokeCap.Round,strokeLineJoin=StrokeJoin.Round,pathBuilder=draw)
    }.build()
    val ArrowBack by lazy {glyph("Meridian.Back"){moveTo(20f,12f);lineTo(4f,12f);moveTo(10f,5f);lineTo(3f,12f);lineTo(10f,19f)}}
    val ArrowForward by lazy {glyph("Meridian.Forward"){moveTo(4f,12f);lineTo(20f,12f);moveTo(14f,5f);lineTo(21f,12f);lineTo(14f,19f)}}
    val KeyboardArrowRight by lazy {glyph("Meridian.Chevron"){moveTo(9f,5f);lineTo(16f,12f);lineTo(9f,19f)}}
    val KeyboardArrowDown by lazy {glyph("Meridian.Down"){moveTo(5f,9f);lineTo(12f,16f);lineTo(19f,9f)}}
    val Search by lazy {glyph("Meridian.Search"){moveTo(16f,10f);curveTo(16f,18f,4f,18f,4f,10f);curveTo(4f,2f,16f,2f,16f,10f);moveTo(15f,15f);lineTo(21f,21f)}}
    val PersonOutline by lazy {glyph("Meridian.Person"){moveTo(16f,7f);curveTo(16f,12f,8f,12f,8f,7f);curveTo(8f,2f,16f,2f,16f,7f);moveTo(4f,21f);curveTo(4f,12f,20f,12f,20f,21f)}}
    val History by lazy {glyph("Meridian.History"){moveTo(3f,10f);curveTo(4f,-1f,21f,1f,21f,12f);curveTo(21f,23f,5f,24f,3f,15f);moveTo(3f,4f);lineTo(3f,10f);lineTo(9f,10f);moveTo(12f,7f);lineTo(12f,12f);lineTo(16f,14f)}}
    val FolderOpen by lazy {glyph("Meridian.Folder"){moveTo(3f,19f);lineTo(3f,5f);lineTo(10f,5f);lineTo(12f,8f);lineTo(21f,8f);lineTo(21f,19f);close();moveTo(3f,11f);lineTo(21f,11f)}}
    val Settings by lazy {glyph("Meridian.Settings"){moveTo(4f,7f);lineTo(20f,7f);moveTo(4f,17f);lineTo(20f,17f);moveTo(8f,4f);lineTo(8f,10f);moveTo(16f,14f);lineTo(16f,20f)}}
    val Explore by lazy {glyph("Meridian.Explore"){moveTo(12f,2f);curveTo(25f,2f,25f,22f,12f,22f);curveTo(-1f,22f,-1f,2f,12f,2f);moveTo(16f,7f);lineTo(14f,14f);lineTo(7f,17f);lineTo(10f,10f);close()}}
    val Pause by lazy {glyph("Meridian.Pause"){moveTo(8f,5f);lineTo(8f,19f);moveTo(16f,5f);lineTo(16f,19f)}}
    val PlayArrow by lazy {glyph("Meridian.Play"){moveTo(8f,4f);lineTo(20f,12f);lineTo(8f,20f);close()}}
    val SkipNext by lazy {glyph("Meridian.Next"){moveTo(5f,5f);lineTo(15f,12f);lineTo(5f,19f);close();moveTo(19f,5f);lineTo(19f,19f)}}
    val SkipPrevious by lazy {glyph("Meridian.Previous"){moveTo(19f,5f);lineTo(9f,12f);lineTo(19f,19f);close();moveTo(5f,5f);lineTo(5f,19f)}}
    val Add by lazy {glyph("Meridian.Add"){moveTo(12f,4f);lineTo(12f,20f);moveTo(4f,12f);lineTo(20f,12f)}}
    val LibraryAdd by lazy {glyph("Meridian.Save"){moveTo(6f,3f);lineTo(18f,3f);lineTo(18f,21f);lineTo(12f,17f);lineTo(6f,21f);close();moveTo(9f,9f);lineTo(15f,9f);moveTo(12f,6f);lineTo(12f,12f)}}
    val MoreHoriz by lazy {glyph("Meridian.More"){moveTo(5f,12f);lineTo(5.2f,12f);moveTo(12f,12f);lineTo(12.2f,12f);moveTo(19f,12f);lineTo(19.2f,12f)}}
    val Shuffle by lazy {glyph("Meridian.Shuffle"){moveTo(3f,6f);lineTo(7f,6f);lineTo(17f,18f);lineTo(21f,18f);moveTo(3f,18f);lineTo(7f,18f);lineTo(17f,6f);lineTo(21f,6f);moveTo(18f,3f);lineTo(21f,6f);lineTo(18f,9f);moveTo(18f,15f);lineTo(21f,18f);lineTo(18f,21f)}}
    val Repeat by lazy {glyph("Meridian.Repeat"){moveTo(4f,10f);lineTo(4f,6f);lineTo(20f,6f);moveTo(17f,3f);lineTo(20f,6f);lineTo(17f,9f);moveTo(20f,14f);lineTo(20f,18f);lineTo(4f,18f);moveTo(7f,15f);lineTo(4f,18f);lineTo(7f,21f)}}
    val RepeatOne get()=Repeat
    val FavoriteBorder by lazy {glyph("Meridian.Favourite"){moveTo(12f,21f);lineTo(4f,13f);curveTo(-3f,5f,7f,-1f,12f,6f);curveTo(17f,-1f,27f,5f,20f,13f);close()}}
    val Favorite by lazy {ImageVector.Builder("Meridian.FavouriteSelected",24.dp,24.dp,24f,24f).apply {
        path(fill=SolidColor(Color.Black)){moveTo(12f,21f);lineTo(4f,13f);curveTo(-3f,5f,7f,-1f,12f,6f);curveTo(17f,-1f,27f,5f,20f,13f);close()}
    }.build()}
    val SpeakerGroup by lazy {glyph("Meridian.Output"){moveTo(3f,8f);lineTo(7f,8f);lineTo(12f,4f);lineTo(12f,20f);lineTo(7f,16f);lineTo(3f,16f);close();moveTo(16f,8f);curveTo(19f,10f,19f,14f,16f,16f);moveTo(19f,4f);curveTo(25f,8f,25f,16f,19f,20f)}}
    val ClearAll by lazy {glyph("Meridian.Clear"){moveTo(4f,6f);lineTo(20f,6f);moveTo(7f,12f);lineTo(20f,12f);moveTo(10f,18f);lineTo(20f,18f)}}
    val GraphicEq by lazy {glyph("Meridian.Signal"){moveTo(4f,9f);lineTo(4f,15f);moveTo(9f,4f);lineTo(9f,20f);moveTo(14f,7f);lineTo(14f,17f);moveTo(19f,10f);lineTo(19f,14f)}}
    val Home by lazy {glyph("Meridian.Home"){moveTo(3f,10f);lineTo(12f,3f);lineTo(21f,10f);moveTo(5f,9f);lineTo(5f,21f);lineTo(10f,21f);lineTo(10f,15f);lineTo(14f,15f);lineTo(14f,21f);lineTo(19f,21f);lineTo(19f,9f)}}
    val Library by lazy {glyph("Meridian.Library"){moveTo(4f,4f);lineTo(4f,20f);moveTo(9f,4f);lineTo(9f,20f);moveTo(14f,5f);lineTo(19f,4f);lineTo(22f,19f);lineTo(17f,20f);close()}}
    fun verify() {listOf(ArrowBack,ArrowForward,KeyboardArrowRight,Search,PlayArrow,Pause,FavoriteBorder,SpeakerGroup).forEach{require(it.defaultWidth.value>0)}}
}
