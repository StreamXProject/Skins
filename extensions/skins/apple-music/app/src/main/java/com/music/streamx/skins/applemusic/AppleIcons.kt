package com.music.streamx.skins.applemusic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
internal object AppleIcons {
 val Previous get()=Icons.Rounded.FastRewind
 val Next get()=Icons.Rounded.FastForward
 val Play get()=Icons.Rounded.PlayArrow
 val Pause get()=Icons.Rounded.Pause
 fun verify(){check(listOf(Previous,Next,Play,Pause).all{it.root.size>0})}
}
