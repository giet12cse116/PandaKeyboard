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
        assertEquals(KeyVisualStyle.Flat, theme.keyStyle)
        assertFalse(theme.isExperimental)
    }

    // ── KeyVisualStyle polymorphic serialization ────────────────────────

    @Test
    fun `default themes parse keyStyle as Flat when omitted`() {
        val catalog = json.decodeFromString<ThemeCatalog>(SAMPLE_CATALOG_JSON)
        for (theme in catalog.themes) {
            assertEquals(KeyVisualStyle.Flat, theme.keyStyle)
        }
    }

    @Test
    fun `glassmorphic keyStyle parses correctly with parameters`() {
        val glassJson = """
            {
              "id": "glass_test_experimental",
              "name": "Glass Test",
              "isPro": false,
              "isExperimental": true,
              "category": "Test",
              "keyBackgroundColor": "#40FFFFFF",
              "keyTextColor": "#FFFFFF",
              "keyboardBackground": {"type": "solid", "color": "#0F172A"},
              "keyShape": "rounded",
              "accentColor": "#00E5FF",
              "keyStyle": {
                "type": "glassmorphic",
                "blurRadiusDp": 14.0,
                "translucencyAlpha": 0.35,
                "borderColorHex": "#60FFFFFF",
                "glowGradientColors": ["#9000E5FF", "#608A2BE2", "#00000000"]
              }
            }
        """.trimIndent()

        val theme = json.decodeFromString<KeyboardTheme>(glassJson)
        assertEquals("Glass Test", theme.name)
        assertTrue(theme.isExperimental)
        assertEquals("Test", theme.category)
        assertIs<KeyVisualStyle.Glassmorphic>(theme.keyStyle)

        val glass = theme.keyStyle as KeyVisualStyle.Glassmorphic
        assertEquals(14.0f, glass.blurRadiusDp)
        assertEquals(0.35f, glass.translucencyAlpha)
        assertEquals("#60FFFFFF", glass.borderColorHex)
        assertEquals(3, glass.glowGradientColors.size)
    }

    @Test
    fun `semi_flat keyStyle parses correctly with parameters`() {
        val semiFlatJson = """
            {
              "id": "studio_dark",
              "name": "Studio Dark",
              "isPro": false,
              "isExperimental": false,
              "category": "Solid",
              "keyBackgroundColor": "#2A2B2E",
              "keyTextColor": "#F0F0F2",
              "keyboardBackground": {"type": "solid", "color": "#18191B"},
              "keyShape": "rounded",
              "accentColor": "#00E5FF",
              "keyStyle": {
                "type": "semi_flat",
                "elevationDp": 3.5,
                "shadowColorHex": "#55000000",
                "lightSourceAngle": 90.0,
                "pressedElevationDp": 0.5
              }
            }
        """.trimIndent()

        val theme = json.decodeFromString<KeyboardTheme>(semiFlatJson)
        assertEquals("Studio Dark", theme.name)
        assertFalse(theme.isExperimental)
        assertIs<KeyVisualStyle.SemiFlat>(theme.keyStyle)

        val semiFlat = theme.keyStyle as KeyVisualStyle.SemiFlat
        assertEquals(3.5f, semiFlat.elevationDp)
        assertEquals("#55000000", semiFlat.shadowColorHex)
        assertEquals(0.5f, semiFlat.pressedElevationDp)
    }

    @Test
    fun `neobrutalist keyStyle parses correctly with parameters`() {
        val jsonStr = """
            {
              "id": "test_neobrutalism",
              "name": "Neo-Brutalism",
              "isPro": false,
              "isExperimental": true,
              "category": "Test",
              "keyBackgroundColor": "#FF5353",
              "keyTextColor": "#000000",
              "keyboardBackground": {"type": "solid", "color": "#FFD027"},
              "keyShape": "rounded",
              "accentColor": "#4D88FF",
              "keyStyle": {
                "type": "neobrutalist",
                "borderWidthDp": 2.5,
                "borderColorHex": "#000000",
                "shadowOffsetDp": 4.0,
                "shadowColorHex": "#000000"
              }
            }
        """.trimIndent()
        val theme = json.decodeFromString<KeyboardTheme>(jsonStr)
        assertIs<KeyVisualStyle.Neobrutalist>(theme.keyStyle)
        val neo = theme.keyStyle as KeyVisualStyle.Neobrutalist
        assertEquals(2.5f, neo.borderWidthDp)
        assertEquals(4.0f, neo.shadowOffsetDp)
    }

    @Test
    fun `claymorphic keyStyle parses correctly with parameters`() {
        val jsonStr = """
            {
              "id": "test_claymorphism_3d",
              "name": "Claymorphism 3D",
              "isPro": false,
              "isExperimental": true,
              "category": "Test",
              "keyBackgroundColor": "#EEF2FF",
              "keyTextColor": "#312E81",
              "keyboardBackground": {"type": "solid", "color": "#C7D2FE"},
              "keyShape": "pill",
              "accentColor": "#6366F1",
              "keyStyle": {
                "type": "claymorphic",
                "elevationDp": 4.5,
                "shadowColorHex": "#403730A3",
                "pressedElevationDp": 1.0
              }
            }
        """.trimIndent()
        val theme = json.decodeFromString<KeyboardTheme>(jsonStr)
        assertIs<KeyVisualStyle.Claymorphic>(theme.keyStyle)
        val clay = theme.keyStyle as KeyVisualStyle.Claymorphic
        assertEquals(4.5f, clay.elevationDp)
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
