package com.music.streamx.skins.expressive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable internal fun ExpressiveSettingsGroup(header: String?,footer: String?,topSpacing: Dp,content: @Composable ()->Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal=16.dp)) {
        if(header!=null) Text(header,Modifier.padding(start=12.dp,top=24.dp,bottom=12.dp),style=MaterialTheme.typography.titleLarge,color=MaterialTheme.colorScheme.onSurface)
        else Spacer(Modifier.height(topSpacing.coerceAtMost(16.dp)))
        Surface(shape=RoundedCornerShape(28.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.fillMaxWidth().padding(vertical=8.dp)) {content()}
        }
        footer?.let{Text(it,Modifier.padding(horizontal=12.dp,vertical=12.dp),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}
@Composable internal fun ExpressiveSettingsDivider() {
    HorizontalDivider(Modifier.padding(horizontal=20.dp),color=MaterialTheme.colorScheme.outlineVariant,thickness=.5.dp)
}
@Composable internal fun ExpressiveSettingsRow(icon: ImageVector?,iconPainter: Painter?,title: String,subtitle: String?,subtitleContent: (@Composable ()->Unit)?,value: String?,badge: String?,enabled: Boolean,onClick: (() ->Unit)?,trailing: (@Composable ()->Unit)?) {
    ListItem(
        headlineContent={Column {Text(title,style=MaterialTheme.typography.titleMedium,maxLines=3,overflow=TextOverflow.Ellipsis);badge?.let{Text(it,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)}}},
        supportingContent=if(subtitleContent!=null)subtitleContent else subtitle?.let{{Text(it,style=MaterialTheme.typography.bodyMedium)}},
        leadingContent=if(icon!=null || iconPainter!=null) ({
            Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=RoundedCornerShape(16.dp)) {
                Box(Modifier.size(48.dp),contentAlignment=Alignment.Center) {
                    if(iconPainter!=null)Icon(iconPainter,null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.onSecondaryContainer)
                    else icon?.let{Icon(expressiveIcon(it),null,Modifier.size(24.dp),tint=MaterialTheme.colorScheme.onSecondaryContainer)}
                }
            }
        }) else null,
        trailingContent=trailing ?: if(value!=null || onClick!=null) ({
            Row(verticalAlignment=Alignment.CenterVertically) {
                value?.let{Text(it,Modifier.widthIn(max=100.dp),style=MaterialTheme.typography.labelLarge,maxLines=2,overflow=TextOverflow.Ellipsis)}
                if(onClick!=null)Icon(ExpressiveSymbols.ChevronRight,null)
            }
        }) else null,
        colors=ListItemDefaults.colors(containerColor=MaterialTheme.colorScheme.surfaceContainer),
        modifier=Modifier.heightIn(min=72.dp).alpha(if(enabled)1f else .38f).then(if(onClick!=null)Modifier.clickable(enabled=enabled,onClick=onClick) else Modifier))
}
