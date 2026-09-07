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
                    val parts = trimmed.split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        val word = parts[0]
                        val freq = parts[1].toIntOrNull() ?: 1
                        trie.insert(word, freq)
                    } else if (parts.size == 1) {
                        trie.insert(parts[0], 1)
                    }
                }
            }
        }
        return trie
    }
}
