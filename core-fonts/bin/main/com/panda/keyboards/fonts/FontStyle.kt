package com.panda.keyboards.fonts

/**
 * All supported Unicode font styles.
 *
 * Each style maps to a specific Unicode block or transformation strategy
 * used by [FontTransformer] to convert plain ASCII text into stylized
 * Unicode characters.
 *
 * @property id Stable string identifier, used for persistence (favorites).
 * @property displayName Human-readable label for UI display.
 */
enum class FontStyle(val id: String, val displayName: String) {
    NORMAL("normal", "Normal"),
    ALGERIAN("algerian", "Algerian"),
    ARIAL_BLACK("arial_black", "Arial Black"),
    BAHNSCHRIFT("bahnschrift", "Bahnschrift Light SemiCondensed"),
    BODONI_MT("bodoni_mt", "Bodoni MT Poster Compressed"),
    BOLD_GOTHIC("bold_gothic", "Bold Gothic"),
    BOLD_ITALIC_SANS("bold_italic_sans", "Bold Italic Sans"),
    BOLD_ITALIC_SERIF("bold_italic_serif", "Bold Italic Serif"),
    BOLD_SANS("bold_sans", "Bold Sans"),
    BOLD_SCRIPT("bold_script", "Bold Script"),
    BOLD_SERIF("bold_serif", "Bold Serif"),
    BOOKMAN_OLD_STYLE("bookman_old_style", "Bookman Old Style"),
    BRADLEY_HAND("bradley_hand", "Bradley Hand ITC"),
    BRUSH_SCRIPT("brush_script", "Brush Script MT"),
    BUBBLE("bubble", "Bubble"),
    CALIBRI("calibri", "Calibri (Body)"),
    CASCADIA_CODE("cascadia_code", "Cascadia Code"),
    CHILLER("chiller", "Chiller"),
    CIRCLED("circled", "Circled"),
    COMIC_SANS("comic_sans", "Comic Sans MS"),
    COPPERPLATE_GOTHIC("copperplate_gothic", "Copperplate Gothic Bold"),
    DOUBLE_STRUCK("double_struck", "Double Struck"),
    EDWARDIAN_SCRIPT("edwardian_script", "Edwardian Script ITC"),
    ELEPHANT("elephant", "Elephant"),
    FREESTYLE_SCRIPT("freestyle_script", "Freestyle Script"),
    FRENCH_SCRIPT("french_script", "French Script MT"),
    FULLWIDTH("fullwidth", "Fullwidth"),
    GOTHIC("gothic", "Gothic"),
    ITALIC_SANS("italic_sans", "Italic Sans"),
    ITALIC_SERIF("italic_serif", "Italic Serif"),
    MONOSPACE("monospace", "Monospace"),
    NEGATIVE_SQUARED("negative_squared", "Negative Squared"),
    OLD_ENGLISH("old_english", "Old English Text MT"),
    PARENTHESIZED("parenthesized", "Parenthesized"),
    POOR_RICHARD("poor_richard", "Poor Richard"),
    REGIONAL_INDICATOR("regional_indicator", "Regional Indicator"),
    SANS_SERIF("sans_serif", "Sans Serif"),
    SCRIPT("script", "Script"),
    SCRIPT_MT_BOLD("script_mt_bold", "Script MT Bold"),
    SMALL_CAPS("small_caps", "Small Caps"),
    SQUARED("squared", "Squared"),
    STRIKETHROUGH("strikethrough", "Strikethrough"),
    SUPERSCRIPT("superscript", "Superscript"),
    TIMES_NEW_ROMAN("times_new_roman", "Times New Roman"),
    UPSIDE_DOWN("upside_down", "Upside Down");

    companion object {
        /** Look up a [FontStyle] by its stable [id], or null if not found. */
        fun fromId(id: String): FontStyle? = entries.find { it.id == id }
    }
}
