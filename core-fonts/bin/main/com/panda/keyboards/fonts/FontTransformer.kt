package com.panda.keyboards.fonts

/**
 * Pure-function Unicode font transformer.
 *
 * Maps plain ASCII characters (A-Z, a-z, 0-9) to their stylized Unicode
 * equivalents for each [FontStyle]. Uses offset-based math against Unicode
 * blocks (primarily Mathematical Alphanumeric Symbols U+1D400–U+1D7FF)
 * rather than hardcoding every character.
 *
 * ## Mapping Strategy
 *
 * Most styles use a simple offset formula:
 * ```
 * styledCodepoint = unicodeBlockBase + (asciiChar - 'A')   // for uppercase
 * styledCodepoint = unicodeBlockBase + (asciiChar - 'a')   // for lowercase
 * ```
 *
 * Some styles (STRIKETHROUGH, SMALL_CAPS, SUPERSCRIPT, BUBBLE/CIRCLED)
 * use combining characters or lookup tables because the Unicode codepoints
 * are not contiguous.
 *
 * Characters without a mapping (punctuation, emoji, CJK, etc.) pass
 * through unchanged.
 */
object FontTransformer {

    /**
     * Transform [input] text into the given Unicode font [style].
     *
     * @param input Plain text to transform.
     * @param style The target [FontStyle].
     * @return The transformed string, with unmappable chars preserved as-is.
     */
    fun transform(input: String, style: FontStyle): String {
        if (style == FontStyle.NORMAL) return input
        if (style == FontStyle.STRIKETHROUGH) return applyStrikethrough(input)
        if (style == FontStyle.CHILLER) return applyChiller(input)
        if (style == FontStyle.UPSIDE_DOWN) return applyUpsideDown(input)

        val sb = StringBuilder(input.length * 2) // surrogate pairs may double length
        for (char in input) {
            sb.append(mapChar(char, style))
        }
        return sb.toString()
    }

    // ── Offset-based styles ─────────────────────────────────────────────

