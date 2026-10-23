package com.music.streamx.skins.applemusic

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale
import com.music.streamx.ui.skins.*

@Composable internal fun applePlayerStyle(): SkinDefaultPlayerStyle {
    val context=LocalContext.current
    val prefs=remember(context){context.getSharedPreferences("skin.apple-music",Context.MODE_PRIVATE)}
    fun read()=SkinDefaultPlayerStyle(fullCover=prefs.getBoolean("fullCover",false))
    var style by remember(prefs){mutableStateOf(read())}
    DisposableEffect(prefs){val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,_->style=read()};prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
    return style
}
@Composable internal fun appleColors(dark: Boolean)=if(dark) darkColorScheme(primary=androidx.compose.ui.graphics.Color(0xFFFA2D55),background=androidx.compose.ui.graphics.Color.Black,surface=androidx.compose.ui.graphics.Color(0xFF1C1C1E),surfaceVariant=androidx.compose.ui.graphics.Color(0xFF2C2C2E)) else lightColorScheme(primary=androidx.compose.ui.graphics.Color(0xFFD91940))
@Composable internal fun ApplePlayerConfiguration(onDismiss: ()->Unit) {
    val style=applePlayerStyle();val context=LocalContext.current
    val prefs=remember{context.getSharedPreferences("skin.apple-music",Context.MODE_PRIVATE)}
    val accent=Color(0xFFFA2D55)
    val list=rememberLazyListState()
    SkinBackHandler(true,onDismiss)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        LazyColumn(Modifier.fillMaxSize(),state=list,contentPadding=PaddingValues(start=20.dp,end=20.dp,top=76.dp,bottom=28.dp),
            verticalArrangement=Arrangement.spacedBy(20.dp)) {
            item {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(13.dp)).background(Color.White),contentAlignment=Alignment.Center) {
                        Icon(Icons.Rounded.MusicNote,null,tint=accent,modifier=Modifier.size(36.dp))
                    }
                    Column {
                        Text("Apple Music",style=MaterialTheme.typography.headlineLarge)
                        Text("Appearance",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Text("Player artwork",style=MaterialTheme.typography.titleLarge)
                Text("Choose how album artwork appears in the full player.",Modifier.padding(top=6.dp),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Row(Modifier.fillMaxWidth().selectableGroup(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    listOf("Cover" to false,"Full cover" to true).forEach{(label,full)->
                        ArtworkChoice(label,full,style.fullCover==full,Modifier.weight(1f),accent) {
                            prefs.edit().putBoolean("fullCover",full).apply()
                        }
                    }
                }
            }
            item {
                Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(if(style.fullCover)"Full cover" else "Cover",style=MaterialTheme.typography.titleMedium)
                        Text(if(style.fullCover)"Artwork fills the player and softly fades into the playback controls." else "A rounded square cover sits above the track details, with blurred artwork behind it.",
                            style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        val topFadeAlpha by remember { derivedStateOf {
            if(list.firstVisibleItemIndex>0)1f else (list.firstVisibleItemScrollOffset/120f).coerceIn(0f,1f)
        } }
        AppleMusicBottomFade(Modifier.rotate(180f).graphicsLayer{alpha=topFadeAlpha},false,MaterialTheme.colorScheme.background)
        IconButton(onClick=onDismiss,modifier=Modifier.padding(start=12.dp,top=8.dp).size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back")
        }
    }
}

@Composable private fun ArtworkChoice(label: String,full: Boolean,selected: Boolean,modifier: Modifier,accent: Color,onClick: ()->Unit) {
    Column(modifier.selectable(selected=selected,role=Role.RadioButton,onClick=onClick),horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(22.dp))
            .border(if(selected)2.dp else 1.dp,if(selected)accent else MaterialTheme.colorScheme.outlineVariant,RoundedCornerShape(22.dp))
            .padding(7.dp).clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(listOf(Color(0xFF89576C),Color(0xFF2A202C))))) {
            if(full) Box(Modifier.fillMaxWidth().height(162.dp).background(Brush.linearGradient(listOf(Color(0xFFF9B274),Color(0xFFD1346D),Color(0xFF423C83)))),contentAlignment=Alignment.Center) {
                Icon(Icons.Rounded.MusicNote,null,tint=Color.White.copy(alpha=.8f),modifier=Modifier.size(72.dp))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xFF392537)),startY=200f)))
            }
            Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Box(Modifier.width(24.dp).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha=.45f)))
                Spacer(Modifier.height(12.dp))
                if(!full) Box(Modifier.size(108.dp).clip(RoundedCornerShape(7.dp)).background(Brush.linearGradient(listOf(Color(0xFFF9B274),Color(0xFFD1346D),Color(0xFF423C83)))),contentAlignment=Alignment.Center) {
                    Icon(Icons.Rounded.MusicNote,null,tint=Color.White.copy(alpha=.8f),modifier=Modifier.size(48.dp))
                } else Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(.8f).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha=.85f)).align(Alignment.Start))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth(.5f).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha=.4f)).align(Alignment.Start))
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(alpha=.35f)))
                Row(Modifier.fillMaxWidth().padding(top=10.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
                    Icon(Icons.Rounded.FastRewind,null,Modifier.size(20.dp),tint=Color.White)
                    Icon(Icons.Rounded.Pause,null,Modifier.size(23.dp),tint=Color.White)
                    Icon(Icons.Rounded.FastForward,null,Modifier.size(20.dp),tint=Color.White)
                }
            }
        }
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
            RadioButton(selected=selected,onClick=null,colors=RadioButtonDefaults.colors(selectedColor=accent))
            Text(label,style=MaterialTheme.typography.bodyLarge)
        }
    }
}


// The reference uses text-led, inset groups rather than a leading icon column.
@Composable internal fun AppleSettingsGroup(
    header: String?, footer: String?, topSpacing: Dp, content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top = topSpacing)) {
        header?.let {
            Text(
                it.uppercase(Locale.ROOT),
                modifier = Modifier.padding(start = 36.dp, end = 36.dp, bottom = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
        ) {
            Column { content() }
        }
        footer?.let {
            Text(
                it,
                modifier = Modifier.padding(start = 36.dp, end = 36.dp, top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable internal fun AppleSettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 20.dp),
        thickness = .5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .12f),
    )
}

@Composable internal fun AppleSettingsRow(
    title: String, subtitle: String?, subtitleContent: (@Composable () -> Unit)?,
    value: String?, badge: String?, enabled: Boolean,
    onClick: (() -> Unit)?, trailing: (@Composable () -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = 64.dp)
            .alpha(if (enabled) 1f else .45f)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            if (subtitleContent != null) {
                subtitleContent()
            } else if (subtitle != null) {
                Text(
                    subtitle, modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                )
            }
            badge?.let {
                Text(it, modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall, color = Color(0xFFFA2D55))
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            value?.let {
                Text(it, modifier = Modifier.widthIn(max = 120.dp),
                    style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (onClick != null) {
                Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(18.dp),
                    tint = colors.onSurfaceVariant.copy(alpha = .6f))
            }
        }
    }
}
