package com.music.streamx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.streamx.data.model.Account

@Composable
internal fun SpotuiHomeHeader(account: Account?, onAccount: () -> Unit, onLocal: () -> Unit, onHistory: () -> Unit, onExplore: () -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).clickable(onClick = onAccount).semantics { contentDescription = "Account" }, contentAlignment = Alignment.Center) {
            if (account?.thumbnailUrl != null) AsyncImage(account.thumbnailUrl, "Account", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(account?.name?.take(1)?.uppercase() ?: "•", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        listOf("Music" to {}, "Local files" to onLocal, "History" to onHistory, "Explore" to onExplore).forEachIndexed { index, (label, action) ->
            Surface(onClick = action, shape = CircleShape, color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                Text(label, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), fontSize = 13.sp,
                    color = if (index == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
