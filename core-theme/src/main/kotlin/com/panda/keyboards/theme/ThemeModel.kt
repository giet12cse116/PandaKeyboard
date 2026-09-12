package com.panda.keyboards.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shape style for keyboard keys.
 */
@Serializable
enum class KeyShape {
    @SerialName("none")
    NONE,

    @SerialName("square")
    SQUARE,

    @SerialName("rounded")
    SQUARE_ROUNDED,

    @SerialName("pill")
    OVAL,

    @SerialName("oval_rounded")
    OVAL_ROUNDED;

    companion object {
        val ROUNDED: KeyShape get() = SQUARE_ROUNDED
        val PILL: KeyShape get() = OVAL
    }
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

    /**
     * Production Asset Skin pipeline utilizing custom bitmap/drawable key templates.
     *
     * @property letterTemplateRes Drawable resource name for shared letter/digit key background plaque.
     * @property functionTemplateRes Drawable resource name for function key background template (backspace).
     * @property functionTemplateBlankRes Drawable resource name for blank function key background (enter, shift, ?123).
     * @property spacebarRes Drawable resource name for ornate spacebar plaque.
     * @property settingsIconRes Drawable resource name for top toolbar settings icon.
     * @property embossDarkColorHex Hex color string for dark offset shadow on carved wood text.
     * @property embossLightColorHex Hex color string for light offset highlight on carved wood text.
     * @property brassTextColorHex Hex color string for metallic brass overlay text/icons.
     */
    @Serializable
    @SerialName("asset_skin")
    data class AssetSkin(
        val letterTemplateRes: String = "skin_wood_brass_letter_template",
        val functionTemplateRes: String = "skin_wood_brass_function_template",
        val functionTemplateBlankRes: String = "skin_wood_brass_function_template_blank",
        val spacebarRes: String = "skin_wood_brass_spacebar",
        val settingsIconRes: String = "skin_wood_brass_icon_settings",
        val embossDarkColorHex: String = "#4A2B10",
        val embossLightColorHex: String = "#FFF8E1",
        val brassTextColorHex: String = "#D4AF37"
    ) : KeyVisualStyle()

    /**
     * Low-Poly Origami / Faceted Paper-Craft 3D visual style.
     *
     * @property mintKeyRes Drawable resource name for Mint Green standard faceted key.
     * @property purpleKeyRes Drawable resource name for Lavender Purple standard faceted key.
     * @property peachKeyRes Drawable resource name for Peach standard faceted key.
     * @property blueKeyRes Drawable resource name for Sky Blue standard faceted key.
     * @property shiftKeyRes Drawable resource name for Shift faceted pyramid key.
     * @property backspaceKeyRes Drawable resource name for Backspace faceted slice key.
     * @property enterKeyRes Drawable resource name for Enter origami envelope key.
     * @property spacebarRes Drawable resource name for Spacebar wide faceted key.
     * @property emojiKeyRes Drawable resource name for Emoji purple faceted key.
     * @property symbolKeyRes Drawable resource name for Symbol peach faceted key.
     * @property topToolbarRes Drawable resource name for top toolbar with origami tabs.
     * @property labelColorHex Hex color string for labels flush on flat top facet (#4A3B6B plum/navy).
     */
    @Serializable
    @SerialName("origami_paper_craft")
    data class OrigamiPaperCraft(
        val mintKeyRes: String = "standard_origami_mint",
        val purpleKeyRes: String = "standard_origami_purple",
        val peachKeyRes: String = "standard_origami_peach",
        val blueKeyRes: String = "standard_origami_blue",
        val shiftKeyRes: String = "special_origami_shift_peach",
        val backspaceKeyRes: String = "special_origami_backspace_pink",
        val enterKeyRes: String = "special_origami_enter_blue",
        val spacebarRes: String = "special_origami_spacebar_mint",
        val emojiKeyRes: String = "special_origami_emoji_purple",
        val symbolKeyRes: String = "special_origami_symbol_peach",
        val topToolbarRes: String = "origami_top_toolbar",
        val labelColorHex: String = "#4A3B6B"
    ) : KeyVisualStyle()

