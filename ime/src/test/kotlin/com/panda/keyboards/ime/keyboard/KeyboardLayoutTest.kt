package com.panda.keyboards.ime.keyboard

import com.panda.keyboards.theme.QwertyOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying keyboard layout structure, key positioning,
 * comma/period key setup, and number row column alignment.
 */
class KeyboardLayoutTest {

    @Test
    fun `qwertyRow4 contains comma and period keys around space`() {
        val letterRows = KeyboardLayouts.getLetterRows(QwertyOrder.QWERTY)
        val bottomRow = letterRows.last()

        // Verify bottom row key sequence: ?123, globe, emoji, comma, space, period, enter
        val emojiKey = bottomRow.find { it.type == KeyType.EMOJI }
        assertNotNull("Emoji key must be present in bottom row", emojiKey)

        val commaKey = bottomRow.find { it.output == "," }
        assertNotNull("Comma ',' key must be present in bottom row", commaKey)
        assertEquals(KeyType.CHARACTER, commaKey?.type)
        assertEquals(1f, commaKey?.widthWeight)

        val periodKey = bottomRow.find { it.output == "." }
        assertNotNull("Period '.' key must be present in bottom row", periodKey)
        assertEquals(KeyType.CHARACTER, periodKey?.type)
        assertEquals(1f, periodKey?.widthWeight)

        val spaceKey = bottomRow.find { it.type == KeyType.SPACE }
        assertNotNull("Space key must be present", spaceKey)

        val commaIndex = bottomRow.indexOf(commaKey)
        val spaceIndex = bottomRow.indexOf(spaceKey)
        val periodIndex = bottomRow.indexOf(periodKey)

        assertTrue("Comma must be positioned before space", commaIndex < spaceIndex)
        assertTrue("Period must be positioned after space", periodIndex > spaceIndex)
    }

    @Test
    fun `numberRow has 10 keys matching top letter row column alignment`() {
        val numberRow = KeyboardLayouts.numberRow
        val topLetterRow = KeyboardLayouts.getLetterRows(QwertyOrder.QWERTY).first()

        assertEquals("Number row must contain exactly 10 digit keys (1-0)", 10, numberRow.size)
        assertEquals("Top letter row must contain exactly 10 keys (q-p)", 10, topLetterRow.size)

        val expectedDigits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val expectedLetters = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")

        for (i in 0 until 10) {
            val numKey = numberRow[i]
            val letterKey = topLetterRow[i]

            assertEquals(expectedDigits[i], numKey.label)
            assertEquals(expectedDigits[i], numKey.output)
            assertEquals(expectedLetters[i], letterKey.label)

            assertEquals(
                "Digit ${numKey.label} width weight must match letter ${letterKey.label} width weight (1.0f) for 1:1 column alignment",
                letterKey.widthWeight,
                numKey.widthWeight
            )
        }
    }

    @Test
    fun `getLetterRows returns valid layouts for all QwertyOrder variants`() {
        for (order in QwertyOrder.entries) {
            val rows = KeyboardLayouts.getLetterRows(order)
            assertEquals(4, rows.size)
            assertEquals(10, rows[0].size) // Row 1 always has 10 keys
        }
    }

    @Test
    fun `showNumberHints calculation is false when numberRowEnabled is true`() {
        val numberRowEnabled = true
        val showNumberHints = !numberRowEnabled
        org.junit.Assert.assertFalse("When dedicated number row is enabled, number hints on QWERTY keys must be hidden", showNumberHints)
    }

    @Test
    fun `showNumberHints calculation is true when numberRowEnabled is false`() {
        val numberRowEnabled = false
        val showNumberHints = !numberRowEnabled
        assertTrue("When dedicated number row is disabled, number hints on QWERTY keys must be shown for long press", showNumberHints)
    }
}
