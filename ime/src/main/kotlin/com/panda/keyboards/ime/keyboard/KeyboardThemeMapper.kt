package com.panda.keyboards.ime.keyboard

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.panda.keyboards.theme.KeyShape
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mapper helper object for theme resolution and color parsing.
 */
object KeyboardThemeMapper {
    fun parseColor(hex: String, fallback: Color = Color.Black): Color {
        val cleaned = hex.removePrefix("#")
        if (cleaned.length != 3 && cleaned.length != 6 && cleaned.length != 8) {
            return fallback
        }
        return try {
            ResolvedTheme.parseColor(hex)
        } catch (e: Exception) {
            fallback
        }
    }

    fun resolveTheme(
        theme: KeyboardTheme?,
        height: com.panda.keyboards.theme.KeyboardHeight = com.panda.keyboards.theme.KeyboardHeight.DEFAULT
    ): ResolvedTheme {
        return if (theme != null) ResolvedTheme.from(theme, height) else ResolvedTheme.DEFAULT
    }
}

/**
 * Resolved colors and shapes from a [KeyboardTheme], ready for Compose consumption.
 *
 * Converts hex strings → [Color], [KeyShape] → Compose [Shape],
 * and [ThemeBackground] → Compose [Brush] or solid [Color].
 *
 * This class is the single mapping point between the serialized theme
 * data model and the rendering layer. No other composable should parse
 * hex colors directly.
 */
