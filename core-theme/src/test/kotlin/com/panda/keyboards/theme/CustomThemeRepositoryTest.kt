package com.panda.keyboards.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for custom theme data modeling and defaults.
 */
class CustomThemeRepositoryTest {

    @Test
    fun `KeyboardTheme defaults isCustom to false`() {
        val theme = KeyboardTheme(
            id = "custom_test",
            name = "Test Custom Theme",
            keyBackgroundColor = "#222222",
            keyTextColor = "#FFFFFF",
            keyboardBackground = ThemeBackground.SolidColor("#000000"),
            accentColor = "#7C4DFF"
        )
        assertFalse(theme.isCustom)
        assertFalse(theme.isPro)
    }

    @Test
    fun `KeyboardTheme custom instance has isCustom set to true`() {
        val customTheme = KeyboardTheme(
            id = "custom_1",
            name = "My Custom Theme",
            isCustom = true,
            isPro = false,
            keyBackgroundColor = "#121212",
            keyTextColor = "#EEEEEE",
            keyboardBackground = ThemeBackground.Gradient(listOf("#121212", "#000000")),
            keyShape = KeyShape.PILL,
            accentColor = "#FF4081"
        )
        assertTrue(customTheme.isCustom)
        assertFalse(customTheme.isPro)
        assertEquals(KeyShape.PILL, customTheme.keyShape)
    }
}
