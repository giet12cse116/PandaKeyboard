package com.panda.keyboards.ime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VoicePermissionTest {

    @Test
    fun `voicePermissionActivity constants are properly defined`() {
        assertEquals(2001, VoicePermissionActivity.PERMISSION_REQUEST_CODE_AUDIO)
    }

    @Test
    fun `voicePermissionActivity can be instantiated`() {
        val activity = VoicePermissionActivity()
        assertNotNull("VoicePermissionActivity instance should be created", activity)
    }
}
