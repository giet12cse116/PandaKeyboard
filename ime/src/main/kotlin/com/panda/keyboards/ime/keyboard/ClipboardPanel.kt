package com.panda.keyboards.ime.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.keyboards.theme.ClipboardItem

/**
 * Format timestamp as a friendly relative time string ("Just now", "2m ago", "1h ago", "3d ago").
 */
fun formatRelativeTimestamp(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val diffMs = (nowMs - timestampMs).coerceAtLeast(0L)
    val diffSec = diffMs / 1000
    return when {
        diffSec < 60 -> "Just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}

/**
 * Full in-keyboard Clipboard history panel replacing standard letter keys.
 *
 * Displays up to 10 entries (most recent first) from [ClipboardHistoryRepository].
 * Features:
 * - Tap to paste full snippet text and return to keyboard
 * - Per-item deletion
 * - Clear all action
 * - Friendly empty state
 * - ABC return button to restore standard QWERTY keys
 */
@Composable
fun ClipboardPanel(
    history: List<ClipboardItem>,
    onItemClick: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit,
    onBackToKeyboard: () -> Unit,
    resolvedTheme: ResolvedTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(resolvedTheme.keyboardBackground)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // ── Top Header Bar ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Panel Title + Count Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "📋 Clipboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = resolvedTheme.keyText
                )

                if (history.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = resolvedTheme.accent.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "${history.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = resolvedTheme.keyText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Actions: Clear All & Back to Keyboard
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (history.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = resolvedTheme.specialKeyBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClearAll)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = resolvedTheme.specialKeyText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Clear all",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = resolvedTheme.specialKeyText
                            )
                        }
                    }
                }

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
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Main Content Area ──────────────────────────────────────────────
        if (history.isEmpty()) {
            // Friendly Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📋",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "Clipboard is empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = resolvedTheme.keyText.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Copied text snippets will automatically appear here",
                        fontSize = 12.sp,
                        color = resolvedTheme.keyText.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // Scrollable List of up to 10 entries (most recent first)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = history,
                    key = { item -> item.id }
                ) { item ->
                    ClipboardItemRow(
                        item = item,
                        onItemClick = onItemClick,
                        onDeleteItem = onDeleteItem,
                        resolvedTheme = resolvedTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun ClipboardItemRow(
    item: ClipboardItem,
    onItemClick: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    resolvedTheme: ResolvedTheme
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = resolvedTheme.keyBackground.copy(alpha = 0.85f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onItemClick(item.text) },
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = resolvedTheme.keyText,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatRelativeTimestamp(item.timestamp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    color = resolvedTheme.keyText.copy(alpha = 0.55f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = resolvedTheme.specialKeyBackground.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onDeleteItem(item.id) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete entry",
                        tint = resolvedTheme.keyText.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
