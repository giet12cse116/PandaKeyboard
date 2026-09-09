package com.panda.keyboards.theme

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests verifying Abstract theme models and height-dependent background image resolution.
 */
class AbstractThemeHeightTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `ThemeBackground Image resolves correct path for COMPACT height`() {
        val imageBg = ThemeBackground.Image(
            assetPath = "themes/abstract/1000299774_Default.jpg",
            compactAssetPath = "themes/abstract/1000299774_Compact.jpg",
            defaultAssetPath = "themes/abstract/1000299774_Default.jpg",
            tallAssetPath = "themes/abstract/1000299774_Tall.jpg"
        )

        assertEquals("themes/abstract/1000299774_Compact.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.COMPACT))
    }

    @Test
    fun `ThemeBackground Image resolves correct path for DEFAULT height`() {
        val imageBg = ThemeBackground.Image(
            assetPath = "themes/abstract/1000299774_Default.jpg",
            compactAssetPath = "themes/abstract/1000299774_Compact.jpg",
            defaultAssetPath = "themes/abstract/1000299774_Default.jpg",
            tallAssetPath = "themes/abstract/1000299774_Tall.jpg"
        )

        assertEquals("themes/abstract/1000299774_Default.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.DEFAULT))
    }

    @Test
    fun `ThemeBackground Image resolves correct path for TALL height`() {
        val imageBg = ThemeBackground.Image(
            assetPath = "themes/abstract/1000299774_Default.jpg",
            compactAssetPath = "themes/abstract/1000299774_Compact.jpg",
            defaultAssetPath = "themes/abstract/1000299774_Default.jpg",
            tallAssetPath = "themes/abstract/1000299774_Tall.jpg"
        )

        assertEquals("themes/abstract/1000299774_Tall.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.TALL))
    }

    @Test
    fun `ThemeBackground Image falls back to assetPath when height paths missing`() {
        val imageBg = ThemeBackground.Image(assetPath = "/custom/user/path.jpg")

        assertEquals("/custom/user/path.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.COMPACT))
        assertEquals("/custom/user/path.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.DEFAULT))
        assertEquals("/custom/user/path.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.TALL))
    }

    @Test
    fun `bundled themes json includes 10 Abstract category themes`() {
        val themesFile = File("src/main/assets/themes.json")
        assertTrue("themes.json asset file must exist", themesFile.exists())

        val catalogJson = themesFile.readText()
        val catalog = json.decodeFromString<ThemeCatalog>(catalogJson)

        val abstractThemes = catalog.themes.filter { it.category.equals("abstract", ignoreCase = true) }
        assertEquals("Must contain 10 abstract category themes", 10, abstractThemes.size)

        val imageThemes = catalog.themes.filter { it.keyboardBackground is ThemeBackground.Image }
        for (theme in imageThemes) {
            val img = theme.keyboardBackground as ThemeBackground.Image
            assertNotNull("Compact path must be set", img.compactAssetPath)
            assertNotNull("Default path must be set", img.defaultAssetPath)
            assertNotNull("Tall path must be set", img.tallAssetPath)
            assertTrue("Compact image path must end with _Compact.jpg", img.compactAssetPath!!.endsWith("_Compact.jpg"))
            assertTrue("Default image path must end with _Default.jpg", img.defaultAssetPath!!.endsWith("_Default.jpg"))
            assertTrue("Tall image path must end with _Tall.jpg", img.tallAssetPath!!.endsWith("_Tall.jpg"))
        }
    }
}