data class ResolvedTheme(
    /** Key surface color. */
    val keyBackground: Color,
    /** Key surface color with slight lightening for pressed state. */
    val keyBackgroundPressed: Color,
    /** Special key (Shift, Backspace, Symbols, etc.) background. */
    val specialKeyBackground: Color,
    /** Special key pressed background. */
    val specialKeyBackgroundPressed: Color,
    /** Key label color for character keys. */
    val keyText: Color,
    /** Key label color for special keys (same as keyText unless theme overrides). */
    val specialKeyText: Color,
    /** Accent color for shift-active, caps-lock indicator. */
    val accent: Color,
    /** Lightened accent for caps-lock. */
    val accentLight: Color,
    /** Background color or brush for the keyboard surface. */
    val keyboardBackground: Color,
    /** Optional gradient brush for the keyboard surface (null = use solid). */
    val keyboardBackgroundBrush: Brush?,
    /** Optional local file path for image background (null = use color/gradient). */
    val imagePath: String? = null,
    /** Compose shape for individual keys. */
    val keyShape: Shape,
    /** Optional border outline color for keys. */
    val keyBorderColor: Color? = null,
    /** Key border stroke width in DP. */
    val keyBorderWidthDp: Float = 0f,
    /** Optional hard drop shadow / highlight color for keys. */
    val keyShadowColor: Color? = null,
    /** Key shadow offset distance in DP. */
    val keyShadowOffsetDp: Float = 0f,
    /** Polymorphic key visual style (Flat, Glassmorphic, etc.). */
    val keyStyle: com.panda.keyboards.theme.KeyVisualStyle = com.panda.keyboards.theme.KeyVisualStyle.Flat,
    /** Radial or linear glow brush for glassmorphism underlay (null for flat themes). */
    val glassmorphicGlowBrush: Brush? = null,
    /** Key opacity alpha factor (0.0f to 1.0f). */
    val keyOpacityAlpha: Float = 1.0f,
    /** Optional popular key background emblem (e.g. "panda", "heart", "puppy"). */
    val decorativeIcon: String? = null,
    /** Font size in SP for key labels. */
    val fontSizeSp: Int = 16,
    /** Font style name (Default, Bold, Rounded, Modern, Playful). */
    val fontStyleName: String = "rounded",
    /** Whether text shadow is enabled. */
    val hasTextShadow: Boolean = false,
    /** Whether this is a user-created custom theme. */
    val isCustom: Boolean = false
) {
    /** Helper property to retrieve [KeyVisualStyle.AssetSkin] if active. */
    val assetSkinStyle: com.panda.keyboards.theme.KeyVisualStyle.AssetSkin?
        get() = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.AssetSkin

    /** Helper property to retrieve [KeyVisualStyle.OrigamiPaperCraft] if active. */
    val origamiPaperCraftStyle: com.panda.keyboards.theme.KeyVisualStyle.OrigamiPaperCraft?
        get() = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.OrigamiPaperCraft

    /** Helper property to retrieve [KeyVisualStyle.PizzaSlice] if active. */
    val pizzaSliceStyle: com.panda.keyboards.theme.KeyVisualStyle.PizzaSlice?
        get() = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.PizzaSlice

    /** Helper property to retrieve [KeyVisualStyle.SteampunkIndustrial] if active. */
    val steampunkIndustrialStyle: com.panda.keyboards.theme.KeyVisualStyle.SteampunkIndustrial?
        get() = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.SteampunkIndustrial

    /** Helper property to retrieve [KeyVisualStyle.SynthwaveCyberpunkNeon] if active. */
    val synthwaveCyberpunkNeonStyle: com.panda.keyboards.theme.KeyVisualStyle.SynthwaveCyberpunkNeon?
        get() = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.SynthwaveCyberpunkNeon

    /** Helper property to retrieve [KeyVisualStyle.ThemeKeyBackgrounds] if active. */
    val themeKeyBackgroundsStyle: com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds?
        get() = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds

    companion object {

        /** Default dark theme — matches the Sprint 2 hardcoded appearance. */
        val DEFAULT: ResolvedTheme
            get() = from(
                KeyboardTheme(
                    id = "default_dark",
                    name = "Default Dark",
                    keyBackgroundColor = "#3A3A3C",
                    keyTextColor = "#FFFFFF",
                    keyboardBackground = ThemeBackground.SolidColor("#1C1C1E"),
                    keyShape = KeyShape.ROUNDED,
                    accentColor = "#7C4DFF"
                )
            )

        /**
         * Map decorativeIcon string to ThemeKeyBackgrounds if keyStyle is Flat.
         */
        fun resolveThemeKeyBackgrounds(decorativeIcon: String?): com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds? {
            return when (decorativeIcon) {
                "ic_popular_sun", "sun", "theme_sun" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_sun_space", "theme_sun_special", "theme_sun_emoji")
                "ic_popular_sunflower", "sunflower", "theme_sunflower" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_sunflower_space", "theme_sunflower_special", "theme_sunflower_emoji")
                "ic_popular_burger", "burger", "theme_burger" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_burger_space", "theme_burger_special", "theme_burger_emoji")
                "ic_popular_pizza", "pizza", "theme_pizza" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_pizza_space", "theme_pizza_special", "theme_pizza_emoji")
                "ic_popular_cookie", "cookie", "theme_cookie" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_cookie_space", "theme_cookie_special", "theme_cookie_emoji")
                "ic_popular_donut", "donut", "theme_donut" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_donut_space", "theme_donut_special", "theme_donut_emoji")
                "ic_popular_mango", "mango", "theme_mango" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_mango_space", "theme_mango_special", "theme_mango_emoji")
                "ic_popular_heart", "heart", "theme_heart" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_heart_space", "theme_heart_special", "theme_heart_emoji")
                "ic_popular_star", "star", "theme_star" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_star_space", "theme_star_special", "theme_star_emoji")
                "ic_popular_panda", "panda", "theme_panda" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_panda_space", "theme_panda_special", "theme_panda_emoji")
                "ic_popular_pig", "pig", "theme_pig" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_pig_space", "theme_pig_special", "theme_pig_emoji")
                "ic_popular_flower", "flower", "theme_flower" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_flower_space", "theme_flower_special", "theme_flower_emoji")
                "ic_popular_fire", "fire", "theme_fire" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_fire_space", "theme_fire_special", "theme_fire_emoji")
                "ic_popular_earth", "earth", "theme_earth" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_earth_space", "theme_earth_special", "theme_earth_emoji")
                "ic_popular_butterfly", "butterfly", "theme_butterfly" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_butterfly_space", "theme_butterfly_special", "theme_butterfly_emoji")
                "ic_popular_football", "football", "theme_football" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_football_space", "theme_football_special", "theme_football_emoji")
                "ic_popular_basketball", "basketball", "theme_basketball" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_basketball_space", "theme_basketball_special", "theme_basketball_emoji")
                "ic_popular_gift", "gift", "theme_gift" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_gift_space", "theme_gift_special", "theme_gift_emoji")
                else -> null
            }
        }

        /**
         * Convert a [KeyboardTheme] into a [ResolvedTheme] ready for rendering,
         * taking [KeyboardHeight] into account for height-aware image backgrounds.
         */
        fun from(
            theme: KeyboardTheme,
            height: com.panda.keyboards.theme.KeyboardHeight = com.panda.keyboards.theme.KeyboardHeight.DEFAULT
        ): ResolvedTheme {
            val keyStyle = if (theme.keyStyle is com.panda.keyboards.theme.KeyVisualStyle.Flat && !theme.decorativeIcon.isNullOrEmpty()) {
                resolveThemeKeyBackgrounds(theme.decorativeIcon) ?: theme.keyStyle
            } else {
                theme.keyStyle
            }
            val glassStyle = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.Glassmorphic
            val semiFlatStyle = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.SemiFlat
            val neobrutalistStyle = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.Neobrutalist
            val claymorphicStyle = keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.Claymorphic

            val rawKeyBg = parseColor(theme.keyBackgroundColor)
            val opacityAlpha = theme.keyOpacityAlpha.coerceIn(0f, 1f)
            val keyBg = when {
                theme.keyShape == KeyShape.NONE -> Color.Transparent
                glassStyle != null -> rawKeyBg.copy(alpha = (glassStyle.translucencyAlpha * opacityAlpha).coerceIn(0f, 1f))
                else -> rawKeyBg.copy(alpha = opacityAlpha)
            }
            val keyTxt = parseColor(theme.keyTextColor)
            val accent = parseColor(theme.accentColor)

            val parsedBorderColor = theme.keyBorderColor?.let { parseColor(it) }
            val parsedShadowColor = theme.keyShadowColor?.let { parseColor(it) }

            val finalBorderColor = when {
                glassStyle != null -> parsedBorderColor ?: parseColor(glassStyle.borderColorHex)
                neobrutalistStyle != null -> parsedBorderColor ?: parseColor(neobrutalistStyle.borderColorHex)
                else -> parsedBorderColor
            }

            val finalBorderWidthDp = when {
                glassStyle != null -> if (theme.keyBorderWidthDp > 0f) theme.keyBorderWidthDp else 1f
                neobrutalistStyle != null -> if (theme.keyBorderWidthDp > 0f) theme.keyBorderWidthDp else neobrutalistStyle.borderWidthDp
                else -> theme.keyBorderWidthDp
            }

            val defaultShadowColor = Color.Black.copy(alpha = 0.25f)

            val finalShadowColor = when {
                semiFlatStyle != null -> parsedShadowColor ?: parseColor(semiFlatStyle.shadowColorHex)
                neobrutalistStyle != null -> parsedShadowColor ?: parseColor(neobrutalistStyle.shadowColorHex)
                claymorphicStyle != null -> parsedShadowColor ?: parseColor(claymorphicStyle.shadowColorHex)
                else -> parsedShadowColor ?: defaultShadowColor
            }

            val finalShadowOffsetDp = when {
                semiFlatStyle != null -> if (theme.keyShadowOffsetDp > 0f) theme.keyShadowOffsetDp else semiFlatStyle.elevationDp
                neobrutalistStyle != null -> if (theme.keyShadowOffsetDp > 0f) theme.keyShadowOffsetDp else neobrutalistStyle.shadowOffsetDp
                claymorphicStyle != null -> if (theme.keyShadowOffsetDp > 0f) theme.keyShadowOffsetDp else claymorphicStyle.elevationDp
                else -> if (theme.keyShadowOffsetDp > 0f) theme.keyShadowOffsetDp else 2.5f
            }

            val glassmorphicGlowBrush = if (glassStyle != null && glassStyle.glowGradientColors.isNotEmpty()) {
                val glowColors = glassStyle.glowGradientColors.map { parseColor(it) }
                Brush.radialGradient(colors = glowColors)
            } else {
                null
            }

            // Derive pressed/special variants by lightening/darkening
            val keyBgPressed = keyBg.lighten(0.15f)
            val specialBg = if (glassStyle != null) rawKeyBg.copy(alpha = (glassStyle.translucencyAlpha + 0.15f).coerceAtMost(1f)) else keyBg.lighten(0.10f)
            val specialBgPressed = specialBg.lighten(0.15f)
            val accentLight = accent.lighten(0.20f)

            // Resolve keyboard background
            val (bgColor, bgBrush) = resolveBackground(theme.keyboardBackground)
            val imagePath = (theme.keyboardBackground as? ThemeBackground.Image)?.getAssetPathForHeight(height)

            // Resolve key shape
            val shape = when (theme.keyShape) {
                KeyShape.NONE -> RoundedCornerShape(0.dp)
                KeyShape.SQUARE -> RoundedCornerShape(2.dp)
                KeyShape.SQUARE_ROUNDED -> RoundedCornerShape(8.dp)
                KeyShape.OVAL -> RoundedCornerShape(24.dp)
                KeyShape.OVAL_ROUNDED -> RoundedCornerShape(16.dp)
            }

            return ResolvedTheme(
                keyBackground = keyBg,
                keyBackgroundPressed = keyBgPressed,
                specialKeyBackground = specialBg,
                specialKeyBackgroundPressed = specialBgPressed,
                keyText = keyTxt,
                specialKeyText = keyTxt,
                accent = accent,
                accentLight = accentLight,
                keyboardBackground = bgColor,
                keyboardBackgroundBrush = bgBrush,
                imagePath = imagePath,
                keyShape = shape,
                keyBorderColor = finalBorderColor,
                keyBorderWidthDp = finalBorderWidthDp,
                keyShadowColor = finalShadowColor,
                keyShadowOffsetDp = finalShadowOffsetDp,
                keyStyle = keyStyle,
                glassmorphicGlowBrush = glassmorphicGlowBrush,
                keyOpacityAlpha = opacityAlpha,
                decorativeIcon = theme.decorativeIcon,
                fontSizeSp = theme.fontSizeSp,
                fontStyleName = theme.fontStyleName,
                hasTextShadow = theme.hasTextShadow,
                isCustom = theme.isCustom
            )
        }

        /**
         * Parse a hex color string (e.g. "#FF3A3A3C" or "#3A3A3C") into Compose [Color].
         */
        internal fun parseColor(hex: String): Color {
            val cleaned = hex.removePrefix("#")
            val colorLong = when (cleaned.length) {
                6 -> "FF$cleaned".toLong(16)      // RGB → ARGB
                8 -> cleaned.toLong(16)             // ARGB as-is
                3 -> {                              // Shorthand RGB
                    val r = cleaned[0]
                    val g = cleaned[1]
                    val b = cleaned[2]
                    "FF$r$r$g$g$b$b".toLong(16)
                }
                else -> 0xFF000000L
            }
            return Color(colorLong)
        }

        /**
         * Resolve a [ThemeBackground] into a solid color and optional gradient brush.
         */
        private fun resolveBackground(bg: ThemeBackground): Pair<Color, Brush?> {
            return when (bg) {
                is ThemeBackground.SolidColor -> parseColor(bg.color) to null

                is ThemeBackground.Gradient -> {
                    val colors = bg.colors.map { parseColor(it) }
                    if (colors.size < 2) {
                        // Fallback to solid if only one color
                        (colors.firstOrNull() ?: Color.Black) to null
                    } else {
                        val angleRad = Math.toRadians(bg.angle.toDouble())
                        val brush = Brush.linearGradient(
                            colors = colors,
                            // Simple directional gradient based on angle
                            start = androidx.compose.ui.geometry.Offset(
                                x = (0.5f - 0.5f * cos(angleRad).toFloat()) * 1000f,
                                y = (0.5f - 0.5f * sin(angleRad).toFloat()) * 1000f
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                x = (0.5f + 0.5f * cos(angleRad).toFloat()) * 1000f,
                                y = (0.5f + 0.5f * sin(angleRad).toFloat()) * 1000f
                            )
                        )
                        colors.first() to brush
                    }
                }

                is ThemeBackground.Image -> {
                    // Image backgrounds will be rendered via a separate Image composable
                    // in PandaKeyboardLayout. For now, provide a fallback color.
                    Color.Black to null
                }
            }
        }

        /**
         * Lighten a color by mixing it with white.
         * @param factor 0f = no change, 1f = fully white.
         */
        private fun Color.lighten(factor: Float): Color {
            return Color(
                red = red + (1f - red) * factor,
                green = green + (1f - green) * factor,
                blue = blue + (1f - blue) * factor,
                alpha = alpha
            )
        }
    }
}
