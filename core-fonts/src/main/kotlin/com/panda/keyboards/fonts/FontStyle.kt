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
    BOLD_SERIF("bold_serif", "Bold Serif"),
    BOLD_SANS("bold_sans", "Bold Sans"),
    ITALIC_SANS("italic_sans", "Italic Sans"),
    GOTHIC("gothic", "Gothic"),
    BUBBLE("bubble", "Bubble"),
    CIRCLED("circled", "Circled"),
    SMALL_CAPS("small_caps", "Small Caps"),
    STRIKETHROUGH("strikethrough", "Strikethrough"),
    SCRIPT("script", "Script"),
    MONOSPACE("monospace", "Monospace"),
    FULLWIDTH("fullwidth", "Fullwidth"),
    SUPERSCRIPT("superscript", "Superscript"),
    SANS_SERIF("sans_serif", "Sans Serif"),
    ITALIC_SERIF("italic_serif", "Italic Serif"),
    BOLD_ITALIC_SANS("bold_italic_sans", "Bold Italic Sans"),
    BOLD_ITALIC_SERIF("bold_italic_serif", "Bold Italic Serif"),
    DOUBLE_STRUCK("double_struck", "Double Struck"),
    BOLD_SCRIPT("bold_script", "Bold Script"),
    BOLD_GOTHIC("bold_gothic", "Bold Gothic"),
    PARENTHESIZED("parenthesized", "Parenthesized"),
    SQUARED("squared", "Squared"),
    NEGATIVE_SQUARED("negative_squared", "Negative Squared"),
    REGIONAL_INDICATOR("regional_indicator", "Regional Indicator"),
    UPSIDE_DOWN("upside_down", "Upside Down"),

    // ── NEW REQUESTED FONTS ────────────────────────────────────────────────
    CALIBRI("calibri", "Calibri (Body)"),
    ALGERIAN("algerian", "Algerian"),
    ARIAL_BLACK("arial_black", "Arial Black"),
    BAHNSCHRIFT("bahnschrift", "Bahnschrift Light SemiCondensed"),
    BRUSH_SCRIPT("brush_script", "Brush Script MT"),
    CHILLER("chiller", "Chiller"),
    BRADLEY_HAND("bradley_hand", "Bradley Hand ITC"),
    BODONI_MT("bodoni_mt", "Bodoni MT Poster Compressed"),
    BOOKMAN_OLD_STYLE("bookman_old_style", "Bookman Old Style"),
    CASCADIA_CODE("cascadia_code", "Cascadia Code"),
    EDWARDIAN_SCRIPT("edwardian_script", "Edwardian Script ITC"),
    COPPERPLATE_GOTHIC("copperplate_gothic", "Copperplate Gothic Bold"),
    COMIC_SANS("comic_sans", "Comic Sans MS"),
    ELEPHANT("elephant", "Elephant"),
    JOKERMAN("jokerman", "Jokerman"),
    FRENCH_SCRIPT("french_script", "French Script MT"),
    FREESTYLE_SCRIPT("freestyle_script", "Freestyle Script"),
    TIMES_NEW_ROMAN("times_new_roman", "Times New Roman"),
    SCRIPT_MT_BOLD("script_mt_bold", "Script MT Bold"),
    POOR_RICHARD("poor_richard", "Poor Richard"),
    OLD_ENGLISH("old_english", "Old English Text MT");

    companion object {
        /** Look up a [FontStyle] by its stable [id], or null if not found. */
        fun fromId(id: String): FontStyle? = entries.find { it.id == id }
    }
}
