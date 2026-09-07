package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserDictionaryTest {

    private lateinit var userDict: UserDictionary

    @Before
    fun setUp() {
        userDict = InMemoryUserDictionary()
    }

    @Test
    fun recordWord_incrementsFrequency() {
        assertEquals(0, userDict.getFrequency("customword"))

        userDict.recordWord("customword")
        assertEquals(1, userDict.getFrequency("customword"))

        userDict.recordWord("customword")
        assertEquals(2, userDict.getFrequency("customword"))
    }

    @Test
    fun wordsWithPrefix_returnsMatchingRecordedWords() {
        userDict.recordWord("panda")
        userDict.recordWord("pandabear")
        userDict.recordWord("pandabear") // freq = 2

        val matches = userDict.wordsWithPrefix("pan")

        assertEquals(2, matches.size)
        assertEquals("pandabear", matches[0].word)
        assertEquals(2, matches[0].frequency)
        assertEquals("panda", matches[1].word)
        assertEquals(1, matches[1].frequency)
    }

    @Test
    fun clear_resetsAllRecordedWords() {
        userDict.recordWord("testword")
        assertEquals(1, userDict.getFrequency("testword"))

        userDict.clear()
        assertEquals(0, userDict.getFrequency("testword"))
        assertTrue(userDict.wordsWithPrefix("test").isEmpty())
    }
}