    /**
     * Slice Keyboards Pizza 3D visual style.
     */
    @Serializable
    @SerialName("pizza_slice")
    data class PizzaSlice(
        val standardKeyRes: String = "pizza_kb_u",
        val bKeyRes: String = "pizza_kb_b",
        val qKeyRes: String = "pizza_kb_q",
        val wKeyRes: String = "pizza_kb_w",
        val pKeyRes: String = "pizza_kb_p",
        val uKeyRes: String = "pizza_kb_u",
        val commaKeyRes: String = "pizza_kb_comma",
        val enterKeyRes: String = "pizza_kb_enter",
        val fullstopKeyRes: String = "pizza_kb_fullstop",
        val numberpadKeyRes: String = "pizza_kb_numberpad",
        val smileyKeyRes: String = "pizza_kb_smieley",
        val spacebarRes: String = "pizza_kb_space",
        val saucepanIconRes: String = "pizza_kb_ic_pizza_saucepan",
        val pizzaPeelIconRes: String = "pizza_kb_ic_pizza_peel",
        val cutterGearIconRes: String = "pizza_kb_ic_pizza_cutter_gear",
        val cheeseShakerIconRes: String = "ic_pizza_cheese_shaker",
        val stickersStIconRes: String = "pizza_kb_ic_pizza_stickers_st",
        val emojiPizzaIconRes: String = "pizza_kb_ic_pizza_emoji_slice",
        val sauceTextColorHex: String = "#931B0D",
        val gorgonzolaTextColorHex: String = "#2F1E14",
        val enterTextColorHex: String = "#FFFFFF"
    ) : KeyVisualStyle()

    /**
     * Steampunk / Industrial 3D visual style.
     */
    @Serializable
    @SerialName("steampunk_industrial")
    data class SteampunkIndustrial(
        val enamelKeyRes: String = "key_base_enamel",
        val leatherSquareRes: String = "key_base_leather_square",
        val leatherWideRes: String = "key_base_leather_wide",
        val nixieTubeRes: String = "base_nixie_tube",
        val spacebarNixieRes: String = "spacebar_nixie_tube",
        val clockworkGearsRes: String = "bg_clockwork_gears",
        val dialThemeRes: String = "ic_dial_theme",
        val dialClipRes: String = "ic_dial_clipboard",
        val dialGearRes: String = "ic_dial_settings",
        val dialMicRes: String = "ic_dial_mic",
        val dialFontRes: String = "ic_dial_font",
        val enamelTextColorHex: String = "#1B120C",
        val leatherTextColorHex: String = "#CFA679",
        val nixieGlowColorHex: String = "#FF9326"
    ) : KeyVisualStyle()

    /**
     * Synthwave Cyberpunk Neon visual style.
     */
    @Serializable
    @SerialName("synthwave_cyberpunk_neon")
    data class SynthwaveCyberpunkNeon(
        val cyanKeyBgColorHex: String = "#0C081D",
        val cyanKeyBorderColorHex: String = "#00E5FF",
        val cyanKeyGlowColorHex: String = "#00E5FF",
        val cyanTextColorHex: String = "#FFFFFF",
        val magentaKeyBgColorHex: String = "#14081E",
        val magentaKeyBorderColorHex: String = "#FF2A85",
        val magentaKeyGlowColorHex: String = "#FF2A85",
        val magentaTextColorHex: String = "#FF2A85",
        val topRibbonBgColorHex: String = "#170E36",
        val skylineBgRes: String = "bg_synthwave_skyline",
        val speedometerIconRes: String = "ic_nav_speedometer",

        val clipboardIconRes: String = "ic_nav_clipboard",
        val settingsIconRes: String = "ic_nav_settings",
        val micIconRes: String = "ic_nav_mic",
        val fontTtIconRes: String = "ic_nav_font_tt",
        val shiftArrowIconRes: String = "ic_shift_arrow",
        val backspaceTagIconRes: String = "ic_backspace_tag",
        val enterArrowIconRes: String = "ic_enter_arrow",
        val emojiSmileIconRes: String = "ic_emoji_smile"
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
        val assetPath: String
    ) : ThemeBackground() {
        /**
         * Resolve the asset path.
         */
        fun getAssetPathForHeight(height: KeyboardHeight): String {
            return assetPath
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
    val keyShape: KeyShape = KeyShape.SQUARE_ROUNDED,
    val keyOpacityAlpha: Float = 1.0f,
    val accentColor: String,
    val keyBorderColor: String? = null,
    val keyBorderWidthDp: Float = 0f,
    val keyShadowColor: String? = null,
    val keyShadowOffsetDp: Float = 0f,
    val keyStyle: KeyVisualStyle = KeyVisualStyle.Flat,
    val decorativeIcon: String? = null,
    val fontSizeSp: Int = 16,
    val fontStyleName: String = "rounded",
    val hasTextShadow: Boolean = false
) {

    companion object {
        /** The default theme ID — must match a theme in themes.json. */
        const val DEFAULT_THEME_ID = "default_dark"

        /** Default fallback KeyboardTheme instance. */
        val DEFAULT = KeyboardTheme(
            id = DEFAULT_THEME_ID,
            name = "Default Dark",
            keyBackgroundColor = "#3A3A3C",
            keyTextColor = "#FFFFFF",
            keyboardBackground = ThemeBackground.SolidColor("#1C1C1E"),
            keyShape = KeyShape.SQUARE_ROUNDED,
            accentColor = "#7C4DFF"
        )
    }
}

/**
 * Wrapper for the themes JSON file structure.
 */
@Serializable
data class ThemeCatalog(
    val themes: List<KeyboardTheme>
)
