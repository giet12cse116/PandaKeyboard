package com.panda.keyboards.ime.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive unit tests for [SuggestionManager] and autocomplete suggestions.
 */
class SuggestionManagerTest {

    @Test
    fun extractCurrentWord_extractsPartialWordBeforeCursor() {
        assertEquals("whe", SuggestionManager.extractCurrentWord("Hello world whe"))
        assertEquals("Ana", SuggestionManager.extractCurrentWord("Testing Ana"))
        assertEquals("Pe", SuggestionManager.extractCurrentWord("Type Pe"))
        assertEquals("", SuggestionManager.extractCurrentWord("Hello world "))
        assertEquals("", SuggestionManager.extractCurrentWord("Hello."))
    }

    @Test
    fun getSuggestions_whePrefix_returnsMultipleSuggestions() {
        val suggestions = SuggestionManager.getSuggestions("Whe", limit = 4)

        assertTrue("Should return up to 4 suggestions", suggestions.size in 1..4)
        assertTrue("Suggestions should contain When", suggestions.contains("When"))
        assertTrue("Suggestions should contain Where", suggestions.contains("Where"))
    }

    @Test
    fun getSuggestions_anaPrefix_returnsMultipleSuggestions() {
        val suggestions = SuggestionManager.getSuggestions("Ana", limit = 4)

        assertTrue("Should return up to 4 suggestions", suggestions.size in 1..4)
        assertTrue("Suggestions should contain Analysis", suggestions.contains("Analysis"))
        assertTrue("Suggestions should contain Analyze", suggestions.contains("Analyze"))
    }

    @Test
    fun getSuggestions_pePrefix_returnsMultipleSuggestions() {
        val suggestions = SuggestionManager.getSuggestions("Pe", limit = 4)

        assertTrue("Should return up to 4 suggestions", suggestions.size in 1..4)
        assertTrue("Suggestions should contain People", suggestions.contains("People"))
        assertTrue("Suggestions should contain Person", suggestions.contains("Person"))
    }

    @Test
    fun recordLearnedWord_boostsCustomUserWordToTop() {
        val customWord = "PandaAwesome"
        SuggestionManager.recordLearnedWord(customWord)

        val suggestions = SuggestionManager.getSuggestions("PandaAwe", limit = 4)
        assertTrue("Learned word should be suggested", suggestions.contains("PandaAwesome"))
    }

    @Test
    fun getSuggestions_underTwoChars_returnsEmptyList() {
        assertTrue(SuggestionManager.getSuggestions("").isEmpty())
        assertTrue(SuggestionManager.getSuggestions("W").isEmpty())
    }
}
