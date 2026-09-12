package com.panda.keyboards.autocomplete

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility helper for loading dictionary text files into a [WordTrie].
 */
object DictionaryLoader {

    /**
     * Load dictionary datasets from classpath resources line-by-line into a new [WordTrie].
     *
     * Expected file formats per line:
     * - `word frequency` (e.g. `hello 2100`)
     * - `word frequency isProperNounFlag` (e.g. `London 2500 1` or `Nike 1800 true`)
     */
    fun loadFromResource(): WordTrie {
        return loadFromResource("/dictionary_en.txt", "/dictionary_proper_nouns.txt")
    }

    fun loadFromResource(vararg resourcePaths: String): WordTrie {
        val paths = if (resourcePaths.isEmpty()) arrayOf("/dictionary_en.txt", "/dictionary_proper_nouns.txt") else resourcePaths
        val trie = WordTrie()
        for (path in paths) {
            val inputStream = DictionaryLoader::class.java.getResourceAsStream(path) ?: continue

            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        val parts = trimmed.split(Regex("\\s+"))
                        if (parts.isNotEmpty()) {
                            val lastPartInt = parts.last().toIntOrNull()
                            val secondLastInt = if (parts.size >= 2) parts[parts.size - 2].toIntOrNull() else null

                            val (rawWord, freq, isProper) = when {
                                parts.size >= 3 && secondLastInt != null && (parts.last() == "0" || parts.last() == "1" || parts.last().equals("true", ignoreCase = true) || parts.last().equals("false", ignoreCase = true)) -> {
                                    Triple(
                                        parts.subList(0, parts.size - 2).joinToString(" "),
                                        secondLastInt,
                                        parts.last() == "1" || parts.last().equals("true", ignoreCase = true)
                                    )
                                }
                                parts.size >= 2 && lastPartInt != null -> {
                                    Triple(
                                        parts.subList(0, parts.size - 1).joinToString(" "),
                                        lastPartInt,
                                        path.contains("proper_noun")
                                    )
                                }
                                else -> {
                                    Triple(
                                        trimmed,
                                        1,
                                        path.contains("proper_noun")
                                    )
                                }
                            }
                            trie.insert(rawWord, freq, isProperNoun = isProper)
                        }
                    }
                }
            }
        }
        return trie
    }
}
