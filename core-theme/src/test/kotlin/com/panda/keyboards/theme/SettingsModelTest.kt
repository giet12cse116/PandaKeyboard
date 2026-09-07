package com.panda.keyboards.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [KeyboardSettings], [QwertyOrder], and [KeyboardHeight] data models.
 */
class SettingsModelTest {

    @Test
    fun `default KeyboardSettings has expected default values`() {
        val settings = KeyboardSettings()
        assertEquals(QwertyOrder.QWERTY, settings.qwertyOrder)
        assertEquals(KeyboardHeight.DEFAULT, settings.keyboardHeight)
        assertTrue(settings.autoCorrectionEnabled)
        assertFalse(settings.offlineModeEnabled)
        assertFalse(settings.numberRowEnabled)
    }

    @Test
    fun `QwertyOrder entries have correct display names`() {
        assertEquals("QWERTY (US/Global)", QwertyOrder.QWERTY.displayName)
        assertEquals("QWERTZ (German/Central European)", QwertyOrder.QWERTZ.displayName)
        assertEquals("AZERTY (French/Belgian)", QwertyOrder.AZERTY.displayName)
    }

    @Test
    fun `KeyboardHeight entries have correct heightDp values`() {
        assertEquals(220, KeyboardHeight.COMPACT.heightDp)
        assertEquals(260, KeyboardHeight.DEFAULT.heightDp)
        assertEquals(300, KeyboardHeight.TALL.heightDp)
    }

    @Test
    fun `KeyboardSettings copy modifies specific fields correctly`() {
        val initial = KeyboardSettings()
        val modified = initial.copy(
            qwertyOrder = QwertyOrder.QWERTZ,
            keyboardHeight = KeyboardHeight.TALL,
            autoCorrectionEnabled = false,
            offlineModeEnabled = true,
            numberRowEnabled = true
        )

        assertEquals(QwertyOrder.QWERTZ, modified.qwertyOrder)
        assertEquals(KeyboardHeight.TALL, modified.keyboardHeight)
        assertFalse(modified.autoCorrectionEnabled)
        assertTrue(modified.offlineModeEnabled)
        assertTrue(modified.numberRowEnabled)
    }

}
