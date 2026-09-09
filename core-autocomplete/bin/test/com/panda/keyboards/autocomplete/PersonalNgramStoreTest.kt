package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PersonalNgramStoreTest {

    private lateinit var store: PersonalNgramStore

    @Before
    fun setUp() {
        store = PersonalNgramStore(maxPairs = 5)
    }

    @Test
    fun recordTransition_reinforcesRepeatedPairs() {
        assertEquals(0, store.getFrequency("good", "vibes"))

        store.recordTransition("good", "vibes")
        assertEquals(1, store.getFrequency("good", "vibes"))

        store.recordTransition("good", "vibes")
        assertEquals(2, store.getFrequency("good", "vibes"))
    }

    @Test
    fun recordTransition_respectsLearningEnabledToggle() {
        store.learningEnabled = false
        store.recordTransition("hello", "friend")

        assertEquals(0, store.getFrequency("hello", "friend"))
        assertEquals(0, store.size())

        store.learningEnabled = true
        store.recordTransition("hello", "friend")
        assertEquals(1, store.getFrequency("hello", "friend"))
    }

    @Test
    fun clearHistory_wipesAllTransitions() {
        store.recordTransition("nice", "day")
        store.recordTransition("good", "luck")
        assertEquals(2, store.size())

        store.clearHistory()
        assertEquals(0, store.size())
        assertEquals(0, store.getFrequency("nice", "day"))
    }

    @Test
    fun capAndEviction_evictsLeastRecentlyReinforcedUnderLoad() {
        // Cap is 5
        store.recordTransition("word1", "next1")
        Thread.sleep(2)
        store.recordTransition("word2", "next2")
        Thread.sleep(2)
        store.recordTransition("word3", "next3")
        Thread.sleep(2)
        store.recordTransition("word4", "next4")
        Thread.sleep(2)
        store.recordTransition("word5", "next5")

        assertEquals(5, store.size())
        assertEquals(1, store.getFrequency("word1", "next1"))

        // Access/reinforce word1 again so it becomes recently used
        store.recordTransition("word1", "next1")

        // Now record a 6th unique pair ("word6", "next6"), pushing store over capacity (5)
        store.recordTransition("word6", "next6")

        assertEquals(5, store.size())
        // word1 was recently reinforced so it must remain
        assertEquals(2, store.getFrequency("word1", "next1"))
        // word2 was the least recently used, so it must have been evicted
        assertEquals(0, store.getFrequency("word2", "next2"))
        // word6 must exist
        assertEquals(1, store.getFrequency("word6", "next6"))
    }
}
