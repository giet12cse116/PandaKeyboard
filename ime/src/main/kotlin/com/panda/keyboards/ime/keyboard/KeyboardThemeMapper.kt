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

    fun resolveTheme(theme: KeyboardTheme?): ResolvedTheme {
        return if (theme != null) ResolvedTheme.from(theme) else ResolvedTheme.DEFAULT
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
    val keyShape: Shape
) {
    companion object {
        /** Default dark theme — matches the Sprint 2 hardcoded appearance. */
        val DEFAULT = from(
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
         * Convert a [KeyboardTheme] into a [ResolvedTheme] ready for rendering.
         */
        fun from(theme: KeyboardTheme): ResolvedTheme {
            val keyBg = parseColor(theme.keyBackgroundColor)
            val keyTxt = parseColor(theme.keyTextColor)
            val accent = parseColor(theme.accentColor)

            // Derive pressed/special variants by lightening/darkening
            val keyBgPressed = keyBg.lighten(0.15f)
            val specialBg = keyBg.lighten(0.10f)
            val specialBgPressed = keyBg.lighten(0.25f)
            val accentLight = accent.lighten(0.20f)

            // Resolve keyboard background
            val (bgColor, bgBrush) = resolveBackground(theme.keyboardBackground)
            val imagePath = (theme.keyboardBackground as? ThemeBackground.Image)?.assetPath

            // Resolve key shape — square rounded corner background across all keyboard themes
            val shape = when (theme.keyShape) {
                KeyShape.ROUNDED -> RoundedCornerShape(8.dp)
                KeyShape.SQUARE -> RoundedCornerShape(8.dp)
                KeyShape.PILL -> RoundedCornerShape(8.dp)
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
                keyShape = shape
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
