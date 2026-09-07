package com.panda.keyboards.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shape style for keyboard keys.
 */
@Serializable
enum class KeyShape {
    @SerialName("rounded")
    ROUNDED,

    @SerialName("square")
    SQUARE,

    @SerialName("pill")
    PILL
}

/**
 * Background specification for the keyboard surface.
 *
 * Sealed hierarchy supports solid color, gradient, and image backgrounds.
 * The `type` discriminator is used by kotlinx.serialization for polymorphism.
 */
@Serializable
sealed class ThemeBackground {

    /**
     * Single solid color background.
     * @property color Hex color string (e.g. "#1C1C1E").
     */
    @Serializable
    @SerialName("solid")
    data class SolidColor(val color: String) : ThemeBackground()

    /**
     * Gradient background with multiple color stops.
     * @property colors List of hex color strings from top to bottom.
     * @property angle Gradient angle in degrees (0 = top-to-bottom, 90 = left-to-right).
     */
    @Serializable
    @SerialName("gradient")
    data class Gradient(
        val colors: List<String>,
        val angle: Float = 0f
    ) : ThemeBackground()

    /**
     * Image-based background.
     * @property assetPath Relative path to a bundled asset image.
     */
    @Serializable
    @SerialName("image")
    data class Image(val assetPath: String) : ThemeBackground()
}

/**
 * Complete keyboard theme definition.
 *
 * Themes are loaded from `assets/themes.json` and describe the full
 * visual appearance of the keyboard: key colors, background, shape, and accent.
 *
 * @property id Unique stable identifier (used for persistence).
 * @property name Human-readable display name.
 * @property isPro Whether this theme requires a premium subscription.
 * @property keyBackgroundColor Hex color for key surfaces.
 * @property keyTextColor Hex color for key labels.
 * @property keyboardBackground Background specification (solid, gradient, or image).
 * @property keyShape Shape style for individual keys.
 * @property accentColor Hex color for accent elements (shift active, enter key, etc.).
 */
@Serializable
data class KeyboardTheme(
    val id: String,
    val name: String,
    val isPro: Boolean = false,
    val isCustom: Boolean = false,
    val previewImage: String? = null,
    val keyBackgroundColor: String,
    val keyTextColor: String,
    val keyboardBackground: ThemeBackground,
    val keyShape: KeyShape = KeyShape.ROUNDED,
    val accentColor: String
) {

    companion object {
        /** The default theme ID — must match a theme in themes.json. */
        const val DEFAULT_THEME_ID = "default_dark"
    }
}

/**
 * Wrapper for the themes JSON file structure.
 */
@Serializable
data class ThemeCatalog(
    val themes: List<KeyboardTheme>
)
