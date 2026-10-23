package com.music.streamx.skins.expressive

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.streamx.skins.expressive.color.hct.Hct
import com.music.streamx.skins.expressive.color.scheme.SchemeExpressive
import com.music.streamx.ui.skins.SkinBackHandler

internal const val DEFAULT_SEED="6750A4"
internal fun validSeed(value: String)=Regex("[0-9a-fA-F]{6}").matches(value.removePrefix("#"))
internal fun seedColor(value: String): Int {
    require(validSeed(value)){"Use a six-digit hex color"}
    return (0xff000000L or value.removePrefix("#").toLong(16)).toInt()
}
private fun preferences(context: Context)=context.getSharedPreferences("skin.material-expressive-v2.appearance",Context.MODE_PRIVATE)
private data class Appearance(val wallpaper: Boolean,val seed: String,val track: Boolean)
private fun SharedPreferences.appearance()=Appearance(getBoolean("wallpaper",true),getString("seed",DEFAULT_SEED).orEmpty().takeIf(::validSeed) ?: DEFAULT_SEED,getBoolean("track",false))
@Composable private fun appearance(): Appearance {
    val context=LocalContext.current
    val prefs=remember(context){preferences(context)}
    var value by remember(prefs){mutableStateOf(prefs.appearance())}
    DisposableEffect(prefs){val listener=SharedPreferences.OnSharedPreferenceChangeListener {_,_->value=prefs.appearance()};prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
    return value
}
@Composable internal fun expressiveColors(dark: Boolean): ColorScheme {
    val context=LocalContext.current
    val config=LocalConfiguration.current
    val state=appearance()
    // Resume/theme/configuration changes re-resolve Android's wallpaper roles.
    var revision by remember{mutableIntStateOf(0)}
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME){revision++}
    val trackUrl by com.music.streamx.ui.skins.SkinTrackArtwork.url.collectAsState()
    val trackSeed=com.music.streamx.ui.skins.rememberSkinArtworkColor(if(state.track)trackUrl else null)
    return if(state.track && trackSeed!=null) {
        remember(dark,trackSeed){tonalScheme(trackSeed.toArgb(),dark)}
    } else if(state.wallpaper && Build.VERSION.SDK_INT>=31) {
        remember(dark,config,revision,state){if(dark)dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)}
    } else remember(dark,state.seed){tonalScheme(seedColor(state.seed),dark)}
}
internal fun tonalScheme(seed: Int,dark: Boolean): ColorScheme {
    val s=SchemeExpressive(Hct.fromInt(seed),dark,0.0)
    return (if(dark)darkColorScheme() else lightColorScheme()).copy(
        primary=Color(s.primary),onPrimary=Color(s.onPrimary),primaryContainer=Color(s.primaryContainer),onPrimaryContainer=Color(s.onPrimaryContainer),inversePrimary=Color(s.inversePrimary),
        secondary=Color(s.secondary),onSecondary=Color(s.onSecondary),secondaryContainer=Color(s.secondaryContainer),onSecondaryContainer=Color(s.onSecondaryContainer),
        tertiary=Color(s.tertiary),onTertiary=Color(s.onTertiary),tertiaryContainer=Color(s.tertiaryContainer),onTertiaryContainer=Color(s.onTertiaryContainer),
        background=Color(s.background),onBackground=Color(s.onBackground),surface=Color(s.surface),onSurface=Color(s.onSurface),surfaceVariant=Color(s.surfaceVariant),onSurfaceVariant=Color(s.onSurfaceVariant),surfaceTint=Color(s.primary),
        inverseSurface=Color(s.inverseSurface),inverseOnSurface=Color(s.inverseOnSurface),outline=Color(s.outline),outlineVariant=Color(s.outlineVariant),scrim=Color(s.scrim),
        surfaceDim=Color(s.surfaceDim),surfaceBright=Color(s.surfaceBright),surfaceContainerLowest=Color(s.surfaceContainerLowest),surfaceContainerLow=Color(s.surfaceContainerLow),surfaceContainer=Color(s.surfaceContainer),surfaceContainerHigh=Color(s.surfaceContainerHigh),surfaceContainerHighest=Color(s.surfaceContainerHighest),
        error=Color(s.error),onError=Color(s.onError),errorContainer=Color(s.errorContainer),onErrorContainer=Color(s.onErrorContainer))
}
internal fun expressiveTypography()=Typography().let { base->
    base.copy(displayLarge=base.displayLarge.copy(fontFamily=FontFamily.SansSerif,fontWeight=FontWeight.Bold,fontSize=44.sp,lineHeight=48.sp),headlineLarge=base.headlineLarge.copy(fontWeight=FontWeight.Bold),headlineMedium=base.headlineMedium.copy(fontWeight=FontWeight.SemiBold))
}
internal fun expressiveShapes()=Shapes(extraSmall=RoundedCornerShape(4.dp),small=RoundedCornerShape(8.dp),medium=RoundedCornerShape(20.dp),large=RoundedCornerShape(28.dp),extraLarge=RoundedCornerShape(40.dp))

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable internal fun AppearanceConfiguration(onDismiss: ()->Unit) {
    SkinBackHandler(true,onDismiss)
    val context=LocalContext.current
    val prefs=remember{preferences(context)}
    val state=appearance()
    var draft by remember(state.seed){mutableStateOf(state.seed)}
    val valid=validSeed(draft)
    Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.verticalScroll(rememberScrollState()).widthIn(max=720.dp).fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding().navigationBarsPadding().padding(start=24.dp,end=24.dp,top=88.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                Text("Skin configuration",style=MaterialTheme.typography.displayLarge)
                Text("Material Expressive",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                ListItem(headlineContent={Text("Wallpaper colors")},supportingContent={Text(if(Build.VERSION.SDK_INT>=31)"Use your Android wallpaper palette" else "Available on Android 12 and later. Manual colors are used here.")},trailingContent={Switch(checked=!state.track && state.wallpaper && Build.VERSION.SDK_INT>=31,enabled=Build.VERSION.SDK_INT>=31,onCheckedChange={prefs.edit().putBoolean("wallpaper",it).putBoolean("track",false).apply()})},colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surface))
                ListItem(headlineContent={Text("Track artwork colors")},supportingContent={Text("Build the palette from the current track’s thumbnail. Uses your wallpaper or manual color when no artwork is available.")},trailingContent={Switch(checked=state.track,onCheckedChange={prefs.edit().putBoolean("track",it).apply()})},colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surface))
                Text("Manual color",style=MaterialTheme.typography.titleLarge)
                Text("Choose a color to generate the complete light and dark palette. Changes apply across the app.",color=MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    listOf("6750A4" to "Iris","006A60" to "Jade","0061A4" to "Ocean","984061" to "Rose","825500" to "Amber","4F662C" to "Fern").forEach {(hex,name)->
                        FilterChip(selected=!state.track && (!state.wallpaper || Build.VERSION.SDK_INT<31) && state.seed==hex,onClick={prefs.edit().putBoolean("wallpaper",false).putBoolean("track",false).putString("seed",hex).apply()},label={Text(name)},leadingIcon={if(!state.track && (!state.wallpaper || Build.VERSION.SDK_INT<31) && state.seed==hex)Icon(ExpressiveSymbols.Check,null,Modifier.size(18.dp)) else Box(Modifier.size(18.dp).background(Color(seedColor(hex)),RoundedCornerShape(9.dp)))})
                    }
                }
                OutlinedTextField(value=draft,onValueChange={draft=it.removePrefix("#").take(6).uppercase()},label={Text("Hex color")},prefix={Text("#")},singleLine=true,isError=draft.isNotEmpty() && !valid,supportingText={Text(if(!valid)"Enter six hexadecimal digits, for example 6750A4" else "Generates tonal colors with accessible foreground roles")},keyboardOptions=KeyboardOptions(capitalization=KeyboardCapitalization.Characters),modifier=Modifier.fillMaxWidth())
                Button(enabled=valid,onClick={prefs.edit().putBoolean("wallpaper",false).putBoolean("track",false).putString("seed",draft.uppercase()).apply()},modifier=Modifier.heightIn(min=48.dp)) {Text("Use manual color")}
                Text("Current palette",style=MaterialTheme.typography.titleLarge)
                Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.secondaryContainer) {
                    Row(Modifier.fillMaxWidth().padding(24.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                        Icon(ExpressiveSymbols.MusicNote,null,Modifier.size(40.dp),tint=MaterialTheme.colorScheme.onSecondaryContainer)
                        Column {Text("Your music, your colors",style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.onSecondaryContainer);Text(if(state.track)"Current track palette" else if(state.wallpaper && Build.VERSION.SDK_INT>=31)"Wallpaper palette" else "Manual palette · #${state.seed}",color=MaterialTheme.colorScheme.onSecondaryContainer)}
                    }
                }
                Text("The app’s theme setting controls light and dark mode. Motion follows your reduced-motion preference.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.fillMaxWidth().height(112.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface,MaterialTheme.colorScheme.surface,Color.Transparent))))
            IconButton(onClick=onDismiss,modifier=Modifier.statusBarsPadding().padding(start=12.dp,top=8.dp).size(48.dp)){Icon(ExpressiveSymbols.ArrowBack,"Back")}
        }
    }
}
