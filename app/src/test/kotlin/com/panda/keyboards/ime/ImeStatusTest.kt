package com.panda.keyboards.ime

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ImeStatus] state logic.
 */
class ImeStatusTest {

    @Test
    fun `isFullyConfigured is false when only enabled`() {
        val status = ImeStatus(isEnabled = true, isDefault = false)
        assertFalse(status.isFullyConfigured)
    }

    @Test
    fun `isFullyConfigured is false when only default`() {
        val status = ImeStatus(isEnabled = false, isDefault = true)
        assertFalse(status.isFullyConfigured)
    }

    @Test
    fun `isFullyConfigured is true when both enabled and default`() {
        val status = ImeStatus(isEnabled = true, isDefault = true)
        assertTrue(status.isFullyConfigured)
    }
}
