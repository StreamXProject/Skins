package com.music.streamx.skins.comic

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.streamx.ui.skins.skinPlayerSettings
import kotlin.math.*

internal val Edge=RoundedCornerShape(0.dp)
internal val Dial=RoundedCornerShape(50)
internal val ComicRed=Color(0xFFF04432)
internal val ComicYellow=Color(0xFFFFE648)
// Follows the app's light/dark mode unless an edition is picked: Ink is the dark one.
internal object ComicOptions {
    var edition by mutableStateOf("auto")
    private var loaded=false
    fun load(context: Context){if(!loaded){edition=context.getSharedPreferences("comic_appearance",0).getString("edition","auto") ?: "auto";loaded=true}}
    fun save(context: Context,value: String){edition=value;context.getSharedPreferences("comic_appearance",0).edit().putString("edition",value).apply()}
}
@Composable internal fun comicColors(dark: Boolean): ColorScheme {
    val context=LocalContext.current
    SideEffect {ComicOptions.load(context)}
    val ink=ComicOptions.edition=="ink" || (ComicOptions.edition=="auto" && dark)
    return if(ink) darkColorScheme(primary=ComicYellow,onPrimary=Color(0xFF191919),primaryContainer=Color(0xFFF04432),onPrimaryContainer=Color(0xFF191919),
        secondary=Color(0xFFFF927D),onSecondary=Color(0xFF191919),secondaryContainer=Color(0xFF462D28),onSecondaryContainer=Color(0xFFFFF5DD),
        background=Color(0xFF19191B),onBackground=Color(0xFFFFF5DD),surface=Color(0xFF242426),onSurface=Color(0xFFFFF5DD),
        surfaceContainer=Color(0xFF242426),surfaceContainerHigh=Color(0xFF303033),surfaceContainerLow=Color(0xFF202022),surfaceContainerHighest=Color(0xFF38383A),
        surfaceVariant=Color(0xFF303033),onSurfaceVariant=Color(0xFFC6BEAC),outline=Color(0xFFFFF5DD),outlineVariant=Color(0xFF8B867C))
    else lightColorScheme(primary=Color(0xFFB4271D),onPrimary=Color(0xFFFFFAEB),primaryContainer=ComicYellow,onPrimaryContainer=Color(0xFF191919),
        secondary=Color(0xFF1852A0),onSecondary=Color.White,secondaryContainer=Color(0xFFFFE648),onSecondaryContainer=Color(0xFF191919),
        background=Color(0xFFF4EEDC),onBackground=Color(0xFF191919),surface=Color(0xFFFFFAEB),onSurface=Color(0xFF191919),
        surfaceContainer=Color(0xFFF4EEDC),surfaceContainerHigh=Color(0xFFEBE2CB),surfaceContainerLow=Color(0xFFFFFAEB),surfaceContainerHighest=Color(0xFFDFD4BA),
        surfaceVariant=Color(0xFFEBE2CB),onSurfaceVariant=Color(0xFF595449),outline=Color(0xFF191919),outlineVariant=Color(0xFF191919))
}
// The manifest binds Comic Neue at weight 400 and Bangers at 700, offline.
internal fun comicTypography()=Typography(
    displayLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=48.sp,lineHeight=49.sp,letterSpacing=.5.sp),
    headlineLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=36.sp,lineHeight=38.sp,letterSpacing=.6.sp),
    headlineMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=30.sp,lineHeight=33.sp,letterSpacing=.5.sp),
    headlineSmall=TextStyle(fontWeight=FontWeight.Bold,fontSize=26.sp,lineHeight=29.sp),
    titleLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=24.sp,lineHeight=27.sp),
    titleMedium=TextStyle(fontWeight=FontWeight.Normal,fontSize=18.sp,lineHeight=22.sp),
    bodyLarge=TextStyle(fontWeight=FontWeight.Normal,fontSize=17.sp,lineHeight=23.sp),
    bodyMedium=TextStyle(fontWeight=FontWeight.Normal,fontSize=15.sp,lineHeight=20.sp),
    labelLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=18.sp,lineHeight=22.sp,letterSpacing=.6.sp),
    labelSmall=TextStyle(fontWeight=FontWeight.Normal,fontSize=11.sp,lineHeight=14.sp,letterSpacing=1.4.sp))
