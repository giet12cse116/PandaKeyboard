package com.panda.keyboards.autocomplete

import com.panda.keyboards.fonts.FontTransformer

/**
 * Next-word prediction engine for Panda Keyboards.
 *
 * Blends a bundled static [BigramModel] (general language frequency) with a local
 * [PersonalNgramStore] (user typing history). Personal history signal is weighted higher
 * than generic bigrams so learned user habits (e.g., "thanks" -> "buddy") outrank generic completions.
 *
 * Fully on-device and privacy-preserving with zero network activity.
 *
 * @param bigramModel Bundled general-language bigram model.
 * @param personalStore On-device store for personal n-gram transitions.
 * @param personalBoostWeight Score multiplier applied to personal transition frequency counts.
 */
class NextWordPredictor(
    private val bigramModel: BigramModel,
    private val personalStore: PersonalNgramStore = PersonalNgramStore(),
    private val personalBoostWeight: Int = 10_000
) {

    /**
     * Data-layer privacy control flag for enabling or disabling history learning.
     */
    var learningEnabled: Boolean
        get() = personalStore.learningEnabled
        set(value) {
            personalStore.learningEnabled = value
        }

    /**
     * Record a completed word pair transition into user history.
     */
    fun recordTransition(previousWord: String, nextWord: String) {
        personalStore.recordTransition(previousWord, nextWord)
    }

    /**
     * Predict up to [limit] candidate next words following [previousWord].
     *
     * @param previousWord The completed previous word.
     * @param limit Maximum number of suggestion candidates to return (default 4).
     * @return Ranked list of predicted next words.
     */
    fun predictNextWords(previousWord: String, limit: Int = 4): List<String> {
        if (previousWord.isBlank() || limit <= 0) return emptyList()

        val normalizedPrev = FontTransformer.normalizeToAscii(previousWord.trim()).lowercase()
        val scores = HashMap<String, Int>()

        // 1. Fetch generic bigram model candidates
        val genericBigrams = bigramModel.nextWordFrequenciesFor(normalizedPrev, limit * 4)
        for (cand in genericBigrams) {
            scores[cand.word] = cand.frequency
        }

        // 2. Fetch personalized user history candidates
        val personalCandidates = personalStore.nextWordsFor(normalizedPrev, limit * 4)
        for (cand in personalCandidates) {
            val baseScore = scores[cand.word] ?: 0
            val boostedScore = baseScore + (cand.frequency * personalBoostWeight)
            scores[cand.word] = boostedScore
        }

        if (scores.isEmpty()) return emptyList()

        // 3. Rank candidates by score descending, breaking ties alphabetically
        val sortedWords = scores.entries
            .asSequence()
            .sortedWith(
                Comparator { a, b ->
                    val scoreCompare = b.value.compareTo(a.value)
                    if (scoreCompare != 0) scoreCompare else a.key.compareTo(b.key)
                }
            )
            .map { it.key }
            .take(limit)
            .toList()

        return sortedWords
    }

    /**
     * Completely clear all recorded typing history from the personal n-gram store.
     */
    fun clearHistory() {
        personalStore.clearHistory()
    }
}
