package com.music.streamx.skins.dot

import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.music.streamx.data.lyrics.LyricLine

private const val LINES_ABOVE_ACTIVE = 2

// Unconsumed list motion must not reach the full player's swipe-to-close gesture.
private val LyricsScrollBoundary = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = Offset(0f, available.y)
    override suspend fun onPostFling(consumed: Velocity, available: Velocity) = Velocity(0f, available.y)
}

@Composable
internal fun DotLyrics(
    lyrics: List<LyricLine>?,
    lyricsUnavailable: Boolean,
    positionMs: Long,
    lyricsOffsetMs: Int,
    reduceAnimation: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MaterialTheme.colorScheme
    val acc = DotOptions.accent.color

    if (lyricsUnavailable || lyrics.isNullOrEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Text(
                text = if (lyricsUnavailable) "// LYRICS UNAVAILABLE" else "// NO LYRICS FOUND",
                style = MaterialTheme.typography.bodyLarge,
                color = c.onSurfaceVariant,
            )
        }
        return
    }

    val effectivePosition = (positionMs - lyricsOffsetMs).coerceAtLeast(0L)
    val listState = rememberLazyListState()

    val activeIndex by remember(lyrics, effectivePosition) {
        derivedStateOf {
            val idx = lyrics.indexOfLast { it.timeMs <= effectivePosition }
            if (idx >= 0) idx else 0
        }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && !listState.isScrollInProgress) {
            val target = (activeIndex - LINES_ABOVE_ACTIVE).coerceAtLeast(0)
            if (reduceAnimation) listState.scrollToItem(target)
            else listState.animateScrollToItem(target)
        }
    }

    TuiPanel(modifier = modifier.fillMaxSize().nestedScroll(LyricsScrollBoundary)) {
        LazyColumn(
            state = listState,
            overscrollEffect = null,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isActive = index == activeIndex
                val isPast = index < activeIndex
                val isSinging = isActive && (line.endMs <= line.timeMs || effectivePosition <= line.endMs)

                val lineModifier = Modifier
                    .fillMaxWidth()
                    .tuiClickable { onSeek(line.timeMs) }
                    .padding(vertical = 6.dp, horizontal = 4.dp)

                if (line.isGap) {
                    Text(
                        text = "· · ·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isActive) acc else c.outlineVariant,
                        textAlign = TextAlign.Center,
                        modifier = lineModifier,
                    )
                } else if (line.isWordSynced && isSinging) {
                    val revealed = line.revealedChars(effectivePosition).toInt().coerceIn(0, line.text.length)
                    val annotated = buildAnnotatedString {
                        append(line.text)
                        addStyle(SpanStyle(color = acc, fontWeight = FontWeight.Bold), 0, revealed)
                        addStyle(SpanStyle(color = c.onSurface, fontWeight = FontWeight.Bold), revealed, line.text.length)
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = lineModifier,
                    )
                } else {
                    val textColor = when {
                        isActive -> acc
                        isPast -> c.outlineVariant
                        else -> c.onSurfaceVariant
                    }
                    Text(
                        text = if (isActive) "> ${line.text}" else "  ${line.text}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = textColor,
                        modifier = lineModifier,
                    )
                }
            }
        }
    }
}
