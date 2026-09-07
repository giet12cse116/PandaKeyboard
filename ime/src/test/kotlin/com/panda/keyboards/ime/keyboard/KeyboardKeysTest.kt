package com.panda.keyboards.ime.keyboard

import com.panda.keyboards.theme.QwertyOrder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [KeyboardLayouts] layout selections based on [QwertyOrder].
 */
class KeyboardKeysTest {

    @Test
    fun `getLetterRows returns standard QWERTY layout`() {
        val rows = KeyboardLayouts.getLetterRows(QwertyOrder.QWERTY)
        assertEquals("q", rows[0][0].label)
        assertEquals("w", rows[0][1].label)
        assertEquals("y", rows[0][5].label)
        assertEquals("z", rows[2][1].label)
    }

    @Test
    fun `getLetterRows returns QWERTZ layout for German`() {
        val rows = KeyboardLayouts.getLetterRows(QwertyOrder.QWERTZ)
        assertEquals("q", rows[0][0].label)
        assertEquals("w", rows[0][1].label)
        assertEquals("z", rows[0][5].label)
        assertEquals("y", rows[2][1].label)
    }

    @Test
    fun `getLetterRows returns AZERTY layout for French`() {
        val rows = KeyboardLayouts.getLetterRows(QwertyOrder.AZERTY)
        assertEquals("a", rows[0][0].label)
        assertEquals("z", rows[0][1].label)
        assertEquals("q", rows[1][0].label)
        assertEquals("w", rows[2][1].label)
    }
}
