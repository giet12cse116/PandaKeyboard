package com.panda.keyboards.ime

import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying surrogate pair detection and transformed Unicode backspace safety.
 */
class UnicodeBackspaceTest {

    @Test
    fun `transformed mathematical bold character is a 2-char UTF-16 surrogate pair`() {
        val transformed = FontTransformer.transform("A", FontStyle.BOLD_SERIF)
        assertEquals(2, transformed.length)
        assertTrue(Character.isSurrogatePair(transformed[0], transformed[1]))
    }

    @Test
    fun `surrogate pair detection correctly identifies 2-unit Unicode codepoints`() {
        val gothicA = FontTransformer.transform("A", FontStyle.GOTHIC)
        assertEquals(2, gothicA.length)

        val isSurrogate = gothicA.length == 2 &&
            Character.isSurrogatePair(gothicA[0], gothicA[1])
        assertTrue(isSurrogate)
    }
}
