package com.panda.keyboards.ime.autocomplete

import com.panda.keyboards.autocomplete.DictionaryLoader
import com.panda.keyboards.autocomplete.InMemoryUserDictionary
import com.panda.keyboards.autocomplete.SuggestionEngine
import com.panda.keyboards.autocomplete.UserDictionary
import com.panda.keyboards.autocomplete.WordTrie

/**
 * Process-lifetime manager for word autocomplete suggestions and personalization.
 *
 * Loads the bundled English dictionary [WordTrie] once per IME process lifetime
 * off the main thread and manages user-learned words via [UserDictionary].
 */
object SuggestionManager {

    /** Lazy, process-lifetime bundled dictionary Trie. */
    val trie: WordTrie by lazy {
        DictionaryLoader.loadFromResource("/dictionary_en.txt")
    }

    /** In-memory personalized user word dictionary. */
    val userDictionary: UserDictionary by lazy {
        InMemoryUserDictionary()
    }

    /** Main suggestion engine blending static dictionary with user words. */
    val engine: SuggestionEngine by lazy {
        SuggestionEngine(
            trie = trie,
            userDictionary = userDictionary
        )
    }

    /**
     * Get up to [limit] completion suggestions for [currentWord].
     */
    fun getSuggestions(currentWord: String, limit: Int = 4): List<String> {
        return engine.suggestionsFor(currentWord, limit)
    }

    /**
     * Record a user-committed word for personalized autocomplete learning.
     */
    fun recordLearnedWord(word: String) {
        val cleaned = com.panda.keyboards.fonts.FontTransformer.normalizeToAscii(word.trim()).trim { !it.isLetterOrDigit() }
        if (cleaned.length >= 2) {
            userDictionary.recordWord(cleaned)
        }
    }

    /**
     * Extract the in-progress word being typed immediately before the cursor.
     *
     * Scans backward from the cursor until encountering a whitespace, punctuation,
     * or newline boundary. Returns `""` if the cursor is preceded by a word boundary.
     */
    fun extractCurrentWord(textBeforeCursor: String?): String {
        if (textBeforeCursor.isNullOrEmpty()) return ""
        val normalized = com.panda.keyboards.fonts.FontTransformer.normalizeToAscii(textBeforeCursor)

        val lastChar = normalized.last()
        if (isWordBoundary(lastChar)) {
            return ""
        }

        var startIndex = normalized.length - 1
        while (startIndex >= 0 && !isWordBoundary(normalized[startIndex])) {
            startIndex--
        }

        return normalized.substring(startIndex + 1)
    }

    /**
     * Determine if a character represents a word boundary (whitespace or punctuation).
     */
    fun isWordBoundary(ch: Char): Boolean {
        return ch.isWhitespace() || ch in PUNCTUATION_SET
    }

    private val PUNCTUATION_SET = setOf(
        '.', ',', '!', '?', ';', ':', '"', '\'', '(', ')', '[', ']', '{', '}', '-', '/', '\\', '@', '#', '$', '%', '^', '&', '*', '+', '=', '<', '>', '|', '~', '`'
    )
}