    /**
     * Map a single character for the given style.
     * Returns original char as a String if no mapping exists.
     */
    private fun mapChar(char: Char, style: FontStyle): String {
        return when (style) {
            FontStyle.BOLD_SERIF        -> mapOffset(char, 0x1D400, 0x1D41A, 0x1D7CE)
            FontStyle.BOLD_SANS         -> mapOffset(char, 0x1D5D4, 0x1D5EE, 0x1D7EC)
            FontStyle.ITALIC_SANS       -> mapOffset(char, 0x1D608, 0x1D622, null)
            FontStyle.SANS_SERIF        -> mapOffset(char, 0x1D5A0, 0x1D5BA, 0x1D7E2)
            FontStyle.ITALIC_SERIF      -> mapItalicSerif(char)
            FontStyle.BOLD_ITALIC_SANS  -> mapOffset(char, 0x1D63C, 0x1D656, null)
            FontStyle.BOLD_ITALIC_SERIF -> mapOffset(char, 0x1D468, 0x1D482, null)
            FontStyle.DOUBLE_STRUCK     -> mapDoubleStruck(char)
            FontStyle.BOLD_SCRIPT       -> mapOffset(char, 0x1D4D0, 0x1D4EA, null)
            FontStyle.BOLD_GOTHIC       -> mapOffset(char, 0x1D56C, 0x1D586, null)
            FontStyle.GOTHIC            -> mapGothic(char)
            FontStyle.SCRIPT            -> mapScript(char)
            FontStyle.MONOSPACE         -> mapOffset(char, 0x1D670, 0x1D68A, 0x1D7F6)
            FontStyle.FULLWIDTH         -> mapFullwidth(char)
            FontStyle.BUBBLE            -> mapBubble(char)
            FontStyle.CIRCLED           -> mapCircled(char)
            FontStyle.SMALL_CAPS        -> mapSmallCaps(char)
            FontStyle.SUPERSCRIPT       -> mapSuperscript(char)
            FontStyle.PARENTHESIZED     -> mapParenthesized(char)
            FontStyle.SQUARED           -> mapSquared(char)
            FontStyle.NEGATIVE_SQUARED  -> mapNegativeSquared(char)
            FontStyle.REGIONAL_INDICATOR-> mapRegionalIndicator(char)
            FontStyle.CALIBRI            -> mapOffset(char, 0x1D5A0, 0x1D5BA, 0x1D7E2)
            FontStyle.ALGERIAN           -> mapOffset(char, 0x1D400, 0x1D400, 0x1D7CE)
            FontStyle.ARIAL_BLACK        -> mapOffset(char, 0x1D5D4, 0x1D5EE, 0x1D7EC)
            FontStyle.BAHNSCHRIFT        -> mapOffset(char, 0x1D5A0, 0x1D5BA, null)
            FontStyle.BRUSH_SCRIPT       -> mapScript(char)
            FontStyle.CHILLER            -> mapChiller(char)
            FontStyle.BRADLEY_HAND       -> mapScript(char)
            FontStyle.BODONI_MT          -> mapOffset(char, 0x1D400, 0x1D41A, 0x1D7CE)
            FontStyle.BOOKMAN_OLD_STYLE  -> mapOffset(char, 0x1D400, 0x1D41A, 0x1D7CE)
            FontStyle.CASCADIA_CODE      -> mapOffset(char, 0x1D670, 0x1D68A, 0x1D7F6)
            FontStyle.EDWARDIAN_SCRIPT   -> mapScript(char)
            FontStyle.COPPERPLATE_GOTHIC -> mapSmallCaps(char)
            FontStyle.COMIC_SANS         -> mapOffset(char, 0x1D5A0, 0x1D5BA, 0x1D7E2)
            FontStyle.ELEPHANT           -> mapOffset(char, 0x1D400, 0x1D41A, 0x1D7CE)
            FontStyle.FRENCH_SCRIPT      -> mapScript(char)
            FontStyle.FREESTYLE_SCRIPT   -> mapScript(char)
            FontStyle.TIMES_NEW_ROMAN    -> mapOffset(char, 0x1D400, 0x1D41A, 0x1D7CE)
            FontStyle.SCRIPT_MT_BOLD     -> mapOffset(char, 0x1D4D0, 0x1D4EA, null)
            FontStyle.POOR_RICHARD       -> mapGothic(char)
            FontStyle.OLD_ENGLISH        -> mapGothic(char)
            else -> char.toString() // NORMAL, STRIKETHROUGH, UPSIDE_DOWN handled above
        }
    }

    /**
     * Generic offset mapper for Mathematical Alphanumeric Symbols block.
     *
     * @param upperBase Codepoint of styled 'A'
     * @param lowerBase Codepoint of styled 'a'
     * @param digitBase Codepoint of styled '0', or null if digits pass through
     */
    private fun mapOffset(
        char: Char,
        upperBase: Int,
        lowerBase: Int,
        digitBase: Int?
    ): String = when {
        char in 'A'..'Z' -> codePointToString(upperBase + (char - 'A'))
        char in 'a'..'z' -> codePointToString(lowerBase + (char - 'a'))
        digitBase != null && char in '0'..'9' -> codePointToString(digitBase + (char - '0'))
        else -> char.toString()
    }

    // ── Gothic / Fraktur ────────────────────────────────────────────────
    // Mathematical Fraktur: U+1D504–U+1D537, but several letters have
    // irregular codepoints (C, H, I, R, Z for uppercase).

    private val gothicUpperExceptions = mapOf(
        'C' to 0x212D,  // ℭ
        'H' to 0x210C,  // ℌ
        'I' to 0x2111,  // ℑ
        'R' to 0x211C,  // ℜ
        'Z' to 0x2128   // ℨ
    )

    private fun mapGothic(char: Char): String = when {
        char in 'A'..'Z' -> {
            val exception = gothicUpperExceptions[char]
            if (exception != null) codePointToString(exception)
            else codePointToString(0x1D504 + (char - 'A'))
        }
        char in 'a'..'z' -> codePointToString(0x1D51E + (char - 'a'))
        else -> char.toString()
    }

    // ── Script / Cursive ────────────────────────────────────────────────
    // Mathematical Script: U+1D49C–U+1D4CF, with exceptions for
    // B, E, F, H, I, L, M, R, e, g, o.

