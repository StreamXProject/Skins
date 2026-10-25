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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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
/**
 * The skin's one setting, how the full player shows the artwork, laid out as an
 * iOS Settings page: a large title that hands over to the bar as it scrolls,
 * and one inset group holding the two looks side by side, drawn with the cover
 * that is playing so the choice is between real pictures, not placeholders.
 */
@Composable internal fun ApplePlayerConfiguration(onDismiss: ()->Unit) {
    val style=applePlayerStyle();val context=LocalContext.current
    val prefs=remember{context.getSharedPreferences("skin.apple-music",Context.MODE_PRIVATE)}
    val accent=Color(0xFFFA2D55)
    val list=rememberLazyListState()
    val artwork by SkinTrackArtwork.url.collectAsState()
    val tint=rememberSkinArtworkColor(artwork) ?: Color(0xFF6E3A4E)
    SkinBackHandler(true,onDismiss)
    val titleUp by remember{derivedStateOf{list.firstVisibleItemIndex>0 || list.firstVisibleItemScrollOffset>48}}
    val titleAlpha by androidx.compose.animation.core.animateFloatAsState(if(titleUp)1f else 0f,label="settingsTitle")
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        LazyColumn(Modifier.fillMaxSize(),state=list,contentPadding=PaddingValues(top=60.dp,bottom=32.dp)) {
            item {
                Text("Apple Music",Modifier.padding(start=20.dp,end=20.dp,top=4.dp),
                    style=MaterialTheme.typography.displayLarge.copy(fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=(-.4).sp))
            }
            item {
                AppleSettingsGroup("Player Artwork",
                    if(style.fullCover)"The artwork fills the top of the player and fades into the playback controls."
                    else "A rounded cover sits above the track details, on its colours blurred behind it.",18.dp) {
                    Row(Modifier.fillMaxWidth().selectableGroup().padding(start=20.dp,end=20.dp,top=22.dp,bottom=18.dp),
                        horizontalArrangement=Arrangement.spacedBy(20.dp)) {
                        listOf("Cover" to false,"Full Cover" to true).forEach{(label,full)->
                            ArtworkChoice(label,full,style.fullCover==full,artwork,tint,accent,Modifier.weight(1f)) {
                                prefs.edit().putBoolean("fullCover",full).apply()
                            }
                        }
                    }
                }
            }
        }
        AppleMusicBottomFade(Modifier.rotate(180f).graphicsLayer{alpha=titleAlpha},false,MaterialTheme.colorScheme.background)
        Text("Apple Music",Modifier.align(Alignment.TopCenter).padding(top=19.dp).graphicsLayer{alpha=titleAlpha},
            style=MaterialTheme.typography.titleMedium.copy(fontWeight=FontWeight.SemiBold))
        IconButton(onClick=onDismiss,modifier=Modifier.padding(start=12.dp,top=4.dp).liquidPress().size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back")
        }
    }
}

/**
 * One look: a phone-shaped preview of the player in it, its name, and iOS's
 * round checkmark under it. Pressing sinks the preview a little, as iOS's
 * appearance pickers do, instead of flashing a highlight over the whole column.
 */
