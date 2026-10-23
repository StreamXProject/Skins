package com.music.streamx.skins.comic

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

@Composable internal fun ComicSettingsGroup(header: String?,footer: String?,topSpacing: Dp,content: @Composable ()->Unit) {
    val c=MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top=topSpacing)) {
        header?.let {Eyebrow(it,Modifier.padding(start=24.dp,end=24.dp,top=20.dp,bottom=12.dp))}
        Surface(modifier=Modifier.padding(horizontal=16.dp).inkShadow(c.onSurface,4.dp),shape=Edge,color=c.surface,border=BorderStroke(2.dp,c.onSurface)) {Column {content()}}
        footer?.let {Text(it,Modifier.padding(horizontal=24.dp,vertical=12.dp),style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant)}
    }
}
@Composable internal fun ComicSettingsDivider(){HorizontalDivider(Modifier.padding(horizontal=16.dp),color=MaterialTheme.colorScheme.outlineVariant)}
@Composable internal fun ComicSettingsRow(icon: ImageVector?,iconPainter: Painter?,title: String,subtitle: String?,subtitleContent: (@Composable ()->Unit)?,value: String?,badge: String?,enabled: Boolean,onClick: (()->Unit)?,trailing: (@Composable ()->Unit)?) {
    val c=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().heightIn(min=68.dp).alpha(if(enabled)1f else .4f).then(if(onClick!=null)Modifier.clickable(enabled=enabled,onClick=onClick)else Modifier).padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        if(icon!=null)Icon(icon,null,Modifier.size(21.dp),tint=c.primary)else if(iconPainter!=null)Icon(iconPainter,null,Modifier.size(21.dp),tint=c.primary)
        Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.bodyLarge);if(subtitleContent!=null)subtitleContent() else subtitle?.let{Text(it,style=MaterialTheme.typography.bodySmall,color=c.onSurfaceVariant,modifier=Modifier.padding(top=3.dp))};badge?.let{Eyebrow(it,Modifier.padding(top=4.dp))}}
        if(trailing!=null)trailing()else {
            value?.let{Text(it,style=MaterialTheme.typography.labelLarge,color=c.primary,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.widthIn(max=110.dp))}
            if(onClick!=null)Icon(ComicIcons.KeyboardArrowRight,null,Modifier.size(18.dp),tint=c.onSurfaceVariant)
        }
    }
}
@Composable internal fun ComicSettingsPage(query: String,onQuery: (String)->Unit,padding: PaddingValues,onBack: ()->Unit,content: @Composable ()->Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(padding)) {
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onBack){Icon(ComicIcons.ArrowBack,"Back")};Eyebrow("COMIC / CONTROL ROOM",Modifier.padding(start=12.dp))}
        Text("YOUR RULES!",style=MaterialTheme.typography.displayLarge,modifier=Modifier.padding(horizontal=24.dp,vertical=20.dp))
        OutlinedTextField(query,onQuery,placeholder={Text("Find a setting")},leadingIcon={Icon(ComicIcons.Search,null)},singleLine=true,shape=Edge,modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(bottom=12.dp))
        content();Spacer(Modifier.height(24.dp))
    }
}
@Composable internal fun ComicSleepTimerDialog(minutes: Int?,afterTrack: Boolean,running: Boolean,presets: List<Int>,onStart: (Int)->Unit,onAfterTrack: ()->Unit,onCancel: ()->Unit,onDismiss: ()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,shape=RoundedCornerShape(24.dp),title={Text("LIGHTS OUT!",style=MaterialTheme.typography.headlineMedium)},text={Column(Modifier.heightIn(max=420.dp).verticalScroll(rememberScrollState())){
        Text(if(afterTrack)"Stopping after this track"else if(running)"$minutes minutes remaining"else"Choose when playback stops",color=MaterialTheme.colorScheme.onSurfaceVariant)
        presets.chunked(2).forEach { pair->Row(Modifier.padding(top=8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){pair.forEach{n->ComicButton("$n min",{onStart(n);onDismiss()},Modifier.weight(1f))}}}
        TextButton({onAfterTrack();onDismiss()}){Text("After this track")};if(running)TextButton({onCancel();onDismiss()}){Text("Cancel timer")}
    }},confirmButton={TextButton(onDismiss){Text("Done")}})
}