    private val scriptUpperExceptions = mapOf(
        'B' to 0x212C,  // ℬ
        'E' to 0x2130,  // ℰ
        'F' to 0x2131,  // ℱ
        'H' to 0x210B,  // ℋ
        'I' to 0x2110,  // ℐ
        'L' to 0x2112,  // ℒ
        'M' to 0x2133,  // ℳ
        'R' to 0x211B   // ℛ
    )

    private val scriptLowerExceptions = mapOf(
        'e' to 0x212F,  // ℯ
        'g' to 0x210A,  // ℊ
        'o' to 0x2134   // ℴ
    )

    private fun mapScript(char: Char): String = when {
        char in 'A'..'Z' -> {
            val exception = scriptUpperExceptions[char]
            if (exception != null) codePointToString(exception)
            else codePointToString(0x1D49C + (char - 'A'))
        }
        char in 'a'..'z' -> {
            val exception = scriptLowerExceptions[char]
            if (exception != null) codePointToString(exception)
            else codePointToString(0x1D4B6 + (char - 'a'))
        }
        else -> char.toString()
    }

    // ── Fullwidth ───────────────────────────────────────────────────────
    // Fullwidth Latin: U+FF01–U+FF5E maps ASCII U+0021–U+007E

    private fun mapFullwidth(char: Char): String {
        val code = char.code
        return if (code in 0x21..0x7E) {
            codePointToString(0xFF00 + (code - 0x20))
        } else {
            char.toString()
        }
    }

    // ── Bubble (Enclosed Alphanumerics) ─────────────────────────────────
    // Uppercase: Ⓐ U+24B6..U+24CF
    // Lowercase: ⓐ U+24D0..U+24E9
    // Digits: 0→⓪ U+24EA, 1→① U+2460..U+2468

