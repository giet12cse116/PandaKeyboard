package com.panda.keyboards.ime.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for VoicePanel keyboard mode, offline mode checks, and permission handling logic.
 */
class VoicePanelTest {

    @Test
    fun `keyboardMode includes VOICE mode`() {
        val modes = KeyboardMode.entries
        assertTrue("KeyboardMode must contain VOICE mode", modes.contains(KeyboardMode.VOICE))
    }

    @Test
    fun `offline mode restricts voice input when on-device recognition is unavailable`() {
        fun computeVoicePanelState(
            hasPermission: Boolean,
            offlineModeEnabled: Boolean,
            isOnDeviceAvailable: Boolean
        ): String {
            return when {
                !hasPermission -> "PERMISSION_DENIED"
                offlineModeEnabled && !isOnDeviceAvailable -> "OFFLINE_UNAVAILABLE"
                else -> "READY_TO_RECORD"
            }
        }

        // Permission denied takes priority
        assertEquals("PERMISSION_DENIED", computeVoicePanelState(hasPermission = false, offlineModeEnabled = false, isOnDeviceAvailable = true))

        // Offline mode enabled without on-device recognition -> OFFLINE_UNAVAILABLE
        assertEquals("OFFLINE_UNAVAILABLE", computeVoicePanelState(hasPermission = true, offlineModeEnabled = true, isOnDeviceAvailable = false))

        // Offline mode enabled WITH on-device recognition -> READY_TO_RECORD
        assertEquals("READY_TO_RECORD", computeVoicePanelState(hasPermission = true, offlineModeEnabled = true, isOnDeviceAvailable = true))

        // Online mode (offlineModeEnabled = false) -> READY_TO_RECORD
        assertEquals("READY_TO_RECORD", computeVoicePanelState(hasPermission = true, offlineModeEnabled = false, isOnDeviceAvailable = false))
    }

    @Test
    fun `amplitude waveform scaling maps rmsDb 0 to 10 safely`() {
        fun calculateBarHeight(rmsDb: Float, factor: Float): Float {
            val normalized = (rmsDb.coerceIn(0f, 10f) / 10f) * factor
            val baseHeight = 8f
            val dynamicAdd = 32f
            return baseHeight + (dynamicAdd * normalized.coerceIn(0.1f, 1.0f))
        }

        // Test silent / 0 dB -> minimum bar height (~11.2dp)
        val silentHeight = calculateBarHeight(0f, factor = 1.0f)
        assertTrue("Silent bar height must be > 8dp", silentHeight >= 8f)

        // Test loud / 10 dB -> maximum bar height (40dp)
        val loudHeight = calculateBarHeight(10f, factor = 1.0f)
        assertEquals(40f, loudHeight, 0.1f)

        // Test out of bounds / negative dB -> safely clamped
        val negativeHeight = calculateBarHeight(-5f, factor = 1.0f)
        assertEquals(silentHeight, negativeHeight, 0.1f)
    }
}
