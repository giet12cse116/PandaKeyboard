package com.panda.keyboards.ime.keyboard

import androidx.compose.ui.graphics.Color
import com.panda.keyboards.theme.KeyShape
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardThemeMapperTest {

    @Test
    fun parseColor_validHex_returnsCorrectColor() {
        val color = KeyboardThemeMapper.parseColor("#FF123456")
        assertEquals(Color(0xFF123456), color)
    }

    @Test
    fun parseColor_invalidHex_returnsFallback() {
        val fallback = Color(0xFF111111)
        val color = KeyboardThemeMapper.parseColor("invalid", fallback)
        assertEquals(fallback, color)
    }

    @Test
    fun resolveTheme_mapsThemePropertiesCorrectly() {
        val theme = KeyboardTheme(
            id = "test-theme",
            name = "Test Theme",
            isPro = false,
            keyBackgroundColor = "#FF222222",
            keyTextColor = "#FFFFFFFF",
            keyboardBackground = ThemeBackground.SolidColor("#FF111111"),
            keyShape = KeyShape.PILL,
            accentColor = "#FF7C4DFF"
        )

        val resolved = KeyboardThemeMapper.resolveTheme(theme)

        assertEquals(Color(0xFF222222), resolved.keyBackground)
        assertEquals(Color(0xFFFFFFFF), resolved.keyText)
        assertEquals(Color(0xFF7C4DFF), resolved.accent)
    }

    @Test
    fun resolveTheme_nullTheme_returnsDefaultTheme() {
        val resolved = KeyboardThemeMapper.resolveTheme(null)
        assertEquals(ResolvedTheme.DEFAULT.keyBackground, resolved.keyBackground)
    }
}
