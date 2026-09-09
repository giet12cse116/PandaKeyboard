package com.panda.keyboards.autocomplete

import com.panda.keyboards.fonts.FontTransformer

/**
 * Bundled general-language bigram (word pair) frequency model.
 * Maps a completed previous word to a ranked list of likely following words.
 *
 * @param pairs Map of lowercased previous words to lists of candidate [WordFrequency] pairs.
 */
class BigramModel(
    private val pairs: Map<String, List<WordFrequency>>
) {

    /**
     * Look up the most likely following words for [previousWord].
     *
     * Performs a case-insensitive, normalized lookup.
     *
     * @param previousWord The last completed word typed by the user.
     * @param limit Maximum number of candidates to return (default 4).
     * @return List of candidate next words ranked by frequency descending.
     */
    fun nextWordsFor(previousWord: String, limit: Int = 4): List<String> {
        return nextWordFrequenciesFor(previousWord, limit).map { it.word }
    }

    /**
     * Look up candidate next words with their frequency ranks for [previousWord].
     *
     * @param previousWord The last completed word typed by the user.
     * @param limit Maximum number of candidates to return (default 4).
     * @return List of [WordFrequency] candidates sorted by frequency descending.
     */
    fun nextWordFrequenciesFor(previousWord: String, limit: Int = 4): List<WordFrequency> {
        if (previousWord.isBlank() || limit <= 0) return emptyList()
        val normalizedPrev = FontTransformer.normalizeToAscii(previousWord.trim()).lowercase()
        val matches = pairs[normalizedPrev] ?: return emptyList()
        return matches.take(limit)
    }

    /**
     * Check if the model contains any word-pair transitions for [previousWord].
     */
    fun containsPreviousWord(previousWord: String): Boolean {
        if (previousWord.isBlank()) return false
        val normalizedPrev = FontTransformer.normalizeToAscii(previousWord.trim()).lowercase()
        return pairs.containsKey(normalizedPrev)
    }
}
