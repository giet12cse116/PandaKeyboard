package com.panda.keyboards.ime.keyboard

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.keyboards.ime.KeyboardActionHandler
import kotlinx.coroutines.launch

/**
 * Full in-keyboard Emoji panel replacing the standard keyboard key rows.
 *
 * Sprint 10 Design:
 * - Single direct view with continuous scrolling through ALL standard Unicode emojis.
 * - Stickers and GIF tabs removed entirely.
 * - Pinned "Recently used" section at the top when recents are available.
 * - Slim category quick-jump strip (Smileys, Animals, Food, Activities, Travel, Objects, Symbols, Flags)
 *   that scrolls the grid directly to that section header (Gboard-style UX).
 */
@Composable
fun EmojiPanel(
    recentEmojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onBackToKeyboard: () -> Unit,
    actionHandler: KeyboardActionHandler? = null,
    editorInfo: EditorInfo? = null,
    resolvedTheme: ResolvedTheme,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    // Calculate section header indices for quick jump
    val (categoryHeaderIndices, totalItemCount) = remember(recentEmojis) {
        val indicesMap = mutableMapOf<EmojiCategory, Int>()
        var recentsHeaderIndex: Int? = null
        var currentIndex = 0

        if (recentEmojis.isNotEmpty()) {
            recentsHeaderIndex = currentIndex
            currentIndex += 1 + recentEmojis.size
        }

        for (category in EmojiCategory.standardCategories) {
            indicesMap[category] = currentIndex
            currentIndex += 1 + category.emojis.size
        }

        Pair(indicesMap, currentIndex)
    }

    // Determine current visible category based on scroll position
    val currentCategory by remember(recentEmojis) {
        derivedStateOf {
            val firstVisible = gridState.firstVisibleItemIndex
            var matched: EmojiCategory = EmojiCategory.SMILEYS
            for (category in EmojiCategory.standardCategories) {
                val headerIndex = categoryHeaderIndices[category] ?: 0
                if (firstVisible >= headerIndex) {
                    matched = category
                } else {
                    break
                }
            }
            matched
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(resolvedTheme.keyboardBackground)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // ── Top Header Bar: Slim Category Quick-Jump Strip + ABC Return Button ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Strip
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recents Icon if available
                if (recentEmojis.isNotEmpty()) {
                    item {
                        val isSelected = remember(recentEmojis) {
                            derivedStateOf {
                                gridState.firstVisibleItemIndex < (categoryHeaderIndices[EmojiCategory.SMILEYS] ?: 0)
                            }
                        }.value

                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) resolvedTheme.accent else resolvedTheme.keyBackground.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        gridState.animateScrollToItem(0)
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🕒", fontSize = 15.sp)
                            }
                        }
                    }
                }

                // Standard Unicode Emoji Categories
                items(EmojiCategory.standardCategories) { category ->
                    val isSelected = (category == currentCategory && (recentEmojis.isEmpty() || gridState.firstVisibleItemIndex >= (categoryHeaderIndices[EmojiCategory.SMILEYS] ?: 0)))
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) resolvedTheme.accent else resolvedTheme.keyBackground.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                val targetIndex = categoryHeaderIndices[category] ?: 0
                                coroutineScope.launch {
                                    gridState.animateScrollToItem(targetIndex)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(category.icon, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Keyboard Return Button (ABC)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = resolvedTheme.specialKeyBackground,
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBackToKeyboard)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ABC",
                        fontWeight = FontWeight.Bold,
                        color = resolvedTheme.specialKeyText,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Single Continuous Emoji Grid ────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Pinned Recently Used Section
            if (recentEmojis.isNotEmpty()) {
                item(span = { GridItemSpan(8) }) {
                    Text(
                        text = "Recently Used",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = resolvedTheme.keyText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 2.dp, top = 4.dp, end = 2.dp, bottom = 4.dp)
                    )
                }

                itemsIndexed(items = recentEmojis, key = { index, emoji -> "recent_${index}_$emoji" }) { _, emojiStr ->
                    EmojiCell(
                        emojiStr = emojiStr,
                        resolvedTheme = resolvedTheme,
                        onEmojiSelected = onEmojiSelected
                    )
                }
            }

            // All Standard Categories Sequentially
            for (category in EmojiCategory.standardCategories) {
                item(span = { GridItemSpan(8) }, key = "header_${category.name}") {
                    Text(
                        text = category.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = resolvedTheme.keyText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 2.dp, top = 8.dp, end = 2.dp, bottom = 4.dp)
                    )
                }

                itemsIndexed(items = category.emojis, key = { index, emoji -> "${category.name}_${index}_$emoji" }) { _, emojiStr ->
                    EmojiCell(
                        emojiStr = emojiStr,
                        resolvedTheme = resolvedTheme,
                        onEmojiSelected = onEmojiSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiCell(
    emojiStr: String,
    resolvedTheme: ResolvedTheme,
    onEmojiSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(resolvedTheme.keyBackground.copy(alpha = 0.5f))
            .clickable { onEmojiSelected(emojiStr) },
        contentAlignment = Alignment.Center
    ) {
        Text(emojiStr, fontSize = 19.sp)
    }
}

