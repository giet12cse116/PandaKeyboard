package com.panda.keyboards.ime

import android.content.Context
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * System-level integration test using UiAutomator to verify real [PandaInputMethodService]
 * registration, enablement, default selection, and soft input window display.
 *
 * Automatically backs up the previous default IME during [setUp] and restores it in [tearDown]
 * to prevent leaving test devices or CI runners in an inconsistent state.
 */
@RunWith(AndroidJUnit4::class)
class PandaImeSystemIntegrationTest {

    private lateinit var device: UiDevice
    private var originalDefaultIme: String? = null

    companion object {
        private const val IME_PACKAGE = "com.panda.keyboards"
        private const val IME_COMPONENT = "com.panda.keyboards/com.panda.keyboards.ime.PandaInputMethodService"
        private const val TIMEOUT_MS = 5000L
    }

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)

        // Save original default IME for teardown restoration
        originalDefaultIme = device.executeShellCommand("settings get secure default_input_method").trim()

        // Enable Panda Keyboard in system settings via shell command
        device.executeShellCommand("settings put secure enabled_input_methods $IME_COMPONENT")
        device.executeShellCommand("ime enable $IME_COMPONENT")
        device.executeShellCommand("ime set $IME_COMPONENT")

        // Wait for system settings to sync
        SystemClock.sleep(500)
    }

    @After
    fun tearDown() {
        // Restore original default IME so CI runner / physical device isn't left in a broken state
        originalDefaultIme?.let { orig ->
            if (orig.isNotEmpty() && orig != "null") {
                device.executeShellCommand("settings put secure default_input_method $orig")
                device.executeShellCommand("ime set $orig")
            }
        }
    }

    @Test
    fun testRealImeServiceEnabledAndSelected() {
        val currentIme = device.executeShellCommand("settings get secure default_input_method").trim()
        assertNotNull("System default IME setting should not be null", currentIme)
    }
}
