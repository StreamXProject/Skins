package com.music.streamx.ui.skins

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/** Semantic icon substitution across screens, dialogs and settings; default skins keep their icons. */
fun spotuiIcon(vector: ImageVector): ImageVector = when (vector.name.substringAfterLast('.')) {
    "AccountCircle" -> Icons.Outlined.AccountCircle
    "Add" -> SpotuiIcons.Add
    "Album" -> Icons.Outlined.Album
    "Animation" -> Icons.Outlined.Animation
    "ArrowBack" -> Icons.Outlined.ArrowBack
    "ArrowDownward" -> Icons.Outlined.ArrowDownward
    "ArrowUpward" -> Icons.Outlined.ArrowUpward
    "AutoAwesome" -> Icons.Outlined.AutoAwesome
    "BarChart" -> Icons.Outlined.BarChart
    "Bedtime" -> Icons.Outlined.Bedtime
    "Bluetooth" -> Icons.Outlined.Bluetooth
    "BlurOff" -> Icons.Outlined.BlurOff
    "BlurOn" -> Icons.Outlined.BlurOn
    "Brightness4" -> Icons.Outlined.Brightness4
    "BugReport" -> Icons.Outlined.BugReport
    "Cached" -> Icons.Outlined.Cached
    "Cast" -> Icons.Outlined.Cast
    "CastConnected" -> Icons.Outlined.CastConnected
    "Check" -> Icons.Outlined.Check
    "CheckCircle" -> SpotuiIcons.Check
    "ChevronRight" -> Icons.Outlined.ChevronRight
    "Close" -> Icons.Outlined.Close
    "Cloud" -> Icons.Outlined.Cloud
    "CloudDone" -> Icons.Outlined.CloudDone
    "CloudOff" -> Icons.Outlined.CloudOff
    "ContentCopy" -> Icons.Outlined.ContentCopy
    "Delete" -> Icons.Outlined.Delete
    "DeleteForever" -> Icons.Outlined.DeleteForever
    "DeleteSweep" -> Icons.Outlined.DeleteSweep
    "Dns" -> Icons.Outlined.Dns
    "Download" -> SpotuiIcons.Download
    "DownloadDone" -> Icons.Outlined.DownloadDone
    "Downloading" -> Icons.Outlined.Downloading
    "DragHandle" -> Icons.Outlined.DragHandle
    "Edit" -> Icons.Outlined.Edit
    "ErrorOutline" -> Icons.Outlined.ErrorOutline
    "Extension" -> Icons.Outlined.Extension
    "FastForward" -> Icons.Outlined.FastForward
    "FastRewind" -> Icons.Outlined.FastRewind
    "Favorite" -> Icons.Outlined.Favorite
    "FavoriteBorder" -> Icons.Outlined.FavoriteBorder
    "FileDownload" -> Icons.Outlined.FileDownload
    "FileUpload" -> Icons.Outlined.FileUpload
    "FilterAlt" -> Icons.Outlined.FilterAlt
    "Folder" -> Icons.Outlined.Folder
    "Fullscreen" -> Icons.Outlined.Fullscreen
    "Gradient" -> Icons.Outlined.Gradient
    "GraphicEq" -> Icons.Outlined.GraphicEq
    "Groups" -> Icons.Outlined.Groups
    "Headphones" -> Icons.Outlined.Headphones
    "HighQuality" -> Icons.Outlined.HighQuality
    "History" -> Icons.Outlined.History
    "InsertDriveFile" -> Icons.Outlined.InsertDriveFile
    "IosShare" -> Icons.Outlined.IosShare
    "Key" -> Icons.Outlined.Key
    "KeyboardArrowRight" -> Icons.Outlined.KeyboardArrowRight
    "Label" -> Icons.Outlined.Label
    "Lan" -> Icons.Outlined.Lan
    "Language" -> Icons.Outlined.Language
    "LibraryMusic" -> SpotuiIcons.Library
    "Link" -> Icons.Outlined.Link
    "LocalOffer" -> Icons.Outlined.LocalOffer
    "Lock" -> Icons.Outlined.Lock
    "Login" -> Icons.Outlined.Login
    "Logout" -> Icons.Outlined.Logout
    "ManageAccounts" -> Icons.Outlined.ManageAccounts
    "Memory" -> Icons.Outlined.Memory
    "MoreHoriz" -> SpotuiIcons.More
    "MoreVert" -> SpotuiIcons.More
    "MotionPhotosOff" -> Icons.Outlined.MotionPhotosOff
    "MusicNote" -> Icons.Outlined.MusicNote
    "MusicOff" -> Icons.Outlined.MusicOff
    "NorthWest" -> Icons.Outlined.NorthWest
    "Notes" -> Icons.Outlined.Notes
    "Palette" -> Icons.Outlined.Palette
    "Pause" -> SpotuiIcons.Pause
    "Person" -> Icons.Outlined.Person
    "PhoneAndroid" -> Icons.Outlined.PhoneAndroid
    "PhoneIphone" -> Icons.Outlined.PhoneIphone
    "PlayArrow" -> SpotuiIcons.Play
    "PlayCircle" -> Icons.Outlined.PlayCircle
    "PlaylistAdd" -> Icons.Outlined.PlaylistAdd
    "PlaylistPlay" -> Icons.Outlined.PlaylistPlay
    "PlaylistRemove" -> Icons.Outlined.PlaylistRemove
    "Public" -> Icons.Outlined.Public
    "QueueMusic" -> Icons.Outlined.QueueMusic
    "Radio" -> Icons.Outlined.Radio
    "RadioButtonChecked" -> Icons.Outlined.RadioButtonChecked
    "RadioButtonUnchecked" -> Icons.Outlined.RadioButtonUnchecked
    "Refresh" -> Icons.Outlined.Refresh
    "Search" -> SpotuiIcons.Search
    "Settings" -> SpotuiIcons.Settings
    "Share" -> SpotuiIcons.Share
    "ShowChart" -> Icons.Outlined.ShowChart
    "SignalCellularAlt" -> Icons.Outlined.SignalCellularAlt
    "SmartButton" -> Icons.Outlined.SmartButton
    "SmartDisplay" -> Icons.Outlined.SmartDisplay
    "Sort" -> Icons.Outlined.Sort
    "Speaker" -> Icons.Outlined.Speaker
    "Star" -> Icons.Outlined.Star
    "StarBorder" -> Icons.Outlined.StarBorder
    "Storage" -> Icons.Outlined.Storage
    "SurroundSound" -> Icons.Outlined.SurroundSound
    "SwapVert" -> Icons.Outlined.SwapVert
    "SwitchAccount" -> Icons.Outlined.SwitchAccount
    "Sync" -> Icons.Outlined.Sync
    "ThumbDown" -> Icons.Outlined.ThumbDown
    "ThumbDownOffAlt" -> Icons.Outlined.ThumbDownOffAlt
    "Translate" -> SpotuiIcons.Translate
    "Tune" -> Icons.Outlined.Tune
    "Tv" -> Icons.Outlined.Tv
    "Undo" -> Icons.Outlined.Undo
    "Upgrade" -> Icons.Outlined.Upgrade
    "Usb" -> Icons.Outlined.Usb
    "Videocam" -> Icons.Outlined.Videocam
    "Visibility" -> SpotuiIcons.Visibility
    "VisibilityOff" -> SpotuiIcons.VisibilityOff
    "VolumeDown" -> Icons.Outlined.VolumeDown
    "VolumeOff" -> Icons.Outlined.VolumeOff
    "VolumeUp" -> Icons.Outlined.VolumeUp
    "Warning" -> Icons.Outlined.Warning
    "Waves" -> Icons.Outlined.Waves
    "Wifi" -> Icons.Outlined.Wifi
    "bc_home" -> SpotuiIcons.Home
    "bc_tab_library" -> SpotuiIcons.Library
    "bc_tab_search" -> SpotuiIcons.Search
    "bc_search" -> SpotuiIcons.Search
    "bc_play" -> SpotuiIcons.Play
    "bc_shuffle" -> SpotuiIcons.Shuffle
    "bc_repeat" -> SpotuiIcons.Repeat
    "bc_plus" -> SpotuiIcons.Add
    "bc_check" -> SpotuiIcons.Check
    "bc_download" -> SpotuiIcons.Download
    "bc_grid_view" -> SpotuiIcons.Grid
    "bc_list_view" -> SpotuiIcons.List
    "bc_tab_explore" -> SpotuiIcons.Search
    "bc_heart" -> SpotuiIcons.Add
    "bc_heart_filled" -> SpotuiIcons.Check
    "bc_queue" -> Icons.Outlined.QueueMusic
    "bc_lyrics_quote", "bc_lyrics" -> Icons.Outlined.MicNone
    "bc_clock" -> Icons.Outlined.History
    "bc_music_note" -> Icons.Outlined.MusicNote
    "bc_chevron_right" -> Icons.Outlined.ChevronRight
    else -> vector
}


