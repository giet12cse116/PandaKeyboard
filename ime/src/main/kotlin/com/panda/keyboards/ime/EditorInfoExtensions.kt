package com.panda.keyboards.ime

import android.view.inputmethod.EditorInfo

/**
 * Extension function to determine if an [EditorInfo] represents a password or sensitive input field.
 *
 * Checks for text and numeric password variations:
 * - [EditorInfo.TYPE_TEXT_VARIATION_PASSWORD]
 * - [EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD]
 * - [EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD]
 * - [EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD]
 */
fun EditorInfo?.isPasswordField(): Boolean {
    if (this == null) return false
    val variation = inputType and EditorInfo.TYPE_MASK_VARIATION
    val inputClass = inputType and EditorInfo.TYPE_MASK_CLASS

    val isTextPassword = inputClass == EditorInfo.TYPE_CLASS_TEXT && (
        variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
        variation == EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
        variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    )

    val isNumberPassword = inputClass == EditorInfo.TYPE_CLASS_NUMBER && (
        variation == EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD
    )

    return isTextPassword || isNumberPassword
}

/**
 * Extension function to determine if an [EditorInfo] represents a numeric input field.
 *
 * Checks if the input class is one of:
 * - [EditorInfo.TYPE_CLASS_NUMBER]
 * - [EditorInfo.TYPE_CLASS_PHONE]
 * - [EditorInfo.TYPE_CLASS_DATETIME]
 */
fun EditorInfo?.isNumericField(): Boolean {
    if (this == null) return false
    val inputClass = inputType and EditorInfo.TYPE_MASK_CLASS
    return inputClass == EditorInfo.TYPE_CLASS_NUMBER ||
           inputClass == EditorInfo.TYPE_CLASS_PHONE ||
           inputClass == EditorInfo.TYPE_CLASS_DATETIME
}
