package com.panda.keyboards.theme

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [FontPreferencesRepository] static constants and defaults.
 */
class FontPreferencesRepositoryTest {

    @Test
    fun `default font style constant is NORMAL`() {
        assertEquals("NORMAL", FontPreferencesRepository.DEFAULT_STYLE_ID)
    }

    @Test
    fun `repository instantiation with null context yields default flow values`() {
        val repository = FontPreferencesRepository(context = null)
        val defaultStyleId = runBlocking {
            repository.selectedStyleId.first()
        }
        assertEquals("NORMAL", defaultStyleId)
    }
}
