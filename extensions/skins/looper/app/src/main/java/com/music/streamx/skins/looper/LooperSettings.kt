package com.music.streamx.skins.looper

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.music.streamx.ui.skins.SkinBackHandler
import com.music.streamx.ui.skins.LocalSkinSettingsGroupKey
import androidx.compose.ui.platform.LocalContext

private data class Category(val key: String,val title: String,val description: String,val icon: ImageVector)
private val categories=listOf(
    Category("theme","Theme","Customize app colors, theme and lyrics backgrounds",LooperIcons.Settings),
    Category("home","Home Dashboard Settings","Customize horizontal rows on your Home screen",LooperIcons.Home),
    Category("playback","Playback & Language","Manage playback behavior and language preferences",LooperIcons.Play),
    Category("audio","Audio & Playback","Streaming quality, equalizer and audio output",LooperIcons.Songs),
    Category("library","Music Library","Folders, local files and offline music",LooperIcons.Library),
    Category("data","Backups & Logs","Export, import and manage app data",LooperIcons.Queue),
    Category("account","Accounts & Sources","Connected accounts, sources and listening together",LooperIcons.Settings),
    Category("about","About & Advanced","App information, performance and advanced settings",LooperIcons.More))
private fun categoryKey(group: String?)=when(group){"appearance"->"theme";"playback","language"->"playback";"audio_quality"->"audio";"library","local_music","downloads","storage"->"library";"your_data"->"data";"account"->"account";else->"about"}
private data class SettingsNavigation(val selected: MutableState<String?>,val query: String)
private val LocalSettingsNavigation=staticCompositionLocalOf<SettingsNavigation?>{null}
@Composable internal fun LooperSettingsPage(query: String,onQuery: (String)->Unit,padding: PaddingValues,onBack: ()->Unit,content: @Composable ()->Unit) {
    val selected=remember{mutableStateOf<String?>(null)}
    var searching by remember{mutableStateOf(query.isNotBlank())}
    val scroll=rememberScrollState()
    LaunchedEffect(selected.value,query){scroll.scrollTo(0)}
    SkinBackHandler(selected.value!=null || searching){if(searching){searching=false;onQuery("")}else selected.value=null}
    CompositionLocalProvider(LocalSettingsNavigation provides SettingsNavigation(selected,query)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(scroll).padding(top=12.dp,bottom=padding.calculateBottomPadding(),start=16.dp,end=16.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom=20.dp),verticalAlignment=Alignment.CenterVertically) {
                LooperIconButton(LooperIcons.Back,if(selected.value!=null)"Back to settings"else"Back",{if(selected.value!=null)selected.value=null else onBack()})
                Text(categories.firstOrNull{it.key==selected.value}?.title ?: "Settings",Modifier.weight(1f),style=MaterialTheme.typography.headlineLarge,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                LooperIconButton(LooperIcons.Search,"Search settings",{searching=!searching;if(!searching)onQuery("")})
            }
            if(searching)OutlinedTextField(query,onQuery,Modifier.fillMaxWidth().padding(bottom=16.dp),placeholder={Text("Search settings")},shape=Capsule,singleLine=true)
            if(selected.value==null && query.isBlank()) categories.forEach{category->
                Surface(onClick={selected.value=category.key},shape=Panel,color=Ink,border=BorderStroke(.8.dp,Color.White.copy(alpha=.05f)),modifier=Modifier.fillMaxWidth().padding(bottom=12.dp)) {
                    Row(Modifier.padding(horizontal=20.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically){
                        Box(Modifier.size(40.dp).background(Pink.copy(alpha=.08f),CircleShape),contentAlignment=Alignment.Center){Icon(category.icon,null,Modifier.size(20.dp),tint=Pink)}
                        Column(Modifier.weight(1f).padding(start=16.dp,end=8.dp)){Text(category.title,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(4.dp));Text(category.description,style=MaterialTheme.typography.bodyMedium,color=Dim)}
                        Text("›",color=Dim)
                    }
                }
            }
            if(selected.value=="home") {
                val options=looperOptions();val context=LocalContext.current
                LooperSettingsRow(LooperIcons.Home,null,"Quick Picks","Show the artwork grid and paging dots on Home",null,null,null,true,null){Switch(options.quickPicks,{context.getSharedPreferences("skin.looper",android.content.Context.MODE_PRIVATE).edit().putBoolean("quickPicks",it).apply()})}
                Text("Recommendation shelves come from your selected source.",Modifier.padding(16.dp),style=MaterialTheme.typography.bodyMedium,color=Dim)
            }
            content()
        }
    }
}
@Composable internal fun LooperSettingsGroup(header: String?,footer: String?,topSpacing: Dp,content: @Composable ()->Unit) {
    val nav=LocalSettingsNavigation.current
    if(nav!=null && nav.query.isBlank()) {
        if(nav.selected.value==null || nav.selected.value!=categoryKey(LocalSkinSettingsGroupKey.current))return
    }
    Column(Modifier.fillMaxWidth().padding(top=if(nav==null)topSpacing else 0.dp,bottom=12.dp)) {
        header?.let{Text(it,Modifier.padding(bottom=10.dp),style=MaterialTheme.typography.titleMedium)}
        Column(Modifier.clip(Panel).background(Ink).border(.8.dp,Color.White.copy(alpha=.05f),Panel)){content()}
        footer?.let{Text(it,Modifier.padding(8.dp),style=MaterialTheme.typography.bodySmall,color=Dim)}
    }
}
@Composable internal fun LooperSettingsDivider(){HorizontalDivider(Modifier.padding(horizontal=16.dp),color=Color.White.copy(alpha=.05f),thickness=.5.dp)}
@Composable internal fun LooperSettingsRow(icon: ImageVector?,iconPainter: Painter?,title: String,subtitle: String?,subtitleContent: (@Composable ()->Unit)?,value: String?,badge: String?,enabled: Boolean,onClick: (()->Unit)?,trailing: (@Composable ()->Unit)?) {
    Row(Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(enabled=enabled,onClick=onClick)else Modifier).heightIn(min=56.dp).padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
        if(icon!=null || iconPainter!=null)Box(Modifier.size(36.dp).background(Pink.copy(alpha=.08f),CircleShape),contentAlignment=Alignment.Center){if(iconPainter!=null)Icon(iconPainter,null,Modifier.size(18.dp),tint=Pink)else Icon(mapLooperIcon(icon!!),null,Modifier.size(18.dp),tint=Pink)}
        Column(Modifier.weight(1f).padding(start=if(icon!=null || iconPainter!=null)12.dp else 0.dp,end=8.dp)){Text(title,style=MaterialTheme.typography.titleMedium,color=Color.White.copy(alpha=if(enabled)1f else .4f));subtitle?.let{Text(it,style=MaterialTheme.typography.bodyMedium,color=Dim)};subtitleContent?.invoke()}
        value?.let{Text(it,style=MaterialTheme.typography.labelMedium,color=Dim)}
        badge?.let{Text(it,style=MaterialTheme.typography.labelSmall,color=Pink)}
        if(trailing!=null)trailing()else if(onClick!=null)Text("›",color=Dim)
    }
}