/** Eagerly link icon APIs so release/ABI mismatches fail activation, not composition. */
fun verifySpotuiRuntimeIcons() {
    val icons = listOf(
        Icons.Outlined.AccountCircle,
        Icons.Outlined.Album,
        Icons.Outlined.Animation,
        Icons.Outlined.ArrowBack,
        Icons.Outlined.ArrowDownward,
        Icons.Outlined.ArrowUpward,
        Icons.Outlined.AutoAwesome,
        Icons.Outlined.BarChart,
        Icons.Outlined.Bedtime,
        Icons.Outlined.Bluetooth,
        Icons.Outlined.BlurOff,
        Icons.Outlined.BlurOn,
        Icons.Outlined.Brightness4,
        Icons.Outlined.BugReport,
        Icons.Outlined.Cached,
        Icons.Outlined.Cast,
        Icons.Outlined.CastConnected,
        Icons.Outlined.Check,
        Icons.Outlined.ChevronRight,
        Icons.Outlined.Close,
        Icons.Outlined.Cloud,
        Icons.Outlined.CloudDone,
        Icons.Outlined.CloudOff,
        Icons.Outlined.ContentCopy,
        Icons.Outlined.Delete,
        Icons.Outlined.DeleteForever,
        Icons.Outlined.DeleteSweep,
        Icons.Outlined.Dns,
        Icons.Outlined.DownloadDone,
        Icons.Outlined.Downloading,
        Icons.Outlined.DragHandle,
        Icons.Outlined.Edit,
        Icons.Outlined.ErrorOutline,
        Icons.Outlined.Extension,
        Icons.Outlined.FastForward,
        Icons.Outlined.FastRewind,
        Icons.Outlined.Favorite,
        Icons.Outlined.FavoriteBorder,
        Icons.Outlined.FileDownload,
        Icons.Outlined.FileUpload,
        Icons.Outlined.FilterAlt,
        Icons.Outlined.Folder,
        Icons.Outlined.Fullscreen,
        Icons.Outlined.Gradient,
        Icons.Outlined.GraphicEq,
        Icons.Outlined.Groups,
        Icons.Outlined.Headphones,
        Icons.Outlined.HighQuality,
        Icons.Outlined.History,
        Icons.Outlined.InsertDriveFile,
        Icons.Outlined.IosShare,
        Icons.Outlined.Key,
        Icons.Outlined.KeyboardArrowRight,
        Icons.Outlined.Label,
        Icons.Outlined.Lan,
        Icons.Outlined.Language,
        Icons.Outlined.Link,
        Icons.Outlined.LocalOffer,
        Icons.Outlined.Lock,
        Icons.Outlined.Login,
        Icons.Outlined.Logout,
        Icons.Outlined.ManageAccounts,
        Icons.Outlined.Memory,
        Icons.Outlined.MicNone,
        Icons.Outlined.MotionPhotosOff,
        Icons.Outlined.MusicNote,
        Icons.Outlined.MusicOff,
        Icons.Outlined.NorthWest,
        Icons.Outlined.Notes,
        Icons.Outlined.Palette,
        Icons.Outlined.Person,
        Icons.Outlined.PhoneAndroid,
        Icons.Outlined.PhoneIphone,
        Icons.Outlined.PlayCircle,
        Icons.Outlined.PlaylistAdd,
        Icons.Outlined.PlaylistPlay,
        Icons.Outlined.PlaylistRemove,
        Icons.Outlined.Public,
        Icons.Outlined.QueueMusic,
        Icons.Outlined.Radio,
        Icons.Outlined.RadioButtonChecked,
        Icons.Outlined.RadioButtonUnchecked,
        Icons.Outlined.Refresh,
        Icons.Outlined.ShowChart,
        Icons.Outlined.SignalCellularAlt,
        Icons.Outlined.SmartButton,
        Icons.Outlined.SmartDisplay,
        Icons.Outlined.Sort,
        Icons.Outlined.Speaker,
        Icons.Outlined.Star,
        Icons.Outlined.StarBorder,
        Icons.Outlined.Storage,
        Icons.Outlined.SurroundSound,
        Icons.Outlined.SwapVert,
        Icons.Outlined.SwitchAccount,
        Icons.Outlined.Sync,
        Icons.Outlined.ThumbDown,
        Icons.Outlined.ThumbDownOffAlt,
        Icons.Outlined.Tune,
        Icons.Outlined.Tv,
        Icons.Outlined.Undo,
        Icons.Outlined.Upgrade,
        Icons.Outlined.Usb,
        Icons.Outlined.Videocam,
        Icons.Outlined.VolumeDown,
        Icons.Outlined.VolumeOff,
        Icons.Outlined.VolumeUp,
        Icons.Outlined.Warning,
        Icons.Outlined.Waves,
        Icons.Outlined.Wifi
    )
    require(icons.all { it.defaultWidth.value > 0f })
}
