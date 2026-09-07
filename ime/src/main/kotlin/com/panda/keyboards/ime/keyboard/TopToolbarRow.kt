package com.panda.keyboards.ime.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Top Toolbar displaying 5 compact action icons above QWERTY key rows.
 *
 * Slots:
 * 1. Theme -> Opens in-keyboard quick theme switcher.
 * 2. Clipboard -> Opens clipboard history panel.
 * 3. Settings -> Deep-links to host app Settings screen.
 * 4. Voice -> Fires platform speech recognition.
 * 5. Fonts -> Reveals Sprint 5 font-selection bar.
 */
@Composable
fun TopToolbarRow(
    onThemeClick: () -> Unit,
    onClipboardClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onFontsClick: () -> Unit,
    resolvedTheme: ResolvedTheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .background(
                color = resolvedTheme.specialKeyBackground.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarIconButton(
            icon = Icons.Default.Palette,
            contentDescription = "Quick Theme Switcher",
            onClick = onThemeClick,
            resolvedTheme = resolvedTheme
        )

        ToolbarIconButton(
            icon = Icons.Default.ContentPaste,
            contentDescription = "Clipboard History",
            onClick = onClipboardClick,
            resolvedTheme = resolvedTheme
        )

        ToolbarIconButton(
            icon = Icons.Default.Settings,
            contentDescription = "Open Settings",
            onClick = onSettingsClick,
            resolvedTheme = resolvedTheme
        )

        ToolbarIconButton(
            icon = Icons.Default.Mic,
            contentDescription = "Voice Input",
            onClick = onVoiceClick,
            resolvedTheme = resolvedTheme
        )

        ToolbarIconButton(
            icon = Icons.Default.TextFields,
            contentDescription = "Font Selection",
            onClick = onFontsClick,
            resolvedTheme = resolvedTheme
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    resolvedTheme: ResolvedTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = resolvedTheme.keyBackground.copy(alpha = 0.6f),
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedTheme.keyText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
