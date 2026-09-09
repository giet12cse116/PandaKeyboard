package com.panda.keyboards.autocomplete

/**
 * Data holder for a word candidate and its corresponding frequency rank.
 */
data class WordFrequency(
    val word: String,
    val frequency: Int
) : Comparable<WordFrequency> {
    override fun compareTo(other: WordFrequency): Int {
        // Order by frequency descending; secondary order alphabetical ascending
        val freqCompare = other.frequency.compareTo(this.frequency)
        return if (freqCompare != 0) freqCompare else this.word.compareTo(other.word)
    }
}

/**
 * Efficient Prefix Trie implementation for fast word completion lookups ordered by frequency.
 *
 * Designed to operate in a pure JVM/Kotlin environment with minimal memory overhead.
 */
class WordTrie {

    private class TrieNode(val char: Char) {
        val children: MutableMap<Char, TrieNode> = HashMap()
        var frequency: Int = 0
        var isWord: Boolean = false
        var fullWord: String? = null
    }

    private val root = TrieNode(char = '\u0000')
    private var _wordCount = 0

    /**
     * Total number of distinct words stored in the Trie.
     */
    val size: Int
        get() = _wordCount

    /**
     * Insert a word with a frequency rank into the Trie.
     * Always converts the word to lowercase for consistent prefix matching.
     */
    fun insert(word: String, frequency: Int) {
        if (word.isBlank()) return
        val normalized = word.trim().lowercase()

        var current = root
        for (ch in normalized) {
            current = current.children.getOrPut(ch) { TrieNode(ch) }
        }

        if (!current.isWord) {
            current.isWord = true
            _wordCount++
        }
        current.frequency = frequency
        current.fullWord = normalized
    }

    /**
     * Check if an exact word exists in the Trie.
     */
    fun contains(word: String): Boolean {
        val node = findNode(word.trim().lowercase())
        return node?.isWord == true
    }

    /**
     * Retrieve the frequency score of an exact word, or 0 if not present.
     */
    fun getFrequency(word: String): Int {
        val node = findNode(word.trim().lowercase())
        return if (node?.isWord == true) node.frequency else 0
    }

    /**
     * Find all candidate words starting with [prefix], sorted by frequency descending.
     * Returns up to [limit] top matching candidates.
     */
    fun wordsWithPrefix(prefix: String, limit: Int = 3): List<WordFrequency> {
        if (prefix.isBlank() || limit <= 0) return emptyList()
        val normalized = prefix.trim().lowercase()

        val prefixNode = findNode(normalized) ?: return emptyList()

        val candidates = mutableListOf<WordFrequency>()
        collectWords(prefixNode, candidates)

        return candidates
            .sorted()
            .take(limit)
    }

    private fun findNode(prefix: String): TrieNode? {
        var current = root
        for (ch in prefix) {
            current = current.children[ch] ?: return null
        }
        return current
    }

    private fun collectWords(node: TrieNode, result: MutableList<WordFrequency>) {
        if (node.isWord && node.fullWord != null) {
            result.add(WordFrequency(node.fullWord!!, node.frequency))
        }
        for (child in node.children.values) {
            collectWords(child, result)
        }
    }
}
