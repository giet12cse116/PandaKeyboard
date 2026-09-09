package com.panda.keyboards.fonts

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [FontTransformer].
 *
 * Each test verifies that the transform function correctly maps
 * "abc ABC 123" for each style, and that unsupported characters
 * pass through unchanged.
 */
class FontTransformerTest {

    private val testInput = "abc ABC 123"

    // ── NORMAL ──────────────────────────────────────────────────────────

    @Test
    fun `NORMAL returns input unchanged`() {
        assertEquals(testInput, FontTransformer.transform(testInput, FontStyle.NORMAL))
    }

    // ── BOLD SERIF ──────────────────────────────────────────────────────

    @Test
    fun `BOLD_SERIF transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BOLD_SERIF)
        // 𝐚𝐛𝐜 𝐀𝐁𝐂 𝟏𝟐𝟑 — Mathematical Bold
        assertTrue(result.contains("𝐚")) // U+1D41A
        assertTrue(result.contains("𝐀")) // U+1D400
        assertTrue(result.contains("𝟏")) // U+1D7CF
        assertNotEquals(testInput, result)
    }

    @Test
    fun `BOLD_SERIF maps uppercase A correctly`() {
        val result = FontTransformer.transform("A", FontStyle.BOLD_SERIF)
        assertEquals("𝐀", result) // U+1D400
    }

    @Test
    fun `BOLD_SERIF maps lowercase a correctly`() {
        val result = FontTransformer.transform("a", FontStyle.BOLD_SERIF)
        assertEquals("𝐚", result) // U+1D41A
    }

    @Test
    fun `BOLD_SERIF maps digit 0 correctly`() {
        val result = FontTransformer.transform("0", FontStyle.BOLD_SERIF)
        assertEquals("𝟎", result) // U+1D7CE
    }

    // ── BOLD SANS ───────────────────────────────────────────────────────

    @Test
    fun `BOLD_SANS transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BOLD_SANS)
        assertTrue(result.contains("𝗮")) // U+1D5EE
        assertTrue(result.contains("𝗔")) // U+1D5D4
        assertNotEquals(testInput, result)
    }

    // ── ITALIC SANS ─────────────────────────────────────────────────────

    @Test
    fun `ITALIC_SANS transforms letters, digits pass through`() {
        val result = FontTransformer.transform(testInput, FontStyle.ITALIC_SANS)
        assertTrue(result.contains("𝘢")) // U+1D622
        assertTrue(result.contains("𝘈")) // U+1D608
        // Italic sans has no digit mapping — digits pass through
        assertTrue(result.contains("1"))
        assertTrue(result.contains("2"))
        assertTrue(result.contains("3"))
    }

    // ── GOTHIC ──────────────────────────────────────────────────────────

    @Test
    fun `GOTHIC transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.GOTHIC)
        assertTrue(result.contains("𝔞")) // U+1D51E lowercase a
        assertNotEquals(testInput, result)
    }

    @Test
    fun `GOTHIC handles exception characters`() {
        // C, H, I, R, Z have irregular codepoints
        assertEquals("ℭ", FontTransformer.transform("C", FontStyle.GOTHIC))
        assertEquals("ℌ", FontTransformer.transform("H", FontStyle.GOTHIC))
        assertEquals("ℑ", FontTransformer.transform("I", FontStyle.GOTHIC))
        assertEquals("ℜ", FontTransformer.transform("R", FontStyle.GOTHIC))
        assertEquals("ℨ", FontTransformer.transform("Z", FontStyle.GOTHIC))
    }

    // ── SCRIPT ──────────────────────────────────────────────────────────

    @Test
    fun `SCRIPT transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.SCRIPT)
        assertNotEquals(testInput, result)
    }

    @Test
    fun `SCRIPT handles exception characters`() {
        assertEquals("ℬ", FontTransformer.transform("B", FontStyle.SCRIPT))
        assertEquals("ℰ", FontTransformer.transform("E", FontStyle.SCRIPT))
        assertEquals("ℱ", FontTransformer.transform("F", FontStyle.SCRIPT))
        assertEquals("ℋ", FontTransformer.transform("H", FontStyle.SCRIPT))
        assertEquals("ℯ", FontTransformer.transform("e", FontStyle.SCRIPT))
        assertEquals("ℊ", FontTransformer.transform("g", FontStyle.SCRIPT))
    }

    // ── MONOSPACE ───────────────────────────────────────────────────────

    @Test
    fun `MONOSPACE transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.MONOSPACE)
        assertTrue(result.contains("𝚊")) // U+1D68A
        assertTrue(result.contains("𝙰")) // U+1D670
        assertTrue(result.contains("𝟷")) // U+1D7F7
        assertNotEquals(testInput, result)
    }

    // ── FULLWIDTH ───────────────────────────────────────────────────────

    @Test
    fun `FULLWIDTH transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.FULLWIDTH)
        assertTrue(result.contains("ａ")) // U+FF41
        assertTrue(result.contains("Ａ")) // U+FF21
        assertTrue(result.contains("１")) // U+FF11
        assertNotEquals(testInput, result)
    }

    @Test
    fun `FULLWIDTH preserves spaces`() {
        val result = FontTransformer.transform(" ", FontStyle.FULLWIDTH)
        assertEquals(" ", result) // regular space passes through
    }

    // ── BUBBLE ──────────────────────────────────────────────────────────

    @Test
    fun `BUBBLE transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BUBBLE)
        assertTrue(result.contains("ⓐ")) // U+24D0
        assertTrue(result.contains("Ⓐ")) // U+24B6
        assertTrue(result.contains("①")) // U+2460
        assertNotEquals(testInput, result)
    }

    @Test
    fun `BUBBLE maps zero correctly`() {
        val result = FontTransformer.transform("0", FontStyle.BUBBLE)
        assertEquals("⓪", result) // U+24EA
    }

    // ── CIRCLED ─────────────────────────────────────────────────────────

    @Test
    fun `CIRCLED transforms uppercase correctly`() {
        val result = FontTransformer.transform("A", FontStyle.CIRCLED)
        assertEquals("🅐", result) // U+1F150
    }

    @Test
    fun `CIRCLED maps zero correctly`() {
        val result = FontTransformer.transform("0", FontStyle.CIRCLED)
        assertEquals("⓿", result) // U+24FF
    }

    @Test
    fun `CIRCLED maps digit 1 correctly`() {
        val result = FontTransformer.transform("1", FontStyle.CIRCLED)
        assertEquals("❶", result) // U+2776
    }

    // ── SMALL CAPS ──────────────────────────────────────────────────────

    @Test
    fun `SMALL_CAPS transforms lowercase only`() {
        val result = FontTransformer.transform(testInput, FontStyle.SMALL_CAPS)
        assertTrue(result.contains("ᴀ"))  // small cap a
        assertTrue(result.contains("ʙ"))  // small cap b
        assertTrue(result.contains("ᴄ"))  // small cap c
        // Uppercase and digits pass through
        assertTrue(result.contains("A"))
        assertTrue(result.contains("1"))
    }

    // ── STRIKETHROUGH ───────────────────────────────────────────────────

    @Test
    fun `STRIKETHROUGH appends combining overlay`() {
        val result = FontTransformer.transform("abc", FontStyle.STRIKETHROUGH)
        // Each letter should be followed by U+0336
        assertEquals("a\u0336b\u0336c\u0336", result)
    }

    @Test
    fun `STRIKETHROUGH does not strikethrough spaces`() {
        val result = FontTransformer.transform("a b", FontStyle.STRIKETHROUGH)
        assertEquals("a\u0336 b\u0336", result)
    }

    // ── SUPERSCRIPT ─────────────────────────────────────────────────────

    @Test
    fun `SUPERSCRIPT transforms digits correctly`() {
        val result = FontTransformer.transform("123", FontStyle.SUPERSCRIPT)
        assertEquals("¹²³", result)
    }

    @Test
    fun `SUPERSCRIPT transforms lowercase correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.SUPERSCRIPT)
        assertEquals("ᵃᵇᶜ", result)
    }

    // ── NEW SPRINT 12 EXPANDED STYLES ────────────────────────────────────

    @Test
    fun `SANS_SERIF transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.SANS_SERIF)
        assertTrue(result.contains("𝖺")) // U+1D5BA
        assertTrue(result.contains("𝖠")) // U+1D5A0
        assertTrue(result.contains(String(Character.toChars(0x1D7E3)))) // U+1D7E3 (for '1')
        assertNotEquals(testInput, result)
    }

    @Test
    fun `ITALIC_SERIF transforms correctly and handles h exception`() {
        val result = FontTransformer.transform(testInput, FontStyle.ITALIC_SERIF)
        assertTrue(result.contains("𝑎")) // U+1D44E
        assertTrue(result.contains("𝐴")) // U+1D434
        assertEquals("ℎ", FontTransformer.transform("h", FontStyle.ITALIC_SERIF))
        assertNotEquals(testInput, result)
    }

    @Test
    fun `BOLD_ITALIC_SANS transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BOLD_ITALIC_SANS)
        assertTrue(result.contains("𝙖")) // U+1D656
        assertTrue(result.contains("𝘼")) // U+1D63C
        assertNotEquals(testInput, result)
    }

    @Test
    fun `BOLD_ITALIC_SERIF transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BOLD_ITALIC_SERIF)
        assertTrue(result.contains("𝒂")) // U+1D482
        assertTrue(result.contains("𝑨")) // U+1D468
        assertNotEquals(testInput, result)
    }

    @Test
    fun `DOUBLE_STRUCK transforms correctly and handles exceptions`() {
        val result = FontTransformer.transform(testInput, FontStyle.DOUBLE_STRUCK)
        assertTrue(result.contains("𝕒")) // U+1D552
        assertTrue(result.contains("𝔸")) // U+1D538
        assertEquals("ℂ", FontTransformer.transform("C", FontStyle.DOUBLE_STRUCK))
        assertEquals("ℍ", FontTransformer.transform("H", FontStyle.DOUBLE_STRUCK))
        assertEquals("ℕ", FontTransformer.transform("N", FontStyle.DOUBLE_STRUCK))
        assertEquals("ℝ", FontTransformer.transform("R", FontStyle.DOUBLE_STRUCK))
        assertNotEquals(testInput, result)
    }

    @Test
    fun `BOLD_SCRIPT transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BOLD_SCRIPT)
        assertTrue(result.contains("𝓪")) // U+1D4EA
        assertTrue(result.contains("𝓐")) // U+1D4D0
        assertNotEquals(testInput, result)
    }

    @Test
    fun `BOLD_GOTHIC transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.BOLD_GOTHIC)
        assertTrue(result.contains("𝖆")) // U+1D586
        assertTrue(result.contains("𝕬")) // U+1D56C
        assertNotEquals(testInput, result)
    }

    @Test
    fun `PARENTHESIZED transforms correctly`() {
        val result = FontTransformer.transform(testInput, FontStyle.PARENTHESIZED)
        assertTrue(result.contains("⒜")) // U+249C
        assertTrue(result.contains("🄐")) // U+1F110
        assertTrue(result.contains("⑴")) // U+2474
        assertNotEquals(testInput, result)
    }

    @Test
    fun `SQUARED transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.SQUARED)
        val expectedA = String(Character.toChars(0x1F130)) // 🄰 U+1F130
        assertTrue(result.contains(expectedA))
        assertNotEquals("abc ABC", result)
    }

    @Test
    fun `NEGATIVE_SQUARED transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.NEGATIVE_SQUARED)
        assertTrue(result.contains("🅰")) // U+1F170
        assertNotEquals("abc ABC", result)
    }

    @Test
    fun `REGIONAL_INDICATOR transforms correctly`() {
        val result = FontTransformer.transform("hi", FontStyle.REGIONAL_INDICATOR)
        assertEquals("🇭🇮", result)
    }

    @Test
    fun `UPSIDE_DOWN transforms and reverses correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.UPSIDE_DOWN)
        assertEquals("ɔqɐ", result)
    }

    // ── 21 NEW REQUESTED FONTS ──────────────────────────────────────────

    @Test
    fun `CALIBRI transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.CALIBRI)
        assertTrue(result.contains("𝖺"))
        assertTrue(result.contains("𝖠"))
    }

    @Test
    fun `ALGERIAN transforms correctly to uppercase styled characters`() {
        val result = FontTransformer.transform("abc", FontStyle.ALGERIAN)
        assertTrue(result.contains("𝐀"))
        assertTrue(result.contains("𝐁"))
        assertTrue(result.contains("𝐂"))
    }

    @Test
    fun `ARIAL_BLACK transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.ARIAL_BLACK)
        assertTrue(result.contains("𝗮"))
        assertTrue(result.contains("𝗔"))
    }

    @Test
    fun `BAHNSCHRIFT transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.BAHNSCHRIFT)
        assertTrue(result.contains("𝖺"))
        assertTrue(result.contains("𝖠"))
    }

    @Test
    fun `BRUSH_SCRIPT transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.BRUSH_SCRIPT)
        assertNotEquals("abc", result)
    }

    @Test
    fun `CHILLER appends combining overlay`() {
        val result = FontTransformer.transform("abc", FontStyle.CHILLER)
        assertEquals("a\u0338b\u0338c\u0338", result)
    }

    @Test
    fun `BRADLEY_HAND transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.BRADLEY_HAND)
        assertNotEquals("abc", result)
    }

    @Test
    fun `BODONI_MT transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.BODONI_MT)
        assertTrue(result.contains("𝐚"))
        assertTrue(result.contains("𝐀"))
    }

    @Test
    fun `BOOKMAN_OLD_STYLE transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.BOOKMAN_OLD_STYLE)
        assertTrue(result.contains("𝐚"))
        assertTrue(result.contains("𝐀"))
    }

    @Test
    fun `CASCADIA_CODE transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.CASCADIA_CODE)
        assertTrue(result.contains("𝚊"))
        assertTrue(result.contains("𝙰"))
    }

    @Test
    fun `EDWARDIAN_SCRIPT transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.EDWARDIAN_SCRIPT)
        assertNotEquals("abc", result)
    }

    @Test
    fun `COPPERPLATE_GOTHIC transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.COPPERPLATE_GOTHIC)
        assertTrue(result.contains("ᴀ"))
        assertTrue(result.contains("ʙ"))
        assertTrue(result.contains("ᴄ"))
    }

    @Test
    fun `COMIC_SANS transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.COMIC_SANS)
        assertTrue(result.contains("𝖺"))
        assertTrue(result.contains("𝖠"))
    }

    @Test
    fun `ELEPHANT transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.ELEPHANT)
        assertTrue(result.contains("𝐚"))
        assertTrue(result.contains("𝐀"))
    }

    @Test
    fun `FRENCH_SCRIPT transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.FRENCH_SCRIPT)
        assertNotEquals("abc", result)
    }

    @Test
    fun `FREESTYLE_SCRIPT transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.FREESTYLE_SCRIPT)
        assertNotEquals("abc", result)
    }

    @Test
    fun `TIMES_NEW_ROMAN transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.TIMES_NEW_ROMAN)
        assertTrue(result.contains("𝐚"))
        assertTrue(result.contains("𝐀"))
    }

    @Test
    fun `SCRIPT_MT_BOLD transforms correctly`() {
        val result = FontTransformer.transform("abc ABC", FontStyle.SCRIPT_MT_BOLD)
        assertTrue(result.contains("𝓪"))
        assertTrue(result.contains("𝓐"))
    }

    @Test
    fun `POOR_RICHARD transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.POOR_RICHARD)
        assertTrue(result.contains("𝔞"))
    }

    @Test
    fun `OLD_ENGLISH transforms correctly`() {
        val result = FontTransformer.transform("abc", FontStyle.OLD_ENGLISH)
        assertTrue(result.contains("𝔞"))
    }

    // ── EDGE CASES ──────────────────────────────────────────────────────

    @Test
    fun `empty string returns empty string for all styles`() {
        FontStyle.entries.forEach { style ->
            assertEquals("", FontTransformer.transform("", style),
                "Empty string should return empty for ${style.displayName}")
        }
    }

    @Test
    fun `unsupported characters pass through unchanged`() {
        // Use only chars that no style maps: space, emoji, and chars outside
        // FULLWIDTH's ASCII range (none here — so we test per-style)
        val emoji = "🐼"
        FontStyle.entries.forEach { style ->
            val result = FontTransformer.transform(emoji, style)
            // Strikethrough appends combining char but preserves the base
            assertTrue(result.contains(emoji),
                "Emoji should be preserved for ${style.displayName}, got: $result")
        }
        // Verify punctuation passes through for non-FULLWIDTH, non-SUPERSCRIPT styles
        val punctuation = ".,?"
        val offsetStyles = listOf(
            FontStyle.NORMAL, FontStyle.BOLD_SERIF, FontStyle.BOLD_SANS,
            FontStyle.ITALIC_SANS, FontStyle.GOTHIC, FontStyle.SCRIPT,
            FontStyle.MONOSPACE, FontStyle.BUBBLE, FontStyle.CIRCLED,
            FontStyle.SMALL_CAPS
        )
        offsetStyles.forEach { style ->
            val result = FontTransformer.transform(punctuation, style)
            assertTrue(result.contains("."),
                "Period should pass through for ${style.displayName}")
        }
    }

    @Test
    fun `space is preserved in all styles`() {
        FontStyle.entries.forEach { style ->
            val result = FontTransformer.transform("a b", style)
            assertTrue(result.contains(" ") || result.contains("\u3000"),
                "Space should be preserved or fullwidth for ${style.displayName}")
        }
    }

    // ── COMPREHENSIVE TRANSFORM TEST ────────────────────────────────────

    @Test
    fun `all styles produce non-empty output for non-empty input`() {
        FontStyle.entries.forEach { style ->
            val result = FontTransformer.transform(testInput, style)
            assertTrue(result.isNotEmpty(),
                "${style.displayName} should produce non-empty output")
        }
    }

    @Test
    fun `all non-NORMAL styles actually transform letters`() {
        FontStyle.entries.filter { it != FontStyle.NORMAL }.forEach { style ->
            val result = FontTransformer.transform("a", style)
            // The result should not be plain "a" (it may include combining chars)
            assertNotEquals("a", result,
                "${style.displayName} should transform 'a'")
        }
    }
}