    private fun mapBubble(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x24B6 + (char - 'A'))
        char in 'a'..'z' -> codePointToString(0x24D0 + (char - 'a'))
        char == '0'       -> codePointToString(0x24EA) // ⓪
        char in '1'..'9' -> codePointToString(0x2460 + (char - '1'))
        else -> char.toString()
    }

    // ── Circled (Negative Circled Latin) ────────────────────────────────
    // Uppercase: 🅐 U+1F150..U+1F169
    // Lowercase: falls back to regular circled (same as Bubble lowercase)
    // Digits: 0→⓿ U+24FF, 1→❶ U+2776..U+277E

    private fun mapCircled(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x1F150 + (char - 'A'))
        char in 'a'..'z' -> codePointToString(0x24D0 + (char - 'a')) // fallback to enclosed
        char == '0'       -> codePointToString(0x24FF) // ⓿
        char in '1'..'9' -> codePointToString(0x2776 + (char - '1'))
        else -> char.toString()
    }

    // ── Small Caps ──────────────────────────────────────────────────────
    // No contiguous Unicode block — must use a lookup table.
    // Only lowercase letters are replaced; uppercase stays as-is.

    private val smallCapsMap = mapOf(
        'a' to 'ᴀ', 'b' to 'ʙ', 'c' to 'ᴄ', 'd' to 'ᴅ',
        'e' to 'ᴇ', 'f' to 'ꜰ', 'g' to 'ɢ', 'h' to 'ʜ',
        'i' to 'ɪ', 'j' to 'ᴊ', 'k' to 'ᴋ', 'l' to 'ʟ',
        'm' to 'ᴍ', 'n' to 'ɴ', 'o' to 'ᴏ', 'p' to 'ᴘ',
        'q' to 'ǫ', 'r' to 'ʀ', 's' to 'ꜱ', 't' to 'ᴛ',
        'u' to 'ᴜ', 'v' to 'ᴠ', 'w' to 'ᴡ', 'x' to 'x', // no small cap x
        'y' to 'ʏ', 'z' to 'ᴢ'
    )

    private fun mapSmallCaps(char: Char): String {
        return (smallCapsMap[char] ?: char).toString()
    }

    // ── Superscript ─────────────────────────────────────────────────────
    // Limited Unicode superscript set — lookup table approach.

    private val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³',
        '4' to '⁴', '5' to '⁵', '6' to '⁶', '7' to '⁷',
        '8' to '⁸', '9' to '⁹',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ',
        'e' to 'ᵉ', 'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ',
        'i' to 'ⁱ', 'j' to 'ʲ', 'k' to 'ᵏ', 'l' to 'ˡ',
        'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ', 'p' to 'ᵖ',
        'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
        'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ',
        'z' to 'ᶻ',
        'A' to 'ᴬ', 'B' to 'ᴮ', 'D' to 'ᴰ', 'E' to 'ᴱ',
        'G' to 'ᴳ', 'H' to 'ᴴ', 'I' to 'ᴵ', 'J' to 'ᴶ',
        'K' to 'ᴷ', 'L' to 'ᴸ', 'M' to 'ᴹ', 'N' to 'ᴺ',
        'O' to 'ᴼ', 'P' to 'ᴾ', 'R' to 'ᴿ', 'T' to 'ᵀ',
        'U' to 'ᵁ', 'V' to 'ⱽ', 'W' to 'ᵂ',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾'
    )

    private fun mapSuperscript(char: Char): String {
        return (superscriptMap[char] ?: char).toString()
    }

    // ── Strikethrough ───────────────────────────────────────────────────
    // Appends U+0336 (COMBINING LONG STROKE OVERLAY) after each character.

    private fun applyStrikethrough(input: String): String {
        val combining = "\u0336"
        val sb = StringBuilder(input.length * 2)
        var i = 0
        while (i < input.length) {
            val codePoint = input.codePointAt(i)
            val chars = Character.toChars(codePoint)
            sb.append(chars)
            if (codePoint != ' '.code) { // don't strikethrough spaces
                sb.append(combining)
            }
            i += Character.charCount(codePoint)
        }
        return sb.toString()
    }

    // ── Chiller ─────────────────────────────────────────────────────────
    // Appends U+0338 (COMBINING LONG SOLIDUS OVERLAY) after each character.

    private fun mapChiller(char: Char): String {
        return if (char != ' ') char + "\u0338" else char.toString()
    }

    private fun applyChiller(input: String): String {
        val combining = "\u0338"
        val sb = StringBuilder(input.length * 2)
        var i = 0
        while (i < input.length) {
            val codePoint = input.codePointAt(i)
            val chars = Character.toChars(codePoint)
            sb.append(chars)
            if (codePoint != ' '.code) {
                sb.append(combining)
            }
            i += Character.charCount(codePoint)
        }
        return sb.toString()
    }

    // ── Italic Serif ────────────────────────────────────────────────────
    private fun mapItalicSerif(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x1D434 + (char - 'A'))
        char == 'h'      -> codePointToString(0x210E) // ℎ Planck constant
        char in 'a'..'z' -> codePointToString(0x1D44E + (char - 'a'))
        else -> char.toString()
    }

    // ── Double Struck (Blackboard Bold) ─────────────────────────────────
    private val doubleStruckUpperExceptions = mapOf(
        'C' to 0x2102, // ℂ
        'H' to 0x210D, // ℍ
        'N' to 0x2115, // ℕ
        'P' to 0x2119, // ℙ
        'Q' to 0x211A, // ℚ
        'R' to 0x211D, // ℝ
        'Z' to 0x2124  // ℤ
    )

    private fun mapDoubleStruck(char: Char): String = when {
        char in 'A'..'Z' -> {
            val exception = doubleStruckUpperExceptions[char]
            if (exception != null) codePointToString(exception)
            else codePointToString(0x1D538 + (char - 'A'))
        }
        char in 'a'..'z' -> codePointToString(0x1D552 + (char - 'a'))
        char in '0'..'9' -> codePointToString(0x1D7D8 + (char - '0'))
        else -> char.toString()
    }

    // ── Parenthesized ───────────────────────────────────────────────────
    private fun mapParenthesized(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x1F110 + (char - 'A'))
        char in 'a'..'z' -> codePointToString(0x249C + (char - 'a'))
        char in '1'..'9' -> codePointToString(0x2474 + (char - '1'))
        else -> char.toString()
    }

    // ── Squared ─────────────────────────────────────────────────────────
    private fun mapSquared(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x1F130 + (char - 'A'))
        char in 'a'..'z' -> codePointToString(0x1F130 + (char - 'a'))
        else -> char.toString()
    }

    // ── Negative Squared ────────────────────────────────────────────────
    private fun mapNegativeSquared(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x1F170 + (char - 'A'))
        char in 'a'..'z' -> codePointToString(0x1F170 + (char - 'a'))
        else -> char.toString()
    }

    // ── Regional Indicator ──────────────────────────────────────────────
    private fun mapRegionalIndicator(char: Char): String = when {
        char in 'A'..'Z' -> codePointToString(0x1F1E6 + (char - 'A'))
        char in 'a'..'z' -> codePointToString(0x1F1E6 + (char - 'a'))
        else -> char.toString()
    }

    // ── Upside Down ─────────────────────────────────────────────────────
    private val upsideDownMap = mapOf(
        'a' to "ɐ", 'b' to "q", 'c' to "ɔ", 'd' to "p", 'e' to "ǝ", 'f' to "ɟ", 'g' to "ƃ",
        'h' to "ɥ", 'i' to "ı", 'j' to "ɾ", 'k' to "ʞ", 'l' to "l", 'm' to "ɯ", 'n' to "u",
        'o' to "o", 'p' to "d", 'q' to "b", 'r' to "ɹ", 's' to "s", 't' to "ʇ", 'u' to "n",
        'v' to "ʌ", 'w' to "ʍ", 'x' to "x", 'y' to "ʎ", 'z' to "z",
        'A' to "∀", 'B' to "𐐒", 'C' to "Ɔ", 'D' to "◖", 'E' to "Ǝ", 'F' to "Ⅎ", 'G' to "⅁",
        'H' to "H", 'I' to "I", 'J' to "ſ", 'K' to "Ʞ", 'L' to "⅂", 'M' to "W", 'N' to "N",
        'O' to "O", 'P' to "Ԁ", 'Q' to "𝮯", 'R' to "ᴚ", 'S' to "S", 'T' to "⊥", 'U' to "∩",
        'V' to "Λ", 'W' to "M", 'X' to "X", 'Y' to "⅄", 'Z' to "Z",
        '0' to "0", '1' to "Ɩ", '2' to "ᄅ", '3' to "Ɛ", '4' to "ㄣ", '5' to "ϛ", '6' to "9",
        '7' to "ㄥ", '8' to "8", '9' to "6",
        '?' to "¿", '!' to "¡", '.' to "˙", ',' to "'", '\'' to ",", '"' to "„", '_' to "‾"
    )

    private fun applyUpsideDown(input: String): String {
        val sb = StringBuilder(input.length * 2)
        for (char in input.reversed()) {
            sb.append(upsideDownMap[char] ?: char.toString())
        }
        return sb.toString()
    }

    // ── Utility ─────────────────────────────────────────────────────────

    // ── Utility & Normalization ─────────────────────────────────────────

    private val reverseExceptionsMap: Map<Int, Char> by lazy {
        val map = HashMap<Int, Char>()
        gothicUpperExceptions.forEach { (k, v) -> map[v] = k }
        scriptUpperExceptions.forEach { (k, v) -> map[v] = k }
        scriptLowerExceptions.forEach { (k, v) -> map[v] = k }
        doubleStruckUpperExceptions.forEach { (k, v) -> map[v] = k }
        smallCapsMap.forEach { (k, v) -> map[v.code] = k }
        superscriptMap.forEach { (k, v) -> map[v.code] = k }
        map[0x210E] = 'h' // Planck constant
        map
    }

    /**
     * Normalize stylized Unicode characters (Mathematical Alphanumeric Symbols, Fullwidth,
     * Enclosed Alphanumerics, Small Caps, Superscript, etc.) back to standard ASCII characters.
     *
     * Used by autocomplete Trie lookup and word boundary detection so that typing in
     * any FontStyle correctly matches standard dictionary words.
     */
    fun normalizeToAscii(input: String): String {
        if (input.isEmpty()) return input
        val uncombined = input.replace("\u0336", "").replace("\u0338", "")
        val sb = StringBuilder(uncombined.length)
        var i = 0
        while (i < uncombined.length) {
            val codePoint = uncombined.codePointAt(i)
            val asciiChar = normalizeCodePointToAscii(codePoint)
            if (asciiChar != null) {
                sb.append(asciiChar)
            } else {
                sb.appendCodePoint(codePoint)
            }
            i += Character.charCount(codePoint)
        }
        return sb.toString()
    }

    private fun normalizeCodePointToAscii(cp: Int): Char? {
        // Direct exception lookup
        val exceptionChar = reverseExceptionsMap[cp]
        if (exceptionChar != null) return exceptionChar

        return when (cp) {
            // Mathematical Alphanumeric Symbols (Uppercase)
            in 0x1D400..0x1D419 -> 'A' + (cp - 0x1D400)
            in 0x1D434..0x1D44D -> 'A' + (cp - 0x1D434)
            in 0x1D468..0x1D481 -> 'A' + (cp - 0x1D468)
            in 0x1D49C..0x1D4B5 -> 'A' + (cp - 0x1D49C)
            in 0x1D4D0..0x1D4E9 -> 'A' + (cp - 0x1D4D0)
            in 0x1D504..0x1D51D -> 'A' + (cp - 0x1D504)
            in 0x1D538..0x1D551 -> 'A' + (cp - 0x1D538)
            in 0x1D56C..0x1D585 -> 'A' + (cp - 0x1D56C)
            in 0x1D5A0..0x1D5B9 -> 'A' + (cp - 0x1D5A0)
            in 0x1D5D4..0x1D5ED -> 'A' + (cp - 0x1D5D4)
            in 0x1D608..0x1D621 -> 'A' + (cp - 0x1D608)
            in 0x1D63C..0x1D655 -> 'A' + (cp - 0x1D63C)
            in 0x1D670..0x1D689 -> 'A' + (cp - 0x1D670)

            // Mathematical Alphanumeric Symbols (Lowercase)
            in 0x1D41A..0x1D433 -> 'a' + (cp - 0x1D41A)
            in 0x1D44E..0x1D467 -> 'a' + (cp - 0x1D44E)
            in 0x1D482..0x1D49B -> 'a' + (cp - 0x1D482)
            in 0x1D4B6..0x1D4CF -> 'a' + (cp - 0x1D4B6)
            in 0x1D4EA..0x1D503 -> 'a' + (cp - 0x1D4EA)
            in 0x1D51E..0x1D537 -> 'a' + (cp - 0x1D51E)
            in 0x1D552..0x1D56B -> 'a' + (cp - 0x1D552)
            in 0x1D586..0x1D59F -> 'a' + (cp - 0x1D586)
            in 0x1D5BA..0x1D5D3 -> 'a' + (cp - 0x1D5BA)
            in 0x1D5EE..0x1D607 -> 'a' + (cp - 0x1D5EE)
            in 0x1D622..0x1D63B -> 'a' + (cp - 0x1D622)
            in 0x1D656..0x1D66F -> 'a' + (cp - 0x1D656)
            in 0x1D68A..0x1D6A3 -> 'a' + (cp - 0x1D68A)

            // Digits
            in 0x1D7CE..0x1D7D7 -> '0' + (cp - 0x1D7CE)
            in 0x1D7D8..0x1D7E1 -> '0' + (cp - 0x1D7D8)
            in 0x1D7E2..0x1D7EB -> '0' + (cp - 0x1D7E2)
            in 0x1D7EC..0x1D7F5 -> '0' + (cp - 0x1D7EC)
            in 0x1D7F6..0x1D7FF -> '0' + (cp - 0x1D7F6)

            // Enclosed Alphanumerics & Fullwidth
            in 0x24B6..0x24CF -> 'A' + (cp - 0x24B6)
            in 0x24D0..0x24E9 -> 'a' + (cp - 0x24D0)
            in 0x1F110..0x1F129 -> 'A' + (cp - 0x1F110)
            in 0x249C..0x24B5 -> 'a' + (cp - 0x249C)
            in 0x1F130..0x1F149 -> 'A' + (cp - 0x1F130)
            in 0x1F150..0x1F169 -> 'A' + (cp - 0x1F150)
            in 0x1F170..0x1F189 -> 'A' + (cp - 0x1F170)
            in 0x1F1E6..0x1F1FF -> 'A' + (cp - 0x1F1E6)
            in 0xFF01..0xFF5E -> (cp - 0xFF00 + 0x20).toChar()

            else -> null
        }
    }

    /** Convert a Unicode codepoint (possibly supplementary) to a String. */
    private fun codePointToString(codePoint: Int): String {
        return String(Character.toChars(codePoint))
    }
}
