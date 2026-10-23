package com.music.streamx.ui.components

import com.music.streamx.ui.skins.SkinIcon as Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/** Spotui's edge-to-edge navigation, using the host's tab IDs and actions. */
@Composable
internal fun SkinBottomNavigation(tabs: List<BottomTab>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val spotui = com.music.streamx.ui.skins.LocalSkinLayout.current.icons == com.music.streamx.ui.skins.SkinIconPack.EXTENSION
    val indices = if (spotui) listOf(0, 3, 2) else tabs.indices.toList()
    NavigationBar(containerColor = if (spotui) { if (MaterialTheme.colorScheme.background.luminance() < .5f) Color.Black else MaterialTheme.colorScheme.background } else Color.Transparent, tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(), windowInsets = WindowInsets.navigationBars) {
        indices.forEach { index ->
            val tab = tabs[index]
            NavigationBarItem(selected = index == selectedIndex || (spotui && index == 3 && selectedIndex == 1), onClick = { onTabSelected(index) },
                icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(25.dp)) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, indicatorColor = Color.Transparent))
        }
    }
}
