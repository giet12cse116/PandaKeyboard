package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.measureTimeMillis

/**
 * Senior Architectural Quality Evaluation Test Suite for Panda Keyboards' :core-autocomplete engine.
 *
 * Performs deep evaluation of dictionary dataset quality, letter distribution sanity,
 * data integrity, behavioral correctness, and real-time execution performance.
 */
class DictionaryQualityEvaluationTest {

    companion object {
        private lateinit var trie: WordTrie
        private lateinit var userDict: UserDictionary
        private lateinit var engine: SuggestionEngine
        private var loadTimeMs: Long = 0L
        private val rawEntries = mutableListOf<Pair<String, Int>>()

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            loadTimeMs = measureTimeMillis {
                trie = DictionaryLoader.loadFromResource("/dictionary_en.txt", "/dictionary_proper_nouns.txt")
            }
            userDict = InMemoryUserDictionary()
            engine = SuggestionEngine(trie, userDict)

            // Read raw lines from resource for data integrity parsing
            val stream = DictionaryQualityEvaluationTest::class.java.getResourceAsStream("/dictionary_en.txt")
                ?: error("dictionary_en.txt not found in resources")
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).useLines { lines ->
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        val parts = trimmed.split(" ", limit = 2)
                        val word = parts[0]
                        val freq = if (parts.size > 1) parts[1].toIntOrNull() ?: 1 else 1
                        rawEntries.add(Pair(word, freq))
                    }
                }
            }
        }

        // Curated list of 100 high-frequency everyday English words across core categories
        val TOP_100_EVERYDAY_WORDS = listOf(
            // Greetings & Interjections (10)
            "hello", "thanks", "please", "sorry", "okay", "yes", "no", "bye", "welcome", "congrats",
            // Time / Date & Frequency (16)
            "today", "tomorrow", "yesterday", "morning", "afternoon", "evening", "night", "monday", "friday", "weekend", "time", "always", "never", "sometimes", "now", "later",
            // Nouns & Objects (26)
            "phone", "email", "message", "birthday", "meeting", "call", "coffee", "weather", "home", "work", "love", "food", "water", "school", "office", "car", "money", "music", "picture", "friend", "family", "house", "job", "name", "place", "number",
            // Verbs & Actions (22)
            "make", "like", "know", "take", "have", "come", "give", "think", "look", "want", "use", "find", "tell", "ask", "need", "feel", "try", "leave", "help", "start", "show", "talk",
            // Adjectives & Qualifiers (20)
            "good", "great", "happy", "new", "first", "last", "long", "little", "own", "other", "old", "right", "big", "high", "different", "small", "large", "early", "young", "best",
            // Core Function / Connectives (6)
            "the", "that", "this", "with", "you", "from"
        )

        // Standard English dictionary word-initial expected letter proportions
        val HIGH_MID_START_LETTERS = listOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'v', 'w')
        val LOW_MID_START_LETTERS = listOf('j', 'k', 'y')
        val RARE_START_LETTERS = listOf('q', 'x', 'z')
    }

    // ── 1. COMMON-WORD COVERAGE TEST ─────────────────────────────────────────

    @Test
    fun testCommonWordCoverage_100EverydayWordsExistInDictionary() {
        val missingWords = mutableListOf<String>()
        for (word in TOP_100_EVERYDAY_WORDS) {
            if (!trie.contains(word)) {
                missingWords.add(word)
            }
        }

        val coveragePercent = ((TOP_100_EVERYDAY_WORDS.size - missingWords.size).toDouble() / TOP_100_EVERYDAY_WORDS.size) * 100.0
        println("=== 1. Common Word Coverage Benchmark ===")
        println("Evaluated Words: ${TOP_100_EVERYDAY_WORDS.size}")
        println("Coverage Rate: ${String.format("%.1f", coveragePercent)}%")
        if (missingWords.isNotEmpty()) {
            println("Missing Words (${missingWords.size}): $missingWords")
        }

        assertTrue(
            "Dictionary dataset failed Everyday Word Coverage test! Missing ${missingWords.size} words out of 100: $missingWords",
            missingWords.isEmpty()
        )
    }

    // ── 2. LETTER-DISTRIBUTION SANITY TEST ───────────────────────────────────

    @Test
    fun testLetterDistributionSanity_balancedPerLetterproportions() {
        val totalWords = trie.size
        val letterCounts = mutableMapOf<Char, Int>()
        for (ch in 'a'..'z') {
            letterCounts[ch] = 0
        }

        for ((word, _) in rawEntries) {
            val firstChar = word.lowercase().firstOrNull()
            if (firstChar != null && firstChar in 'a'..'z') {
                letterCounts[firstChar] = (letterCounts[firstChar] ?: 0) + 1
            }
        }

        println("\n=== 2. Letter-Distribution Proportions ===")
        println("Total Words in Dictionary: $totalWords")
        val defectiveLetters = mutableListOf<String>()

        for (ch in 'a'..'z') {
            val count = letterCounts[ch] ?: 0
            val percentage = (count.toDouble() / totalWords) * 100.0
            println("Letter '$ch': $count words (${String.format("%.2f", percentage)}%)")

            val minExpected = when (ch) {
                in HIGH_MID_START_LETTERS -> 1.0
                in LOW_MID_START_LETTERS -> 0.3
                in RARE_START_LETTERS -> 0.05
                else -> 0.1
            }

            if (percentage < minExpected) {
                defectiveLetters.add("'$ch': $count words (${String.format("%.2f", percentage)}% vs min ${minExpected}%)")
            }
        }

        assertTrue(
            "Letter distribution sanity failure! The following starting letters have implausibly low coverage: $defectiveLetters",
            defectiveLetters.isEmpty()
        )
    }

    // ── 3. DATA INTEGRITY TEST ───────────────────────────────────────────────

    @Test
    fun testDataIntegrity_noCorruptedEntriesAndValidFrequencyOrdering() {
        val corruptedEntries = mutableListOf<String>()
        val duplicates = mutableSetOf<String>()
        val duplicateEntriesFound = mutableListOf<String>()
        var invalidFrequencyCount = 0

        val validCharRegex = Regex("^[a-z]+(['-][a-z]+)*$")

        for ((word, freq) in rawEntries) {
            val lower = word.lowercase()
            if (duplicates.contains(lower)) {
                duplicateEntriesFound.add(word)
            } else {
                duplicates.add(lower)
            }

            if (freq < 0) {
                invalidFrequencyCount++
            }

            // Check for leading/trailing punctuation or fragments
            if (word.isEmpty() || word.startsWith("-") || word.endsWith("-") ||
                word.startsWith("'") || word.endsWith("'") || !word.lowercase().matches(validCharRegex)
            ) {
                corruptedEntries.add(word)
            }
        }

        println("\n=== 3. Data Integrity & Quality Evaluation ===")
        println("Raw Entries Inspected: ${rawEntries.size}")
        println("Duplicate Word Count: ${duplicateEntriesFound.size}")
        println("Corrupted Entry Count: ${corruptedEntries.size}")

        // Sort top 20 words by frequency
        val top20Words = rawEntries.sortedByDescending { it.second }.take(20).map { it.first.lowercase() }
        println("Top 20 Highest Frequency Words: $top20Words")

        // Assertions
        assertTrue(
            "Corrupted or invalid token fragments found in dictionary (${corruptedEntries.size}): ${corruptedEntries.take(10)}",
            corruptedEntries.isEmpty()
        )

        assertTrue(
            "Duplicate entries found in dictionary (${duplicateEntriesFound.size}): ${duplicateEntriesFound.take(10)}",
            duplicateEntriesFound.isEmpty()
        )

        assertEquals("Negative frequency values found", 0, invalidFrequencyCount)

        // Top 20 high-frequency words must contain plausible core English function words
        val expectedTopFunctionWords = listOf("the", "of", "and", "to", "in", "a", "is", "that", "for", "it", "was", "on", "are", "as", "with", "at", "be", "this", "have", "from")
        val topMatchCount = top20Words.count { it in expectedTopFunctionWords }
        assertTrue(
            "Top 20 words must reflect realistic corpus frequencies. Matched $topMatchCount/20 function words in top 20: $top20Words",
            topMatchCount >= 10
        )
    }

    // ── 4. SUGGESTION ENGINE BEHAVIORAL TESTS ────────────────────────────────

    @Test
    fun testSuggestionEngineBehavior_prefixMatchesLimitAndRanking() {
        val testPrefixes = listOf("he", "th", "wor", "app")
        for (prefix in testPrefixes) {
            val suggestions = engine.suggestionsFor(prefix, limit = 4)
            assertFalse("Suggestions for prefix '$prefix' must not be empty", suggestions.isEmpty())
            assertTrue("Suggestions for '$prefix' must not exceed limit 4", suggestions.size <= 4)
            for (s in suggestions) {
                assertTrue("Suggestion '$s' must start with prefix '$prefix'", s.lowercase().startsWith(prefix.lowercase()))
            }
        }
    }

    @Test
    fun testSuggestionEngineBehavior_twoCharTriggerThreshold() {
        // 1-character input returns empty list
        val singleCharSugs = engine.suggestionsFor("h", limit = 4)
        assertTrue("1-character input must return empty list per 2-char threshold spec", singleCharSugs.isEmpty())

        // 2-character input returns candidates
        val twoCharSugs = engine.suggestionsFor("he", limit = 4)
        assertFalse("2-character input must return candidates", twoCharSugs.isEmpty())
    }

    @Test
    fun testSuggestionEngineBehavior_casingPreservation() {
        val lowerSugs = engine.suggestionsFor("hel", limit = 3)
        assertTrue("Lowercase prefix 'hel' must return lowercase 'hello'", lowerSugs.contains("hello"))

        val capSugs = engine.suggestionsFor("Hel", limit = 3)
        assertTrue("Capitalized prefix 'Hel' must return capitalized 'Hello'", capSugs.contains("Hello"))

        val upperSugs = engine.suggestionsFor("HEL", limit = 3)
        assertTrue("Uppercase prefix 'HEL' must return uppercase 'HELLO'", upperSugs.contains("HELLO"))
    }

    @Test
    fun testSuggestionEngineBehavior_userDictionaryBlending() {
        val customUserWord = "helmuth"
        userDict.recordWord(customUserWord)

        val suggestions = engine.suggestionsFor("hel", limit = 4)
        assertTrue(
            "User-learned word '$customUserWord' must appear in suggestions for prefix 'hel'",
            suggestions.contains("helmuth")
        )
        assertEquals("User-learned word should be boosted to top rank", customUserWord, suggestions[0])
    }

    // ── 5. PERFORMANCE TESTS ────────────────────────────────────────────────

    @Test
    fun testPerformance_trieLoadTimeUnderThreshold() {
        println("\n=== 5. Performance Benchmarks ===")
        println("WordTrie Load Time: $loadTimeMs ms")
        assertTrue("WordTrie load time ($loadTimeMs ms) must be under 200 ms", loadTimeMs < 200)
    }

    @Test
    fun testPerformance_suggestionLookupLatencyUnderFrameBudget() {
        var totalNanoTime = 0L
        val testQueries = listOf("he", "th", "wor", "app", "com", "con", "str", "pro", "int", "rea")

        for (query in testQueries) {
            val start = System.nanoTime()
            engine.suggestionsFor(query, limit = 4)
            val end = System.nanoTime()
            totalNanoTime += (end - start)
        }

        val avgLatencyMs = (totalNanoTime.toDouble() / testQueries.size) / 1_000_000.0
        println("Average suggestion lookup latency: ${String.format("%.3f", avgLatencyMs)} ms")
        assertTrue(
            "Single suggestionsFor() call latency ($avgLatencyMs ms) must be well under 16ms 60fps frame budget",
            avgLatencyMs < 16.0
        )
    }
}
