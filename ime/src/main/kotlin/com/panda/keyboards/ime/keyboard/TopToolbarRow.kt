package com.panda.keyboards.ime.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
    val synthwaveStyle = resolvedTheme.synthwaveCyberpunkNeonStyle
    val origamiStyle = resolvedTheme.origamiPaperCraftStyle
    val pizzaStyle = resolvedTheme.pizzaSliceStyle
    val steampunkStyle = resolvedTheme.steampunkIndustrialStyle
    val assetSkinStyle = resolvedTheme.assetSkinStyle

    if (synthwaveStyle != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .background(
                    color = Color(0xDC170E36),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0x40C084FC),
                    shape = RoundedCornerShape(12.dp)
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SynthwaveNavIcon(icon = Icons.Default.Palette, desc = "Theme", dotColor = Color(0xFF00E5FF), onClick = onThemeClick)
            SynthwaveNavIcon(icon = Icons.Default.ContentPaste, desc = "Clipboard", dotColor = Color(0xFFFF2A85), onClick = onClipboardClick)
            SynthwaveNavIcon(icon = Icons.Default.Settings, desc = "Settings", dotColor = Color(0xFF00E5FF), onClick = onSettingsClick)
            SynthwaveNavIcon(icon = Icons.Default.Mic, desc = "Voice", dotColor = Color(0xFF00E5FF), onClick = onVoiceClick)
            SynthwaveNavIcon(icon = Icons.Default.TextFields, desc = "Fonts", dotColor = Color(0xFFFF2A85), onClick = onFontsClick)
        }
    } else {
        val iconTint = when {
            steampunkStyle != null -> Color(0xFFD4AF37)
            pizzaStyle != null -> Color(0xFF5D2E17)
            origamiStyle != null -> Color(0xFF4A3B6B)
            assetSkinStyle != null -> Color(0xFFD4AF37)
            else -> resolvedTheme.keyText
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarIconButton(
                icon = Icons.Default.Palette,
                contentDescription = "Quick Theme Switcher",
                tint = iconTint,
                onClick = onThemeClick
            )

            ToolbarIconButton(
                icon = Icons.Default.ContentPaste,
                contentDescription = "Clipboard History",
                tint = iconTint,
                onClick = onClipboardClick
            )

            ToolbarIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Open Settings",
                tint = iconTint,
                onClick = onSettingsClick
            )

            ToolbarIconButton(
                icon = Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = iconTint,
                onClick = onVoiceClick
            )

            ToolbarIconButton(
                icon = Icons.Default.TextFields,
                contentDescription = "Font Selection",
                tint = iconTint,
                onClick = onFontsClick
            )
        }
    }
}

@Composable
private fun SynthwaveNavIcon(
    icon: ImageVector,
    desc: String,
    dotColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = Color(0xFFC084FC),
            modifier = Modifier.size(22.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
    }
}


@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}




