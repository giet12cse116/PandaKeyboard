package com.panda.keyboards.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Unit tests for [ThemeRepository] static catalog parsing and validation.
 *
 * These tests exercise [ThemeRepository.parseCatalog] which doesn't need
 * Android Context. Integration tests for DataStore persistence require
 * an instrumented test environment.
 */
class ThemeRepositoryTest {

    @Test
    fun `parseCatalog returns correct number of themes`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        assertEquals(6, themes.size)
    }

    @Test
    fun `parseCatalog returns themes with expected IDs`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        val ids = themes.map { it.id }.toSet()
        assertEquals(
            setOf("default_dark", "midnight", "pastel_dream", "forest", "sunset_glow", "panda"),
            ids
        )
    }

    @Test
    fun `parseCatalog default theme is present`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        val defaultTheme = themes.find { it.id == KeyboardTheme.DEFAULT_THEME_ID }
        assertNotNull(defaultTheme)
        assertEquals("Default Dark", defaultTheme!!.name)
    }

    @Test
    fun `parseCatalog pro themes count is correct`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        assertEquals(2, themes.count { it.isPro })
        assertEquals(4, themes.count { !it.isPro })
    }

    @Test
    fun `parseCatalog all themes have non-blank required fields`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        themes.forEach { theme ->
            assertTrue("id should not be blank", theme.id.isNotBlank())
            assertTrue("name should not be blank", theme.name.isNotBlank())
            assertTrue("keyBackgroundColor should not be blank", theme.keyBackgroundColor.isNotBlank())
            assertTrue("keyTextColor should not be blank", theme.keyTextColor.isNotBlank())
            assertTrue("accentColor should not be blank", theme.accentColor.isNotBlank())
        }
    }

    @Test
    fun `parseCatalog handles empty themes array`() {
        val emptyJson = """{"themes": []}"""
        val themes = ThemeRepository.parseCatalog(emptyJson)
        assertTrue(themes.isEmpty())
    }

    @Test
    fun `parseCatalog ignores unknown fields gracefully`() {
        val jsonWithExtra = """
        {
          "themes": [
            {
              "id": "test",
              "name": "Test",
              "unknownField": "should be ignored",
              "keyBackgroundColor": "#000",
              "keyTextColor": "#FFF",
              "keyboardBackground": {"type": "solid", "color": "#000"},
              "accentColor": "#FFF"
            }
          ]
        }
        """.trimIndent()
        val themes = ThemeRepository.parseCatalog(jsonWithExtra)
        assertEquals(1, themes.size)
        assertEquals("test", themes[0].id)
    }

    @Test
    fun `parseCatalog preserves theme ordering`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        assertEquals("default_dark", themes[0].id)
        assertEquals("midnight", themes[1].id)
        assertEquals("pastel_dream", themes[2].id)
        assertEquals("forest", themes[3].id)
        assertEquals("sunset_glow", themes[4].id)
        assertEquals("panda", themes[5].id)
    }

    @Test
    fun `parseCatalog themes have valid hex color format`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        val hexColorRegex = Regex("^#[0-9A-Fa-f]{3,8}$")
        themes.forEach { theme ->
            assertTrue(
                "keyBackgroundColor '${theme.keyBackgroundColor}' should be hex",
                hexColorRegex.matches(theme.keyBackgroundColor)
            )
            assertTrue(
                "keyTextColor '${theme.keyTextColor}' should be hex",
                hexColorRegex.matches(theme.keyTextColor)
            )
            assertTrue(
                "accentColor '${theme.accentColor}' should be hex",
                hexColorRegex.matches(theme.accentColor)
            )
        }
    }

    @Test
    fun `parseCatalog gradient backgrounds have at least 2 colors`() {
        val themes = ThemeRepository.parseCatalog(ThemeModelTest.SAMPLE_CATALOG_JSON)
        themes.forEach { theme ->
            val bg = theme.keyboardBackground
            if (bg is ThemeBackground.Gradient) {
                assertTrue(
                    "Gradient for '${theme.id}' should have >= 2 colors",
                    bg.colors.size >= 2
                )
            }
        }
    }
}
