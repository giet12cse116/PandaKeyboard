package com.panda.keyboards.autocomplete

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility helper for loading dictionary text files into a [WordTrie].
 */
object DictionaryLoader {

    /**
     * Load dictionary dataset from a classpath resource line-by-line into a new [WordTrie].
     *
     * Expected file format per line:
     * `word frequency` (e.g. `hello 2100`)
     */
    fun loadFromResource(resourcePath: String = "/dictionary_en.txt"): WordTrie {
        val trie = WordTrie()
        val inputStream = DictionaryLoader::class.java.getResourceAsStream(resourcePath)
            ?: return trie

        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            reader.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val spaceIdx = trimmed.indexOf(' ')
                    if (spaceIdx > 0) {
                        val word = trimmed.substring(0, spaceIdx)
                        val freq = trimmed.substring(spaceIdx + 1).toIntOrNull() ?: 1
                        trie.insert(word, freq)
                    } else {
                        trie.insert(trimmed, 1)
                    }
                }
            }
        }
        return trie
    }
}