@Composable private fun ArtworkChoice(label: String,full: Boolean,selected: Boolean,artwork: String?,tint: Color,accent: Color,modifier: Modifier,onClick: ()->Unit) {
    val press=remember{androidx.compose.foundation.interaction.MutableInteractionSource()}
    val pressed by press.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(if(pressed).96f else 1f,label="choicePress")
    val ring by androidx.compose.animation.animateColorAsState(if(selected)accent else Color.Transparent,label="choiceRing")
    val screen=RoundedCornerShape(20.dp)
    val deep=Brush.verticalGradient(listOf(tint,androidx.compose.ui.graphics.lerp(tint,Color.Black,.62f)))
    Column(modifier.selectable(selected=selected,role=Role.RadioButton,interactionSource=press,indication=null,onClick=onClick),
        horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.graphicsLayer{scaleX=scale;scaleY=scale}.fillMaxWidth().aspectRatio(.5f)
            .border(2.5.dp,ring,RoundedCornerShape(24.dp)).padding(5.dp).clip(screen).background(deep)) {
            if(full) {
                // The art runs edge to edge and dissolves into the player's colour.
                // Masked rather than overlaid: a fade drawn over the art ended on a
                // different shade from the player under it, and showed as a line.
                Box(Modifier.fillMaxWidth().fillMaxHeight(.66f)
                    .graphicsLayer{compositingStrategy=androidx.compose.ui.graphics.CompositingStrategy.Offscreen}
                    .drawWithContent {
                        drawContent()
                        drawRect(Brush.verticalGradient(.45f to Color.Black,1f to Color.Transparent),blendMode=androidx.compose.ui.graphics.BlendMode.DstIn)
                    }) {
                    PreviewArt(artwork,tint,Modifier.fillMaxSize())
                }
            } else {
                // Its colours, blurred, behind a rounded sleeve.
                PreviewArt(artwork,tint,Modifier.fillMaxSize().blur(28.dp).graphicsLayer{alpha=.55f})
            }
            Column(Modifier.fillMaxSize().padding(horizontal=11.dp,vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                Box(Modifier.width(20.dp).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha=.5f)))
                if(full) Spacer(Modifier.weight(1f)) else {
                    Spacer(Modifier.height(16.dp))
                    PreviewArt(artwork,tint,Modifier.fillMaxWidth(.86f).aspectRatio(1f).shadow(8.dp,RoundedCornerShape(7.dp)).clip(RoundedCornerShape(7.dp)))
                    Spacer(Modifier.weight(1f))
                }
                Box(Modifier.fillMaxWidth(.72f).height(5.dp).clip(CircleShape).background(Color.White.copy(alpha=.9f)).align(Alignment.Start))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth(.46f).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha=.45f)).align(Alignment.Start))
                Spacer(Modifier.height(11.dp))
                Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Color.White.copy(alpha=.3f))) {
                    Box(Modifier.fillMaxWidth(.38f).fillMaxHeight().background(Color.White.copy(alpha=.85f)))
                }
                Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=6.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FastRewind,null,Modifier.size(17.dp),tint=Color.White)
                    Icon(Icons.Rounded.Pause,null,Modifier.size(22.dp),tint=Color.White)
                    Icon(Icons.Rounded.FastForward,null,Modifier.size(17.dp),tint=Color.White)
                }
            }
        }
        Text(label,Modifier.padding(top=12.dp),style=MaterialTheme.typography.bodyLarge.copy(fontSize=15.sp))
        Box(Modifier.padding(top=8.dp).size(24.dp).clip(CircleShape).background(if(selected)accent else Color.Transparent)
            .border(if(selected)0.dp else 1.5.dp,if(selected)Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha=.3f),CircleShape),
            contentAlignment=Alignment.Center) {
            if(selected) Icon(LibraryGlyphs.Check,null,Modifier.size(16.dp),tint=Color.White)
        }
    }
}

/** The playing cover, or its colour in a soft wash when nothing is loaded yet. */
@Composable private fun PreviewArt(artwork: String?,tint: Color,modifier: Modifier) {
    Box(modifier.background(Brush.linearGradient(listOf(androidx.compose.ui.graphics.lerp(tint,Color.White,.25f),tint)))) {
        if(artwork!=null) coil3.compose.AsyncImage(artwork,null,Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
        else Icon(Icons.Rounded.MusicNote,null,Modifier.align(Alignment.Center).size(28.dp),tint=Color.White.copy(alpha=.8f))
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
            // iOS 26 rounds its inset groups well past the old 10pt corner.
            shape = RoundedCornerShape(22.dp),
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
            .heightIn(min = 52.dp)
            .alpha(if (enabled) 1f else .45f)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
