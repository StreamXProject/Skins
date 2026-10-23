package com.music.streamx.skins.looper

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.streamx.ui.skins.SkinBackHandler
import com.music.streamx.skins.looper.color.hct.Hct

internal val Capsule=RoundedCornerShape(50)
internal val Panel=RoundedCornerShape(20.dp)
internal val DialogShape=RoundedCornerShape(28.dp)
internal val Ink=Color(0xFF0A0A0A)
internal val Dim=Color(0xFF999999)
internal val Pink=Color(0xFFF4427D)
internal data class LooperOptions(val segmented: Boolean,val quickPicks: Boolean)
internal object LooperPreferences { var quickPicks=true }
@Composable internal fun looperOptions(): LooperOptions {
    val context=LocalContext.current
    val prefs=remember(context){context.getSharedPreferences("skin.looper",Context.MODE_PRIVATE)}
    var value by remember(prefs){mutableStateOf(LooperOptions(prefs.getBoolean("segmented-v2",true),prefs.getBoolean("quickPicks",true)))}
    DisposableEffect(prefs){val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,_->value=LooperOptions(prefs.getBoolean("segmented-v2",true),prefs.getBoolean("quickPicks",true))};prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
    LooperPreferences.quickPicks=value.quickPicks
    return value
}
@Composable internal fun looperColors(dark: Boolean): ColorScheme {looperOptions();return darkColorScheme(primary=Pink,onPrimary=Color.Black,secondary=Pink,background=Color.Black,onBackground=Color.White,surface=Ink,onSurface=Color.White,surfaceVariant=Color(0xFF181818),onSurfaceVariant=Dim,surfaceContainerLowest=Color.Black,surfaceContainerLow=Ink,surfaceContainer=Ink,surfaceContainerHigh=Color(0xFF141414),surfaceContainerHighest=Color(0xFF202020),outlineVariant=Color(0xFF202020))}
internal fun looperTypography()=Typography().let{it.copy(displayLarge=it.displayLarge.copy(fontSize=24.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold),headlineLarge=it.headlineLarge.copy(fontSize=24.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold),headlineMedium=it.headlineMedium.copy(fontSize=24.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold),headlineSmall=it.headlineSmall.copy(fontSize=22.sp,lineHeight=28.sp,fontWeight=FontWeight.Bold),titleLarge=it.titleLarge.copy(fontSize=18.sp,lineHeight=25.sp),titleMedium=it.titleMedium.copy(fontSize=16.sp,lineHeight=22.sp,fontWeight=FontWeight.SemiBold),bodyLarge=it.bodyLarge.copy(fontSize=14.sp,lineHeight=20.sp),bodyMedium=it.bodyMedium.copy(fontSize=12.sp,lineHeight=18.sp),bodySmall=it.bodySmall.copy(fontSize=12.sp,lineHeight=18.sp))}
internal fun looperShapes()=Shapes(extraSmall=RoundedCornerShape(8.dp),small=RoundedCornerShape(12.dp),medium=Panel,large=RoundedCornerShape(28.dp),extraLarge=DialogShape)
internal data class PlayerColors(val top: Color,val bottom: Color,val accent: Color)
internal fun playerColors(seed: Int?): PlayerColors {
    val h=Hct.fromInt(seed ?: 0xff68626f.toInt())
    fun tone(t: Double)=Color(Hct.from(h.hue,h.chroma.coerceAtMost(64.0),t).toInt())
    return PlayerColors(tone(24.0),tone(5.0),tone(72.0))
}
@Composable internal fun AppearanceConfiguration(onDismiss: ()->Unit) {
    val options=looperOptions();val context=LocalContext.current
    SkinBackHandler(true,onDismiss)
    Surface(Modifier.fillMaxSize(),color=Color.Black){Column(Modifier.statusBarsPadding().navigationBarsPadding().padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){
        Row {IconButton(onClick=onDismiss){Icon(LooperIcons.Back,"Back")};Text("Looper settings",style=MaterialTheme.typography.headlineLarge)}
        Text("Player and lyrics colors follow the track artwork.",color=Dim)
        LooperSettingsRow(null,null,"Segmented Looper controls","Use the repository’s connected end shapes. Off keeps every button fully rounded.",null,null,null,true,null){Switch(options.segmented,{context.getSharedPreferences("skin.looper",Context.MODE_PRIVATE).edit().putBoolean("segmented-v2",it).apply()})}
    }}
}
internal fun mapLooperIcon(icon: androidx.compose.ui.graphics.vector.ImageVector)=when(icon.name.substringAfterLast('.')){"Home"->LooperIcons.Home;"Search"->LooperIcons.Search;"Settings"->LooperIcons.Settings;"LibraryMusic","LibraryBooks"->LooperIcons.Library;"MusicNote"->LooperIcons.Songs;"PlayArrow"->LooperIcons.Play;"Pause"->LooperIcons.Pause;"SkipNext"->LooperIcons.Next;"SkipPrevious"->LooperIcons.Previous;"QueueMusic"->LooperIcons.Queue;"Lyrics"->LooperIcons.Lyrics;"ArrowBack"->LooperIcons.Back;else->icon}
internal fun verifyLooperIcons(){check(listOf(LooperIcons.Home,LooperIcons.Play,LooperIcons.Pause,LooperIcons.Queue,LooperIcons.Lyrics).all{it.root.size>0})}
