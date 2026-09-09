package com.panda.keyboards.autocomplete

import com.panda.keyboards.fonts.FontTransformer

/**
 * Data class representing a word pair key for transition tracking.
 */
data class WordPairKey(
    val previousWord: String,
    val nextWord: String
)

/**
 * On-device local store for user typing history and word-pair transition counts.
 *
 * Stores transitions entirely locally on-device. Enforces a strict upper bound ([maxPairs])
 * on total stored pairs using LRU (least-recently-reinforced) eviction.
 *
 * Exposes mandatory privacy controls:
 * - [learningEnabled] data-layer opt-out toggle (when false, [recordTransition] is a no-op).
 * - [clearHistory] action that completely wipes all stored transitions.
 *
 * @param maxPairs Maximum number of unique word-pair transitions to retain (default 2000).
 * @param learningEnabled Flag indicating whether new typing transitions are recorded (default true).
 */
class PersonalNgramStore(
    private val maxPairs: Int = 2000,
    var learningEnabled: Boolean = true
) {

    private class TransitionInfo(
        var frequency: Int,
        var lastReinforcedMs: Long
    )

    // LinkedHashMap with accessOrder = true so iteration order reflects least-recently reinforced first
    private val transitions = LinkedHashMap<WordPairKey, TransitionInfo>(128, 0.75f, true)
    private val lock = Any()

    /**
     * Record a word transition from [previousWord] to [nextWord].
     *
     * If [learningEnabled] is false, this call is a no-op.
     * Inputs are normalized to lowercase ASCII.
     */
    fun recordTransition(previousWord: String, nextWord: String) {
        if (!learningEnabled) return

        val normPrev = FontTransformer.normalizeToAscii(previousWord.trim()).lowercase()
        val normNext = FontTransformer.normalizeToAscii(nextWord.trim()).lowercase()

        if (normPrev.isEmpty() || normNext.isEmpty()) return

        val key = WordPairKey(normPrev, normNext)
        val now = System.currentTimeMillis()

        synchronized(lock) {
            val existing = transitions[key]
            if (existing != null) {
                existing.frequency += 1
                existing.lastReinforcedMs = now
                // Touch key to refresh LRU ordering position
                transitions[key]
            } else {
                // If capacity limit reached, evict least-recently-reinforced pair (head of access-ordered map)
                if (transitions.size >= maxPairs) {
                    val oldestKey = transitions.keys.iterator().next()
                    transitions.remove(oldestKey)
                }
                transitions[key] = TransitionInfo(frequency = 1, lastReinforcedMs = now)
            }
        }
    }

    /**
     * Look up candidate next words recorded for [previousWord], ordered by frequency.
     *
     * @param previousWord The previous word typed.
     * @param limit Maximum number of candidate next words to return.
     * @return List of [WordFrequency] candidates sorted by frequency descending.
     */
    fun nextWordsFor(previousWord: String, limit: Int = 4): List<WordFrequency> {
        if (previousWord.isBlank() || limit <= 0) return emptyList()

        val normPrev = FontTransformer.normalizeToAscii(previousWord.trim()).lowercase()

        return synchronized(lock) {
            transitions.entries
                .asSequence()
                .filter { it.key.previousWord == normPrev }
                .map { WordFrequency(it.key.nextWord, it.value.frequency) }
                .sortedByDescending { it.frequency }
                .take(limit)
                .toList()
        }
    }

    /**
     * Get recorded transition count for a specific pair.
     */
    fun getFrequency(previousWord: String, nextWord: String): Int {
        val normPrev = FontTransformer.normalizeToAscii(previousWord.trim()).lowercase()
        val normNext = FontTransformer.normalizeToAscii(nextWord.trim()).lowercase()
        val key = WordPairKey(normPrev, normNext)

        return synchronized(lock) {
            transitions[key]?.frequency ?: 0
        }
    }

    /**
     * Total number of unique word-pair transitions currently stored.
     */
    fun size(): Int {
        return synchronized(lock) { transitions.size }
    }

    /**
     * Completely clear all recorded typing history.
     */
    fun clearHistory() {
        synchronized(lock) {
            transitions.clear()
        }
    }
}
