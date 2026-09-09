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
 * Sealed hierarchy describing polymorphic key visual styles.
 *
 * Designed to support data-driven visual style definitions (Glassmorphism,
 * Neubrutalism, Claymorphism, Neumorphism, etc.) without altering core rendering code.
 */
@Serializable
sealed class KeyVisualStyle {

    /**
     * Standard flat keys (default behavior for all shipped themes).
     */
    @Serializable
    @SerialName("flat")
    data object Flat : KeyVisualStyle()

    /**
     * Glassmorphic style with backdrop blur, translucency, hairline border, and glow.
     *
     * @property blurRadiusDp Blur radius applied on API 31+.
     * @property translucencyAlpha Translucency factor for key surface fill (0f..1f).
     * @property borderColorHex Hex string for hairline key border outline.
     * @property glowGradientColors Color stops for glowing underlay behind key grid.
     */
    @Serializable
    @SerialName("glassmorphic")
    data class Glassmorphic(
        val blurRadiusDp: Float = 14f,
        val translucencyAlpha: Float = 0.35f,
        val borderColorHex: String = "#60FFFFFF",
        val glowGradientColors: List<String> = listOf("#9000E5FF", "#608A2BE2", "#00000000")
    ) : KeyVisualStyle()

    /**
     * Semi-Flat (Flat 2.0) style with subtle tonal elevation drop shadow and press-down shift.
     *
     * @property elevationDp Unpressed key surface elevation shadow offset in DP.
     * @property shadowColorHex Hex color string for key elevation drop shadow.
     * @property lightSourceAngle Simulated light source angle in degrees (90f = top-down light).
     * @property pressedElevationDp Dynamic key surface elevation shadow offset when key is physically pressed down.
     */
    @Serializable
    @SerialName("semi_flat")
    data class SemiFlat(
        val elevationDp: Float = 3f,
        val shadowColorHex: String = "#40000000",
        val lightSourceAngle: Float = 90f,
        val pressedElevationDp: Float = 0.5f
    ) : KeyVisualStyle()

    /**
     * Neo-Brutalism style with thick outlines, high-contrast saturated blocks, and hard drop shadow offset.
     *
     * @property borderWidthDp Border stroke width in DP.
     * @property borderColorHex Hex string for key border outline (default black).
     * @property shadowOffsetDp Hard drop shadow offset distance in DP.
     * @property shadowColorHex Hex string for hard drop shadow color (default black).
     */
    @Serializable
    @SerialName("neobrutalist")
    data class Neobrutalist(
        val borderWidthDp: Float = 2.5f,
        val borderColorHex: String = "#000000",
        val shadowOffsetDp: Float = 4f,
        val shadowColorHex: String = "#000000"
    ) : KeyVisualStyle()

    /**
     * Claymorphism 3D style with soft inflated 3D geometry and deep pastel elevation shadows.
     *
     * @property elevationDp Unpressed key surface elevation shadow offset in DP.
     * @property shadowColorHex Soft elevation drop shadow color.
     * @property pressedElevationDp Elevation shadow offset when key is physically pressed down.
     */
    @Serializable
    @SerialName("claymorphic")
    data class Claymorphic(
        val elevationDp: Float = 4.5f,
        val shadowColorHex: String = "#403730A3",
        val pressedElevationDp: Float = 1f
    ) : KeyVisualStyle()
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
     * @property assetPath Relative path to a bundled asset image or custom file path.
     * @property compactAssetPath Optional image path for Compact keyboard height.
     * @property defaultAssetPath Optional image path for Default keyboard height.
     * @property tallAssetPath Optional image path for Tall keyboard height.
     */
    @Serializable
    @SerialName("image")
    data class Image(
        val assetPath: String,
        val compactAssetPath: String? = null,
        val defaultAssetPath: String? = null,
        val tallAssetPath: String? = null
    ) : ThemeBackground() {
        /**
         * Resolve the appropriate asset path corresponding to the given [KeyboardHeight].
         */
        fun getAssetPathForHeight(height: KeyboardHeight): String {
            return when (height) {
                KeyboardHeight.COMPACT -> compactAssetPath ?: assetPath
                KeyboardHeight.DEFAULT -> defaultAssetPath ?: assetPath
                KeyboardHeight.TALL -> tallAssetPath ?: assetPath
            }
        }
    }
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
 * @property isCustom Whether this theme was created by the user.
 * @property isExperimental Whether this theme is temporary/experimental (e.g. test theme).
 * @property category Visual style category (e.g. "abstract", "classic", "gradient", "solid", "test").
 * @property previewImage Optional static preview image resource name.
 * @property keyBackgroundColor Hex color for key surfaces.
 * @property keyTextColor Hex color for key labels.
 * @property keyboardBackground Background specification (solid, gradient, or image).
 * @property keyShape Shape style for individual keys.
 * @property accentColor Hex color for accent elements (shift active, enter key, etc.).
 * @property keyStyle Key visual style (Flat, Glassmorphic, etc.). Defaults to Flat.
 */
@Serializable
data class KeyboardTheme(
    val id: String,
    val name: String,
    val isPro: Boolean = false,
    val isCustom: Boolean = false,
    val isExperimental: Boolean = false,
    val category: String = "classic",
    val previewImage: String? = null,
    val keyBackgroundColor: String,
    val keyTextColor: String,
    val keyboardBackground: ThemeBackground,
    val keyShape: KeyShape = KeyShape.ROUNDED,
    val accentColor: String,
    val keyBorderColor: String? = null,
    val keyBorderWidthDp: Float = 0f,
    val keyShadowColor: String? = null,
    val keyShadowOffsetDp: Float = 0f,
    val keyStyle: KeyVisualStyle = KeyVisualStyle.Flat
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
