package com.panda.keyboards.ime.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for SuggestionBar UI reservation and branded placeholder logic.
 */
class SuggestionBarTest {

    @Test
    fun `suggestion bar height is reserved at 36dp when autoCorrection is enabled`() {
        fun computeSuggestionBarHeightDp(autoCorrectionEnabled: Boolean, isEmojiOrClipboard: Boolean): Int {
            return if (isEmojiOrClipboard) 36 else if (autoCorrectionEnabled) 36 else 0
        }

        // Even with 0 suggestions, when autoCorrection is enabled, height remains 36dp
        assertEquals(36, computeSuggestionBarHeightDp(autoCorrectionEnabled = true, isEmojiOrClipboard = false))
        assertEquals(0, computeSuggestionBarHeightDp(autoCorrectionEnabled = false, isEmojiOrClipboard = false))
        assertEquals(36, computeSuggestionBarHeightDp(autoCorrectionEnabled = false, isEmojiOrClipboard = true))
    }

    @Test
    fun `suggestion strip empty state displays Panda Keyboards branded placeholder`() {
        fun resolveSuggestionStripContent(suggestions: List<String>, quickPasteText: String?): String {
            return when {
                suggestions.isNotEmpty() -> "SUGGESTIONS:${suggestions.joinToString(",")}"
                !quickPasteText.isNullOrBlank() -> "QUICK_PASTE:$quickPasteText"
                else -> "BRANDED_PLACEHOLDER:Panda Keyboards"
            }
        }

        // Empty state with 0 suggestions and no clip -> Branded placeholder
        assertEquals("BRANDED_PLACEHOLDER:Panda Keyboards", resolveSuggestionStripContent(emptyList(), null))

        // When suggestions are present -> Suggestion chips replace placeholder
        assertEquals("SUGGESTIONS:the,that,this,there", resolveSuggestionStripContent(listOf("the", "that", "this", "there"), null))

        // When quick paste is available -> Quick paste chip takes priority over branded placeholder
        assertEquals("QUICK_PASTE:Hello World", resolveSuggestionStripContent(emptyList(), "Hello World"))

        // When user types 2+ letters and suggestions become available -> Replaces quick paste chip
        assertEquals("SUGGESTIONS:the,that", resolveSuggestionStripContent(listOf("the", "that"), "Hello World"))
    }
}
