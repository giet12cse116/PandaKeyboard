package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordTrieTest {

    private lateinit var trie: WordTrie

    @Before
    fun setUp() {
        trie = WordTrie()
    }

    @Test
    fun insertAndContains_findsInsertedWords() {
        trie.insert("hello", 100)
        trie.insert("help", 90)

        assertTrue(trie.contains("hello"))
        assertTrue(trie.contains("help"))
        assertFalse(trie.contains("hel"))
        assertFalse(trie.contains("world"))
    }

    @Test
    fun getFrequency_returnsCorrectRank() {
        trie.insert("keyboard", 500)
        trie.insert("key", 200)

        assertEquals(500, trie.getFrequency("keyboard"))
        assertEquals(200, trie.getFrequency("key"))
        assertEquals(0, trie.getFrequency("missing"))
    }

    @Test
    fun wordsWithPrefix_returnsCandidatesInFrequencyDescendingOrder() {
        trie.insert("helicopter", 50)
        trie.insert("hello", 1000)
        trie.insert("help", 500)

        val results = trie.wordsWithPrefix("hel", limit = 3)

        assertEquals(3, results.size)
        assertEquals("hello", results[0].word)
        assertEquals(1000, results[0].frequency)
        assertEquals("help", results[1].word)
        assertEquals(500, results[1].frequency)
        assertEquals("helicopter", results[2].word)
        assertEquals(50, results[2].frequency)
    }

    @Test
    fun wordsWithPrefix_respectsLimit() {
        trie.insert("apple", 10)
        trie.insert("application", 20)
        trie.insert("apply", 30)

        val results = trie.wordsWithPrefix("app", limit = 2)

        assertEquals(2, results.size)
        assertEquals("apply", results[0].word)
        assertEquals("application", results[1].word)
    }

    @Test
    fun wordsWithPrefix_emptyOrInvalidPrefix_returnsEmptyList() {
        trie.insert("test", 10)

        assertTrue(trie.wordsWithPrefix("").isEmpty())
        assertTrue(trie.wordsWithPrefix("xyz").isEmpty())
    }

    @Test
    fun dictionaryLoader_loadsResourceCorrectly() {
        val loadedTrie = DictionaryLoader.loadFromResource()
        assertTrue("Dictionary trie should contain words", loadedTrie.size > 50)
        assertTrue(loadedTrie.contains("the"))
        assertTrue(loadedTrie.contains("hello"))
        assertTrue(loadedTrie.contains("keyboard"))
    }
}
