package com.panda.keyboards.theme

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertIs

/**
 * Unit tests for the theme data models and JSON serialization.
 *
 * These are JVM-only tests (no Android context required) that verify:
 * - JSON parsing produces correct model instances
 * - All theme fields are deserialized properly
 * - ThemeBackground sealed hierarchy is parsed via discriminator
 * - KeyShape enum serialization round-trips correctly
 * - Edge cases (missing optional fields, extra fields) are handled
 */
class ThemeModelTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ── Full catalog parsing ────────────────────────────────────────────

    @Test
    fun `themes json parses into expected number of themes`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        assertEquals(6, catalog.themes.size)
    }

    @Test
    fun `all theme IDs are unique`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val ids = catalog.themes.map { it.id }
        assertEquals(ids.distinct().size, ids.size)
    }

    @Test
    fun `default theme exists in catalog`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        assertTrue(
            catalog.themes.any { it.id == KeyboardTheme.DEFAULT_THEME_ID }
        )
    }

    @Test
    fun `pro themes are correctly marked`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val proThemes = catalog.themes.filter { it.isPro }
        val freeThemes = catalog.themes.filter { !it.isPro }
        assertEquals(2, proThemes.size)
        assertEquals(4, freeThemes.size)
    }

    // ── Individual theme parsing ────────────────────────────────────────

    @Test
    fun `default dark theme has correct fields`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val theme = catalog.themes.first { it.id == "default_dark" }

        assertEquals("Default Dark", theme.name)
        assertFalse(theme.isPro)
        assertEquals("#3A3A3C", theme.keyBackgroundColor)
        assertEquals("#FFFFFF", theme.keyTextColor)
        assertEquals(KeyShape.ROUNDED, theme.keyShape)
        assertEquals("#7C4DFF", theme.accentColor)
    }

    // ── ThemeBackground sealed hierarchy ────────────────────────────────

    @Test
    fun `solid color background parses correctly`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val defaultTheme = catalog.themes.first { it.id == "default_dark" }

        assertIs<ThemeBackground.SolidColor>(defaultTheme.keyboardBackground)
        val bg = defaultTheme.keyboardBackground as ThemeBackground.SolidColor
        assertEquals("#1C1C1E", bg.color)
    }

    @Test
    fun `gradient background parses correctly`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val midnight = catalog.themes.first { it.id == "midnight" }

        assertIs<ThemeBackground.Gradient>(midnight.keyboardBackground)
        val bg = midnight.keyboardBackground as ThemeBackground.Gradient
        assertEquals(3, bg.colors.size)
        assertEquals("#0F0F23", bg.colors[0])
        assertEquals(0f, bg.angle)
    }

    @Test
    fun `gradient with angle parses correctly`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val pastel = catalog.themes.first { it.id == "pastel_dream" }

        assertIs<ThemeBackground.Gradient>(pastel.keyboardBackground)
        val bg = pastel.keyboardBackground as ThemeBackground.Gradient
        assertEquals(45f, bg.angle)
    }

    // ── KeyShape enum ───────────────────────────────────────────────────

    @Test
    fun `all key shapes are represented in catalog`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        val shapes = catalog.themes.map { it.keyShape }.toSet()
        assertEquals(
            setOf(KeyShape.ROUNDED, KeyShape.PILL, KeyShape.SQUARE),
            shapes
        )
    }

    // ── Serialization round-trip ────────────────────────────────────────

    @Test
    fun `theme serialization round-trips correctly`() {
        val original = KeyboardTheme(
            id = "test",
            name = "Test Theme",
            isPro = true,
            keyBackgroundColor = "#FF0000",
            keyTextColor = "#00FF00",
            keyboardBackground = ThemeBackground.Gradient(
                colors = listOf("#AA0000", "#BB0000"),
                angle = 90f
            ),
            keyShape = KeyShape.PILL,
            accentColor = "#0000FF"
        )
        val encoded = json.encodeToString(KeyboardTheme.serializer(), original)
        val decoded = json.decodeFromString<KeyboardTheme>(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `isPro defaults to false when missing`() {
        val themeJson = """
            {
                "id": "test", "name": "Test",
                "keyBackgroundColor": "#000", "keyTextColor": "#FFF",
                "keyboardBackground": {"type": "solid", "color": "#000"},
                "accentColor": "#FFF"
            }
        """.trimIndent()
        val theme = json.decodeFromString<KeyboardTheme>(themeJson)
        assertFalse(theme.isPro)
    }

    @Test
    fun `keyShape defaults to ROUNDED when missing`() {
        val themeJson = """
            {
                "id": "test", "name": "Test",
                "keyBackgroundColor": "#000", "keyTextColor": "#FFF",
                "keyboardBackground": {"type": "solid", "color": "#000"},
                "accentColor": "#FFF"
            }
        """.trimIndent()
        val theme = json.decodeFromString<KeyboardTheme>(themeJson)
        assertEquals(KeyShape.ROUNDED, theme.keyShape)
    }

    // ── Gradient angle default ──────────────────────────────────────────

    @Test
    fun `gradient angle defaults to 0 when missing`() {
        val bgJson = """{"type": "gradient", "colors": ["#AAA", "#BBB"]}"""
        val bg = json.decodeFromString<ThemeBackground>(bgJson)
        assertIs<ThemeBackground.Gradient>(bg)
        assertEquals(0f, (bg as ThemeBackground.Gradient).angle)
    }

    // ── Sample catalog JSON (same content as assets/themes.json) ────────

    companion object {
        val SAMPLE_CATALOG_JSON = """
        {
          "themes": [
            {
              "id": "default_dark",
              "name": "Default Dark",
              "isPro": false,
              "keyBackgroundColor": "#3A3A3C",
              "keyTextColor": "#FFFFFF",
              "keyboardBackground": {"type": "solid", "color": "#1C1C1E"},
              "keyShape": "rounded",
              "accentColor": "#7C4DFF"
            },
            {
              "id": "midnight",
              "name": "Midnight",
              "isPro": false,
              "keyBackgroundColor": "#1A1A2E",
              "keyTextColor": "#E0E0FF",
              "keyboardBackground": {"type": "gradient", "colors": ["#0F0F23", "#1A1A2E", "#16213E"], "angle": 0},
              "keyShape": "rounded",
              "accentColor": "#5C6BC0"
            },
            {
              "id": "pastel_dream",
              "name": "Pastel Dream",
              "isPro": false,
              "keyBackgroundColor": "#F5E6F0",
              "keyTextColor": "#4A4A4A",
              "keyboardBackground": {"type": "gradient", "colors": ["#FFECD2", "#FCB69F", "#FF9A9E"], "angle": 45},
              "keyShape": "pill",
              "accentColor": "#FF6B9D"
            },
            {
              "id": "forest",
              "name": "Forest",
              "isPro": false,
              "keyBackgroundColor": "#2D4A3E",
              "keyTextColor": "#E8F5E9",
              "keyboardBackground": {"type": "gradient", "colors": ["#1B2A21", "#2D4A3E"], "angle": 0},
              "keyShape": "rounded",
              "accentColor": "#66BB6A"
            },
            {
              "id": "sunset_glow",
              "name": "Sunset Glow",
              "isPro": true,
              "keyBackgroundColor": "#3D2040",
              "keyTextColor": "#FFE0B2",
              "keyboardBackground": {"type": "gradient", "colors": ["#1A0A2E", "#3D1053", "#6D2077"], "angle": 135},
              "keyShape": "pill",
              "accentColor": "#FF7043"
            },
            {
              "id": "panda",
              "name": "Panda",
              "isPro": true,
              "keyBackgroundColor": "#FAFAFA",
              "keyTextColor": "#212121",
              "keyboardBackground": {"type": "solid", "color": "#EEEEEE"},
              "keyShape": "square",
              "accentColor": "#7C4DFF"
            }
          ]
        }
        """.trimIndent()
    }
}
