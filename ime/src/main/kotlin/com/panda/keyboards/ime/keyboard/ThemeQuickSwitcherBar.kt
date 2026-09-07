package com.panda.keyboards.ime.keyboard

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import java.io.File

/**
 * Horizontal scrollable quick-theme switcher strip for the in-keyboard top panel.
 * Tapping a theme swatch live-applies the theme instantly without leaving the keyboard.
 */
@Composable
fun ThemeQuickSwitcherBar(
    themes: List<KeyboardTheme>,
    currentThemeId: String?,
    onThemeSelected: (String) -> Unit,
    onCloseClick: () -> Unit,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close Button (returns to 4-icon toolbar)
        Surface(
            shape = CircleShape,
            color = resolvedTheme.keyBackground,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onCloseClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Theme Panel",
                    tint = resolvedTheme.keyText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Horizontal Scrollable Theme Swatches
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                items = themes,
                key = { it.id }
            ) { theme ->
                val isSelected = (theme.id == currentThemeId)
                ThemeSwatchItem(
                    theme = theme,
                    isSelected = isSelected,
                    onClick = { onThemeSelected(theme.id) },
                    resolvedTheme = resolvedTheme
                )
            }
        }
    }
}

@Composable
private fun ThemeSwatchItem(
    theme: KeyboardTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    resolvedTheme: ResolvedTheme,
    modifier: Modifier = Modifier
) {
    val swatchModifier = modifier
        .width(54.dp)
        .height(34.dp)
        .clip(RoundedCornerShape(6.dp))
        .border(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) resolvedTheme.accent else Color.White.copy(alpha = 0.3f),
            shape = RoundedCornerShape(6.dp)
        )
        .clickable(onClick = onClick)

    Box(
        modifier = swatchModifier,
        contentAlignment = Alignment.Center
    ) {
        // Background swatch rendering
        when (val bg = theme.keyboardBackground) {
            is ThemeBackground.SolidColor -> {
                val color = parseSwatchHex(bg.color)
                Box(modifier = Modifier.fillMaxSize().background(color))
            }
            is ThemeBackground.Gradient -> {
                val colors = bg.colors.map { parseSwatchHex(it) }
                val brush = Brush.linearGradient(colors = if (colors.size >= 2) colors else listOf(Color.DarkGray, Color.Black))
                Box(modifier = Modifier.fillMaxSize().background(brush))
            }
            is ThemeBackground.Image -> {
                val imageBitmap = remember(bg.assetPath) {
                    try {
                        val file = File(bg.assetPath)
                        if (file.exists()) BitmapFactory.decodeFile(bg.assetPath)?.asImageBitmap() else null
                    } catch (e: Exception) {
                        null
                    }
                }
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
                }
            }
        }

        // Active Theme Checkmark
        if (isSelected) {
            Surface(
                shape = CircleShape,
                color = resolvedTheme.accent,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun parseSwatchHex(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorLong = when (clean.length) {
            6 -> "FF$clean".toLong(16)
            8 -> clean.toLong(16)
            else -> 0xFF000000L
        }
        Color(colorLong)
    } catch (e: Exception) {
        Color.DarkGray
    }
}
