package com.panda.keyboards.autocomplete

import java.util.concurrent.ConcurrentHashMap

/**
 * Interface tracking personalized words typed or explicitly committed by the user.
 */
interface UserDictionary {
    /** Record a word commitment by the user, incrementing its usage count. */
    fun recordWord(word: String)

    /** Retrieve the current usage count for a user word. */
    fun getFrequency(word: String): Int

    /** Find user words starting with [prefix]. */
    fun wordsWithPrefix(prefix: String): List<WordFrequency>

    /** Reset/clear all recorded user words. */
    fun clear()
}

/**
 * In-memory thread-safe implementation of [UserDictionary].
 */
class InMemoryUserDictionary : UserDictionary {

    private val wordMap = ConcurrentHashMap<String, Int>()

    override fun recordWord(word: String) {
        val normalized = word.trim().lowercase()
        if (normalized.length >= 2) {
            wordMap.compute(normalized) { _, count -> (count ?: 0) + 1 }
        }
    }

    override fun getFrequency(word: String): Int {
        val normalized = word.trim().lowercase()
        return wordMap[normalized] ?: 0
    }

    override fun wordsWithPrefix(prefix: String): List<WordFrequency> {
        val normalized = prefix.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()

        return wordMap.entries
            .asSequence()
            .filter { it.key.startsWith(normalized) }
            .map { WordFrequency(it.key, it.value) }
            .sorted()
            .toList()
    }

    override fun clear() {
        wordMap.clear()
    }
}
