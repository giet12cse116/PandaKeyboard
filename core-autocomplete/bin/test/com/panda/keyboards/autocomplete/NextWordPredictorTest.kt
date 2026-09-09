package com.panda.keyboards.autocomplete

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NextWordPredictorTest {

    private lateinit var bigramModel: BigramModel
    private lateinit var personalStore: PersonalNgramStore
    private lateinit var predictor: NextWordPredictor

    @Before
    fun setUp() {
        bigramModel = BigramLoader.loadFromResource("/bigrams_en.txt")
        personalStore = PersonalNgramStore(maxPairs = 100)
        predictor = NextWordPredictor(bigramModel, personalStore)
    }

    @Test
    fun predictNextWords_usesGenericBigramsWhenNoPersonalHistory() {
        val predictions = predictor.predictNextWords("thanks", limit = 4)
        assertTrue(predictions.isNotEmpty())
        assertEquals("for", predictions[0])
    }

    @Test
    fun predictNextWords_favorsStrongPersonalSignalOverGenericBigrams() {
        // Record "thanks" -> "buddy" multiple times
        predictor.recordTransition("thanks", "buddy")
        predictor.recordTransition("thanks", "buddy")

        val predictions = predictor.predictNextWords("thanks", limit = 4)
        assertEquals("buddy", predictions[0])
    }

    @Test
    fun learningEnabled_toggleDisablesPersonalHistoryRecording() {
        predictor.learningEnabled = false
        predictor.recordTransition("thanks", "mate")

        val predictions = predictor.predictNextWords("thanks", limit = 4)
        assertFalse(predictions.contains("mate"))
    }

    @Test
    fun clearHistory_removesPersonalSignalAndRevertsToGenericBigrams() {
        predictor.recordTransition("thanks", "pal")
        assertEquals("pal", predictor.predictNextWords("thanks", limit = 4)[0])

        predictor.clearHistory()
        assertEquals("for", predictor.predictNextWords("thanks", limit = 4)[0])
    }
}
