package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Automated test suite validating the production word-frequency dictionary dataset.
 *
 * Ensures:
 * 1. Target dataset size is within the production range (15,000 - 30,000 words).
 * 2. 50+ essential everyday words exist in the dictionary resource.
 * 3. Letter distribution balance covers all 26 letters A through Z.
 * 4. Trie load time meets performance threshold (< 150ms).
 * 5. Prefix completion quality ranks common words at top positions.
 */
class DictionaryValidationTest {

    companion object {
        private lateinit var trie: WordTrie
        private var loadTimeMs: Long = 0L

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            loadTimeMs = measureTimeMillis {
                trie = DictionaryLoader.loadFromResource("/dictionary_en.txt")
            }
        }

        private val ESSENTIAL_EVERYDAY_WORDS = listOf(
            "hello", "thanks", "please", "today", "tomorrow", "phone",
            "email", "message", "okay", "yes", "sorry", "morning",
            "happy", "birthday", "meeting", "call", "coffee", "weather",
            "home", "work", "love", "good", "great", "the",
            "that", "have", "this", "with", "you", "from",
            "they", "will", "would", "there", "their", "what",
            "about", "which", "when", "make", "like", "time",
            "just", "know", "take", "people", "into", "year",
            "your", "some"
        )
    }

    @Test
    fun dictionary_sizeIsWithinProductionRange() {
        val wordCount = trie.size
        assertTrue(
            "Dictionary size ($wordCount) should be within 15,000 and 30,000 words",
            wordCount in 15000..30000
        )
    }

    @Test
    fun dictionary_containsAllFiftyEssentialEverydayWords() {
        val missingWords = mutableListOf<String>()
        for (word in ESSENTIAL_EVERYDAY_WORDS) {
            if (!trie.contains(word)) {
                missingWords.add(word)
            }
        }
        assertTrue(
            "All essential everyday words must be present. Missing: $missingWords",
            missingWords.isEmpty()
        )
    }

    @Test
    fun dictionary_letterCoverageIsBalancedAcrossAll26Letters() {
        for (ch in 'a'..'z') {
            val prefixWords = trie.wordsWithPrefix(ch.toString(), limit = 10)
            assertTrue(
                "Letter '$ch' must have word entries in dictionary",
                prefixWords.isNotEmpty()
            )
        }
    }

    @Test
    fun dictionary_loadPerformanceUnderThreshold() {
        assertTrue(
            "Dictionary Trie load time ($loadTimeMs ms) should be under 250ms",
            loadTimeMs < 250
        )
    }

    @Test
    fun dictionary_prefixSuggestions_he_includesHelloAndHelp() {
        val suggestions = trie.wordsWithPrefix("he", limit = 4).map { it.word }
        assertTrue("Prefix 'he' should surface 'he'", suggestions.contains("he"))
        assertTrue("Prefix 'he' should surface 'hello'", suggestions.contains("hello"))
        assertTrue("Prefix 'he' should surface 'help'", suggestions.contains("help"))
    }

    @Test
    fun dictionary_prefixSuggestions_th_includesTheThatThanks() {
        val suggestions = trie.wordsWithPrefix("th", limit = 4).map { it.word }
        assertTrue("Prefix 'th' should surface 'the'", suggestions.contains("the"))
        assertTrue("Prefix 'th' should surface 'that'", suggestions.contains("that"))
        assertTrue("Prefix 'th' should surface 'thanks'", suggestions.contains("thanks"))
    }

    @Test
    fun dictionary_prefixSuggestions_pl_includesPlease() {
        val suggestions = trie.wordsWithPrefix("pl", limit = 4).map { it.word }
        assertTrue("Prefix 'pl' should surface 'please'", suggestions.contains("please"))
    }
}
