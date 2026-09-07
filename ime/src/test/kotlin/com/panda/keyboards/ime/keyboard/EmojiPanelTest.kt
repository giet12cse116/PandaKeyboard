package com.panda.keyboards.ime.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Sprint 10 Emoji Panel simplification and expanded dataset.
 * Tests emoji key presence, KeyboardMode.EMOJI, emoji data categories,
 * multi-codepoint ZWJ sequences, skin-tone modifiers, and dataset breadth.
 */
class EmojiPanelTest {

    @Test
    fun `emojiKey is present in bottom row layout`() {
        val letterRows = KeyboardLayouts.getLetterRows(com.panda.keyboards.theme.QwertyOrder.QWERTY)
        val bottomRow = letterRows.last()

        val emojiKey = bottomRow.find { it.type == KeyType.EMOJI }
        assertNotNull("Emoji key must be present in the bottom row layout", emojiKey)
        assertEquals("😀", emojiKey?.label)
        assertEquals(1f, emojiKey?.widthWeight)
    }

    @Test
    fun `keyboardMode includes EMOJI state`() {
        val modes = KeyboardMode.entries
        assertTrue("KeyboardMode must contain EMOJI mode", modes.contains(KeyboardMode.EMOJI))
    }

    @Test
    fun `emojiData defines 8 standard Unicode categories with comprehensive coverage`() {
        val categories = EmojiCategory.standardCategories
        assertEquals("EmojiCategory must have 8 standard categories", 8, categories.size)

        var totalEmojis = 0
        for (category in categories) {
            val list = category.emojis
            assertNotNull("Category ${category.displayName} must return emojis list", list)
            assertTrue("Category ${category.displayName} must contain emojis", list.isNotEmpty())
            totalEmojis += list.size
        }

        assertTrue("Emoji dataset must contain over 300 emojis for Gboard comparability, found: $totalEmojis", totalEmojis > 300)
    }

    @Test
    fun `emojiData contains skin tone modifiers and complex ZWJ sequences`() {
        val smileysAndPeople = EmojiCategory.SMILEYS.emojis
        val flags = EmojiCategory.FLAGS.emojis

        // Skin tones
        assertTrue("Must contain skin tone wave", smileysAndPeople.contains("👋🏻"))
        assertTrue("Must contain dark skin tone wave", smileysAndPeople.contains("👋🏿"))

        // ZWJ professions & family
        assertTrue("Must contain male technologist ZWJ sequence", smileysAndPeople.contains("👨‍💻"))
        assertTrue("Must contain female astronaut ZWJ sequence", smileysAndPeople.contains("👩‍🚀"))
        assertTrue("Must contain family ZWJ sequence", smileysAndPeople.contains("👨‍👩‍👧"))

        // Flags with ZWJ / tag sequences
        assertTrue("Must contain Pirate flag (ZWJ sequence)", flags.contains("🏴‍☠️"))
        assertTrue("Must contain Rainbow flag (ZWJ sequence)", flags.contains("🏳️‍🌈"))
    }
}

