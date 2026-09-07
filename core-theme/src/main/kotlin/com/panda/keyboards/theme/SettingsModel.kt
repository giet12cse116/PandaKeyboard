package com.panda.keyboards.theme

import kotlinx.serialization.Serializable

/**
 * Supported keyboard layout arrangements.
 */
@Serializable
enum class QwertyOrder(val displayName: String) {
    QWERTY("QWERTY (US/Global)"),
    QWERTZ("QWERTZ (German/Central European)"),
    AZERTY("AZERTY (French/Belgian)")
}

/**
 * Supported keyboard vertical heights in dp.
 */
@Serializable
enum class KeyboardHeight(val displayName: String, val heightDp: Int) {
    COMPACT("Compact (220 dp)", 220),
    DEFAULT("Default (260 dp)", 260),
    TALL("Tall (300 dp)", 300)
}

/**
 * Data model for all customizable keyboard preferences.
 */
@Serializable
data class KeyboardSettings(
    val qwertyOrder: QwertyOrder = QwertyOrder.QWERTY,
    val keyboardHeight: KeyboardHeight = KeyboardHeight.DEFAULT,
    val autoCorrectionEnabled: Boolean = true,
    val offlineModeEnabled: Boolean = false,
    val numberRowEnabled: Boolean = false
)

