package com.music.streamx.ui.skins

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/** Small visual overrides for skins that reuse the complete original player. */
data class SkinDefaultPlayerStyle(val fullCover: Boolean=false,val liquidGlass: Boolean=true)
interface SkinDefaultPlayerProvider {
    @Composable fun PlayerStyle(): SkinDefaultPlayerStyle
}
interface SkinSolidNavigation
interface SkinNavigationProvider {
    fun navigationIcon(index: Int): androidx.compose.ui.graphics.vector.ImageVector?
}
/** Android supplies real refracting glass; other platforms keep the same bounded hit shape. */
val LocalSkinPlayerGlass = staticCompositionLocalOf<@Composable (Modifier,Boolean)->Modifier> {{modifier,_->modifier}}

/** Keep the host's typography/shapes while still offering a skin configuration page. */
interface SkinUseHostTheme

/** Capture only the player background, never the glass controls sampling it. */
val LocalSkinPlayerBackdrop=staticCompositionLocalOf<Modifier> {Modifier}
