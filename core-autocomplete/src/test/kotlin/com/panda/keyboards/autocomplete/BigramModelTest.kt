package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BigramModelTest {

    private lateinit var bigramModel: BigramModel

    @Before
    fun setUp() {
        bigramModel = BigramLoader.loadFromResource("/bigrams_en.txt")
    }

    @Test
    fun nextWordsFor_returnsCorrectRankedCandidates() {
        val candidates = bigramModel.nextWordsFor("could", limit = 4)
        assertTrue(candidates.isNotEmpty())
        assertEquals("you", candidates[0])
        assertTrue(candidates.contains("be"))
    }

    @Test
    fun nextWordsFor_isCaseInsensitive() {
        val lowerCandidates = bigramModel.nextWordsFor("thank", limit = 4)
        val upperCandidates = bigramModel.nextWordsFor("THANK", limit = 4)
        val titleCandidates = bigramModel.nextWordsFor("Thank", limit = 4)

        assertEquals(lowerCandidates, upperCandidates)
        assertEquals(lowerCandidates, titleCandidates)
        assertTrue(lowerCandidates.contains("you"))
    }

    @Test
    fun containsPreviousWord_returnsTrueForKnownWord() {
        assertTrue(bigramModel.containsPreviousWord("good"))
        assertTrue(bigramModel.containsPreviousWord("GOOD"))
        assertFalse(bigramModel.containsPreviousWord("nonexistentwordxyz"))
    }

    @Test
    fun nextWordsFor_respectsLimit() {
        val candidates = bigramModel.nextWordsFor("good", limit = 2)
        assertEquals(2, candidates.size)
    }
}