internal fun comicShapes()=Shapes(extraSmall=Edge,small=Edge,medium=Edge,large=Edge,extraLarge=Edge)
internal fun Modifier.inkShadow(ink: Color,offset: Dp=4.dp,shape: Shape=Edge)=drawBehind {
    val outline=shape.createOutline(size,layoutDirection,this)
    with(drawContext.canvas){save();translate(offset.toPx(),offset.toPx());drawOutline(outline,Paint().apply{color=ink});restore()}
}
@Composable internal fun Eyebrow(text: String,modifier: Modifier=Modifier){Text(text.uppercase(),modifier,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}
@Composable internal fun ComicButton(label: String,onClick: ()->Unit,modifier: Modifier=Modifier,selected: Boolean=false) {
    val c=MaterialTheme.colorScheme
    Surface(onClick=onClick,modifier=modifier.heightIn(min=48.dp).inkShadow(c.onSurface,3.dp),shape=Edge,
        color=if(selected)ComicYellow else c.surface,contentColor=if(selected)Color(0xFF191919)else c.onSurface,border=BorderStroke(2.dp,c.onSurface)) {
        Box(Modifier.padding(horizontal=12.dp,vertical=10.dp),contentAlignment=Alignment.Center){Text(label.uppercase(),style=MaterialTheme.typography.labelLarge,maxLines=1)}
    }
}
@Composable internal fun ComicBurst(label: String,modifier: Modifier=Modifier){
    Box(modifier.size(60.dp),contentAlignment=Alignment.Center){
        Canvas(Modifier.fillMaxSize()) {val p=Path();repeat(24){i->val a=(i*15-90)*PI/180;val r=min(size.width,size.height)*(if(i%2==0).5f else .34f);val x=center.x+cos(a).toFloat()*r;val y=center.y+sin(a).toFloat()*r;if(i==0)p.moveTo(x,y)else p.lineTo(x,y)};p.close();drawPath(p,ComicYellow)}
        Text(label,style=MaterialTheme.typography.labelLarge,color=Color(0xFF191919),modifier=Modifier.padding(8.dp))
    }
}
@Composable internal fun ComicSignal(playing: Boolean,modifier: Modifier=Modifier){
    val reduce by skinPlayerSettings().reduceAnimation.collectAsStateWithLifecycle()
    val transition=rememberInfiniteTransition(label="Comic rhythm")
    val phase=if(playing && !reduce)transition.animateFloat(0f,6.2832f,infiniteRepeatable(tween(1000,easing=LinearEasing)),label="Rhythm").value else 0f
    val c=MaterialTheme.colorScheme
    Canvas(modifier){repeat(4){i->val h=if(playing)size.height*(.3f+.6f*abs(sin(phase+i)))else size.height*.22f;val x=size.width*(i+.5f)/4f;drawLine(c.primary,Offset(x,(size.height-h)/2),Offset(x,(size.height+h)/2),2.dp.toPx())}}
}
@Composable internal fun ComicDial(modifier: Modifier=Modifier,playing: Boolean=false){ComicBurst("PLAY!",modifier)}
@Composable internal fun AppearanceConfiguration(onDismiss: ()->Unit){
    val context=LocalContext.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp)) {
        ComicButton("Back",onDismiss);Spacer(Modifier.height(32.dp));ComicBurst("HEY!");Text("PICK YOUR\nEDITION.",style=MaterialTheme.typography.displayLarge,modifier=Modifier.padding(vertical=24.dp))
        listOf("auto" to "Follow app mode","paper" to "Paper edition · light","ink" to "Ink edition · dark").forEach{(id,label)->ComicButton(label,{ComicOptions.save(context,id)},Modifier.fillMaxWidth().padding(bottom=14.dp),ComicOptions.edition==id)}
        Text("Bangers headings. Comic Neue text. Bold ink panels, paper surfaces and a little mischief.",modifier=Modifier.padding(top=16.dp),style=MaterialTheme.typography.bodyLarge)
        Text("Fonts licensed under the SIL Open Font License. Both families are bundled for offline use.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=16.dp))
    }
}
