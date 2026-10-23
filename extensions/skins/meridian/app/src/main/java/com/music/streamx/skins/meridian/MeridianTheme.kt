package com.music.streamx.skins.meridian

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.ui.skins.skinPlayerSettings
import kotlin.math.*

internal val Edge = RoundedCornerShape(14.dp)
internal val Dial = RoundedCornerShape(50)
@Composable internal fun meridianColors(dark: Boolean): ColorScheme = if(dark) darkColorScheme(
    primary=Color(0xFFF3AB78),onPrimary=Color(0xFF332014),primaryContainer=Color(0xFF513A2A),onPrimaryContainer=Color(0xFFFFDCC3),
    secondary=Color(0xFFA6C5B6),onSecondary=Color(0xFF112C25),secondaryContainer=Color(0xFF294139),onSecondaryContainer=Color(0xFFD9EBDD),
    tertiary=Color(0xFFB7C6DC),background=Color(0xFF0C1720),onBackground=Color(0xFFF5EEDC),surface=Color(0xFF13232D),onSurface=Color(0xFFF5EEDC),
    onSurfaceVariant=Color(0xFF9FADA9),surfaceVariant=Color(0xFF1B313B),surfaceContainer=Color(0xFF13232D),surfaceContainerHigh=Color(0xFF1B313B),
    surfaceContainerLow=Color(0xFF101E27),surfaceContainerHighest=Color(0xFF253D46),outline=Color(0xFF576B70),outlineVariant=Color(0xFF2E434B)
) else lightColorScheme(primary=Color(0xFF944A29),onPrimary=Color.White,primaryContainer=Color(0xFFFFDBC5),onPrimaryContainer=Color(0xFF382012),
    secondary=Color(0xFF406457),secondaryContainer=Color(0xFFDCEADF),onSecondaryContainer=Color(0xFF193328),
    background=Color(0xFFF4F0E6),onBackground=Color(0xFF162B33),surface=Color(0xFFFFFDF5),onSurface=Color(0xFF162B33),
    onSurfaceVariant=Color(0xFF556567),surfaceVariant=Color(0xFFE7E5D9),surfaceContainer=Color(0xFFEDEADD),surfaceContainerHigh=Color(0xFFE7E5D9),
    surfaceContainerLow=Color(0xFFF8F5EB),surfaceContainerHighest=Color(0xFFDCDDD2),outline=Color(0xFF788886),outlineVariant=Color(0xFFCFD4C9))
internal fun meridianTypography()=Typography(
    displayLarge=TextStyle(fontFamily=FontFamily.Serif,fontWeight=FontWeight.Normal,fontSize=44.sp,lineHeight=48.sp,letterSpacing=(-1.5).sp),
    headlineLarge=TextStyle(fontFamily=FontFamily.Serif,fontSize=32.sp,lineHeight=36.sp),
    headlineMedium=TextStyle(fontFamily=FontFamily.Serif,fontSize=28.sp,lineHeight=33.sp),
    headlineSmall=TextStyle(fontFamily=FontFamily.Serif,fontSize=24.sp,lineHeight=29.sp),
    titleLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Medium,fontSize=20.sp,lineHeight=26.sp),
    titleMedium=TextStyle(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Medium,fontSize=16.sp,lineHeight=21.sp),
    bodyLarge=TextStyle(fontFamily=FontFamily.SansSerif,fontSize=16.sp,lineHeight=23.sp),
    labelSmall=TextStyle(fontFamily=FontFamily.Monospace,fontSize=10.sp,lineHeight=14.sp,letterSpacing=1.sp))
internal fun meridianShapes()=Shapes(extraSmall=RoundedCornerShape(4.dp),small=RoundedCornerShape(8.dp),medium=Edge,large=RoundedCornerShape(20.dp),extraLarge=RoundedCornerShape(24.dp))
@Composable internal fun Eyebrow(text: String,modifier: Modifier=Modifier) {
    Text(text.uppercase(),modifier,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
}
@Composable internal fun MeridianButton(label: String,onClick: ()->Unit,modifier: Modifier=Modifier,selected: Boolean=false) {
    val c=MaterialTheme.colorScheme
    Surface(onClick=onClick,modifier=modifier.heightIn(min=48.dp),shape=Edge,color=if(selected)c.primary else c.surfaceContainerHigh,
        contentColor=if(selected)c.onPrimary else c.onSurface,border=if(selected)null else BorderStroke(1.dp,c.outlineVariant)) {
        Box(Modifier.padding(horizontal=16.dp,vertical=12.dp),contentAlignment=Alignment.Center){Text(label,style=MaterialTheme.typography.labelLarge,maxLines=1)}
    }
}
/** Decorative dial; no fake progress or interactive affordances. */
@Composable internal fun MeridianDial(modifier: Modifier=Modifier,playing: Boolean=false) {
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val transition=rememberInfiniteTransition(label="Meridian dial")
    val phase=if(playing && !reduce) transition.animateFloat(0f,360f,infiniteRepeatable(tween(18000,easing=LinearEasing)),label="Dial rotation").value else 0f
    val c=MaterialTheme.colorScheme
    Canvas(modifier){
        val radius=min(size.width,size.height)*.44f
        repeat(60){i->val a=(i*6-90)*PI/180;val len=if(i%5==0)8.dp.toPx() else 3.dp.toPx()
            drawLine(c.outlineVariant,Offset(center.x+cos(a).toFloat()*radius,center.y+sin(a).toFloat()*radius),Offset(center.x+cos(a).toFloat()*(radius-len),center.y+sin(a).toFloat()*(radius-len)),1.dp.toPx())}
        val a=(phase-90)*PI/180
        drawCircle(c.primary,3.dp.toPx(),Offset(center.x+cos(a).toFloat()*radius,center.y+sin(a).toFloat()*radius))
        drawLine(c.outlineVariant,Offset(center.x-8.dp.toPx(),center.y),Offset(center.x+8.dp.toPx(),center.y),1.dp.toPx())
        drawLine(c.outlineVariant,Offset(center.x,center.y-8.dp.toPx()),Offset(center.x,center.y+8.dp.toPx()),1.dp.toPx())
    }
}
@Composable internal fun AppearanceConfiguration(onDismiss: ()->Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().padding(24.dp)) {
        MeridianButton("Back",onDismiss)
        Spacer(Modifier.height(32.dp));Eyebrow("Appearance / Meridian")
        Text("Quiet detail.\nFluid motion.",style=MaterialTheme.typography.displayLarge,modifier=Modifier.padding(vertical=24.dp))
        MeridianDial(Modifier.size(160.dp))
        Text("Meridian follows your app’s light or dark mode. Animations follow the reduced-animation preference in Settings.",style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=24.dp))
    }
}

/** A quiet activity indicator; stops animating on pause and under reduced motion. */
@Composable internal fun MeridianSignal(playing: Boolean,modifier: Modifier=Modifier) {
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val transition=rememberInfiniteTransition(label="Playback signal")
    val phase=if(playing && !reduce)transition.animateFloat(0f,6.2832f,infiniteRepeatable(tween(1400,easing=LinearEasing)),label="Signal rhythm").value else 0f
    val c=MaterialTheme.colorScheme
    Canvas(modifier){repeat(4){i->val h=if(playing)size.height*(.28f+.65f*abs(sin(phase+i*.9f)))else size.height*.2f
        val x=size.width*(i+.5f)/4f;drawLine(c.primary,Offset(x,(size.height-h)/2),Offset(x,(size.height+h)/2),2.dp.toPx(),cap=androidx.compose.ui.graphics.StrokeCap.Round)}}
}
