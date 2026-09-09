package com.panda.keyboards.autocomplete

import com.panda.keyboards.fonts.FontTransformer

/**
 * Casing pattern detected from the user's current typed word prefix.
 */
private enum class CasingStyle {
    LOWERCASE,
    CAPITALIZED,
    ALL_UPPERCASE,
    MIXED
}

/**
 * Standalone word autocomplete & suggestion engine.
 *
 * Blends candidates from a bundled static [WordTrie] with personalized
 * user words from [UserDictionary], preserving the casing pattern of the
 * typed input prefix.
 *
 * @param trie Standard word frequency Trie.
 * @param userDictionary Optional personal user dictionary for learned words.
 * @param userWordBoostWeight Score boost added to user-learned words to ensure prominent ranking.
 */
class SuggestionEngine(
    private val trie: WordTrie,
    private val userDictionary: UserDictionary? = null,
    private val userWordBoostWeight: Int = 100_000
) {

    /**
     * Generate up to [limit] word completion suggestions for [currentWord].
     *
     * Returns an empty list if [currentWord] contains fewer than 2 characters.
     */
    fun suggestionsFor(currentWord: String, limit: Int = 4): List<String> {
        val asciiInput = FontTransformer.normalizeToAscii(currentWord.trim())
        // Threshold check: return empty list for 0-1 chars to prevent noise
        if (asciiInput.length < 2 || limit <= 0) {
            return emptyList()
        }

        val normalizedPrefix = asciiInput.lowercase()
        val casingStyle = detectCasingStyle(asciiInput)

        // 1. Fetch candidates from bundled dictionary
        val trieCandidates = trie.wordsWithPrefix(normalizedPrefix, limit * 4)

        // 2. Fetch candidates from user dictionary (if available)
        val userCandidates = userDictionary?.wordsWithPrefix(normalizedPrefix) ?: emptyList()

        // 3. Blend & weight scores
        val blendedScores = HashMap<String, Int>()

        for (cand in trieCandidates) {
            blendedScores[cand.word] = cand.frequency
        }

        for (userCand in userCandidates) {
            val currentScore = blendedScores[userCand.word] ?: 0
            // Boost user-learned words so they appear near the top
            val boostedScore = currentScore + (userCand.frequency * userWordBoostWeight)
            blendedScores[userCand.word] = boostedScore
        }

        // 4. Sort candidates by blended score descending
        val sortedWords = blendedScores.entries
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

        // 5. Apply user casing pattern to suggestions
        return sortedWords.map { word ->
            applyCasingStyle(word, casingStyle, asciiInput)
        }
    }

    private fun detectCasingStyle(text: String): CasingStyle {
        if (text.isEmpty()) return CasingStyle.LOWERCASE
        val isFirstUpper = text[0].isUpperCase()
        val isAllUpper = text.all { !it.isLetter() || it.isUpperCase() }
        val isAllLower = text.all { !it.isLetter() || it.isLowerCase() }

        return when {
            isAllUpper && text.any { it.isLetter() } -> CasingStyle.ALL_UPPERCASE
            isAllLower -> CasingStyle.LOWERCASE
            isFirstUpper && text.drop(1).all { !it.isLetter() || it.isLowerCase() } -> CasingStyle.CAPITALIZED
            else -> CasingStyle.MIXED
        }
    }

    private fun applyCasingStyle(word: String, style: CasingStyle, inputPrefix: String): String {
        return when (style) {
            CasingStyle.LOWERCASE -> word.lowercase()
            CasingStyle.ALL_UPPERCASE -> word.uppercase()
            CasingStyle.CAPITALIZED -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            CasingStyle.MIXED -> {
                // Preserve exact input prefix characters, append remainder of word
                if (word.length >= inputPrefix.length) {
                    inputPrefix + word.substring(inputPrefix.length)
                } else {
                    word
                }
            }
        }
    }
}
