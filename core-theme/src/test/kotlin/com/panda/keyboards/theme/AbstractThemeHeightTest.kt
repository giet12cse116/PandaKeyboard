package com.panda.keyboards.theme

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests verifying Abstract theme models and background image resolution.
 */
class AbstractThemeHeightTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `ThemeBackground Image resolves assetPath correctly`() {
        val imageBg = ThemeBackground.Image(
            assetPath = "themes/abstract/1000299774_Default.jpg"
        )

        assertEquals("themes/abstract/1000299774_Default.jpg", imageBg.getAssetPathForHeight(KeyboardHeight.DEFAULT))
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
            assertNotNull("assetPath must be set", img.assetPath)
        }
    }
}
