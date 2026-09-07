package com.panda.keyboards.ime.keyboard

import android.view.inputmethod.EditorInfo

/**
 * Represents the current mode of the keyboard.
 */
enum class KeyboardMode {
    LOWERCASE,
    UPPERCASE,
    CAPS_LOCK,
    SYMBOLS_1,
    SYMBOLS_2,
    EMOJI,
    CLIPBOARD
}

/**
 * Observable state of the keyboard UI.
 *
 * @property mode Current keyboard mode (letter case, symbols layer).
 * @property imeAction The IME action from the current editor, used to
 *           change the Enter key icon/label (Search, Send, Done, etc.).
 */
data class KeyboardState(
    val mode: KeyboardMode = KeyboardMode.LOWERCASE,
    val imeAction: Int = EditorInfo.IME_ACTION_UNSPECIFIED
) {
    /** Whether the keyboard is currently showing letters (not symbols). */
    val isLetterMode: Boolean
        get() = mode == KeyboardMode.LOWERCASE ||
                mode == KeyboardMode.UPPERCASE ||
                mode == KeyboardMode.CAPS_LOCK

    /** Whether shift is active (UPPERCASE or CAPS_LOCK). */
    val isShifted: Boolean
        get() = mode == KeyboardMode.UPPERCASE || mode == KeyboardMode.CAPS_LOCK

    /** Whether caps lock is engaged. */
    val isCapsLock: Boolean
        get() = mode == KeyboardMode.CAPS_LOCK
}
