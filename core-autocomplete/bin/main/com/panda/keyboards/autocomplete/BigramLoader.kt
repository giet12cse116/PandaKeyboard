package com.panda.keyboards.autocomplete

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loader utility for reading bigram dataset text files off classpath resources into a [BigramModel].
 */
object BigramLoader {

    /**
     * Load bundled bigram dataset resource formatted as `prev_word next_word frequency`.
     *
     * @param resourcePath Classpath path to the bigram text dataset.
     * @return Initialized [BigramModel].
     */
    fun loadFromResource(resourcePath: String = "/bigrams_en.txt"): BigramModel {
        val rawPairsMap = HashMap<String, MutableList<WordFrequency>>()
        val inputStream = BigramLoader::class.java.getResourceAsStream(resourcePath)
            ?: return BigramModel(emptyMap())

        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            reader.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    val parts = trimmed.split("\\s+".toRegex())
                    if (parts.size >= 2) {
                        val prev = parts[0].lowercase()
                        val next = parts[1].lowercase()
                        val freq = if (parts.size >= 3) parts[2].toIntOrNull() ?: 1 else 1
                        val list = rawPairsMap.getOrPut(prev) { ArrayList() }
                        list.add(WordFrequency(next, freq))
                    }
                }
            }
        }

        // Sort candidates for each previous word by frequency descending
        val sortedMap = rawPairsMap.mapValues { (_, list) ->
            list.sortedByDescending { it.frequency }
        }

        return BigramModel(sortedMap)
    }
}
