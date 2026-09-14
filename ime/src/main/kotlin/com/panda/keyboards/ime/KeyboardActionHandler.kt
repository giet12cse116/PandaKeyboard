package com.panda.keyboards.ime

import android.os.Build
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer
import com.panda.keyboards.ime.autocomplete.SuggestionManager

/**
 * Bridges keyboard composable key events and suggestion bar taps to [InputConnection] calls.
 *
 * This keeps the keyboard composables decoupled from the Android IME
 * framework — they only call methods on this interface, never
 * [InputConnection] directly.
 */
interface KeyboardActionHandler {
    /** Commit a string of text at the cursor position. */
    fun onTextInput(text: String)

    /** Delete one character before the cursor. */
    fun onBackspace()

    /** Handle Enter key — performs the IME action or inserts a newline. */
    fun onEnter()

    /** Insert a space character. */
    fun onSpace()

    /** Replace the in-progress word before the cursor with a selected suggestion chip. */
    fun onSuggestionSelected(suggestion: String) {}

    /** Retrieve current in-progress word before cursor. */
    fun getCurrentWord(): String = ""

    /** Check whether current cursor context requires auto-capitalization of next character. */
    fun shouldAutoCapitalize(): Boolean = false

    /** Switch to next input method or open input method picker. */
    fun onGlobeKey() {}

    /** Hide the soft keyboard. */
    fun onHideKeyboard() {}
}

/**
 * Default implementation that delegates to the current [InputConnection].
 *
 * @param inputConnectionProvider Lambda returning the current InputConnection.
 * @param editorInfoProvider Lambda returning current EditorInfo for IME action.
 * @param isAutoCapEnabled Lambda returning auto-capitalization toggle state.
 * @param activeFontStyleProvider Lambda returning currently selected font style.
 * @param onGlobeKeyAction Lambda invoked when globe key is tapped.
 * @param onHideKeyboardAction Lambda invoked when hide keyboard action is triggered.
 */
class DefaultKeyboardActionHandler(
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?,
    private val isAutoCapEnabled: () -> Boolean = { true },
    private val activeFontStyleProvider: () -> FontStyle = { FontStyle.NORMAL },
    private val onGlobeKeyAction: (() -> Unit)? = null,
    private val onHideKeyboardAction: (() -> Unit)? = null
) : KeyboardActionHandler {

    override fun shouldAutoCapitalize(): Boolean {
        if (!isAutoCapEnabled()) return false
        val editorInfo = editorInfoProvider()
        if (editorInfo.isPasswordField()) return false

        val ic = inputConnectionProvider() ?: return false
        val precedingText = ic.getTextBeforeCursor(2, 0)?.toString() ?: ""
        return precedingText.isEmpty() ||
                precedingText.endsWith(". ") ||
                precedingText.endsWith("! ") ||
                precedingText.endsWith("? ") ||
                precedingText.endsWith("\n")
    }

    override fun onTextInput(text: String) {
        val ic = inputConnectionProvider() ?: return

        // Learning hook: if typing punctuation or word boundary, learn preceding completed word (disabled on password fields)
        val editorInfo = editorInfoProvider()
        if (!editorInfo.isPasswordField() && text.length == 1 && SuggestionManager.isWordBoundary(text[0])) {
            val precedingText = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
            val completedWord = SuggestionManager.extractCurrentWord(precedingText)
            if (completedWord.isNotEmpty()) {
                SuggestionManager.recordLearnedWord(completedWord)
            }
        }

        // Commit exact text passed from keyboard UI layout (WYSIWYG), transformed using active FontStyle
        val activeStyle = activeFontStyleProvider()
        val textToCommit = FontTransformer.transform(text, activeStyle)

        ic.commitText(textToCommit, 1)
    }

    override fun onBackspace() {
        val ic = inputConnectionProvider() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ic.deleteSurroundingTextInCodePoints(1, 0)
        } else {
            val precedingText = ic.getTextBeforeCursor(2, 0)
            if (!precedingText.isNullOrEmpty() && precedingText.length == 2 &&
                Character.isSurrogatePair(precedingText[0], precedingText[1])
            ) {
                ic.deleteSurroundingText(2, 0)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
        }
    }

    override fun onEnter() {
        val ic = inputConnectionProvider() ?: return
        val precedingText = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        val completedWord = SuggestionManager.extractCurrentWord(precedingText)
        if (completedWord.isNotEmpty()) {
            SuggestionManager.recordLearnedWord(completedWord)
        }

        val editorInfo = editorInfoProvider()
        val imeAction = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_UNSPECIFIED

        // If there's a specific IME action (Search, Send, Next, Done, Go),
        // perform it. Otherwise, insert a newline.
        if (imeAction != EditorInfo.IME_ACTION_UNSPECIFIED &&
            imeAction != EditorInfo.IME_ACTION_NONE
        ) {
            ic.performEditorAction(imeAction)
        } else {
            ic.commitText("\n", 1)
        }
    }

    override fun onSpace() {
        val ic = inputConnectionProvider() ?: return
        val precedingText = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        val completedWord = SuggestionManager.extractCurrentWord(precedingText)
        if (completedWord.isNotEmpty()) {
            SuggestionManager.recordLearnedWord(completedWord)
        }

        ic.commitText(" ", 1)
    }

    override fun onSuggestionSelected(suggestion: String) {
        val ic = inputConnectionProvider() ?: return
        val precedingText = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        val currentWord = SuggestionManager.extractCurrentWord(precedingText)

        if (currentWord.isNotEmpty()) {
            ic.deleteSurroundingText(currentWord.length, 0)
        }

        val activeStyle = activeFontStyleProvider()
        val transformedSuggestion = FontTransformer.transform(suggestion, activeStyle)

        ic.commitText("$transformedSuggestion ", 1)
        SuggestionManager.recordLearnedWord(suggestion)
    }

    override fun getCurrentWord(): String {
        val ic = inputConnectionProvider() ?: return ""
        val precedingText = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        return SuggestionManager.extractCurrentWord(precedingText)
    }

    override fun onGlobeKey() {
        onGlobeKeyAction?.invoke()
    }

    override fun onHideKeyboard() {
        onHideKeyboardAction?.invoke()
    }
}

