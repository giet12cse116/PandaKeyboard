package com.panda.keyboards.ime.keyboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer

/**
 * In-keyboard font selector bar rendered directly above key rows.
 *
 * Displays a horizontal scrollable strip of [FontStyle] options.
 * Favorited font styles surface at the beginning of the list with a heart indicator.
 */
@Composable
fun FontSelectionBar(
    activeFontStyle: FontStyle,
    favoriteFontIds: Set<String>,
    onFontStyleSelected: (FontStyle) -> Unit,
    resolvedTheme: ResolvedTheme,
    onCloseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Sort styles so favorited ones surface first
    val sortedStyles = remember(favoriteFontIds) {
        val all = FontStyle.entries
        val favoritesList = all.filter { it.id in favoriteFontIds }
        val nonFavoritesList = all.filter { it.id !in favoriteFontIds }
        favoritesList + nonFavoritesList
    }

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
        if (onCloseClick != null) {
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
                        contentDescription = "Close Font Panel",
                        tint = resolvedTheme.keyText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                items = sortedStyles,
                key = { it.id }
            ) { style ->
                val isSelected = style == activeFontStyle
                val isFavorite = style.id in favoriteFontIds

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) resolvedTheme.accent
                    else resolvedTheme.keyBackground.copy(alpha = 0.75f),
                    label = "chipBg"
                )

                val textColor = if (isSelected) Color.White else resolvedTheme.keyText

                val previewText = remember(style) {
                    if (style == FontStyle.NORMAL) "Normal"
                    else FontTransformer.transform("Abc", style)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .clickable { onFontStyleSelected(style) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFavorite) {
                            Text(
                                text = "♥ ",
                                color = if (isSelected) Color.White else Color(0xFFFF4081),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = previewText,
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
