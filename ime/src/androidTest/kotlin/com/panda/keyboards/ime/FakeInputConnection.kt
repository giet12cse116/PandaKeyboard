package com.panda.keyboards.ime

import android.os.Build
import android.os.Bundle
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo

/**
 * In-memory spy [InputConnection] for verifying IME key commits, backspace deletes,
 * auto-capitalization, and font transformer output during instrumented UI testing.
 */
class FakeInputConnection(
    private var initialText: String = "",
    val isSingleLine: Boolean = false
) : InputConnection {

    private val textBuffer = StringBuilder(initialText)

    val currentText: String
        get() = textBuffer.toString()

    val committedEvents = mutableListOf<String>()
    val backspaceEvents = mutableListOf<Pair<Int, Int>>()
    val performedActions = mutableListOf<Int>()

    override fun getTextBeforeCursor(n: Int, flags: Int): CharSequence {
        val len = textBuffer.length
        return if (len <= n) textBuffer.toString() else textBuffer.substring(len - n)
    }

    override fun getTextAfterCursor(n: Int, flags: Int): CharSequence {
        return ""
    }

    override fun getSelectedText(flags: Int): CharSequence? {
        return null
    }

    override fun getCursorCapsMode(reqModes: Int): Int {
        return 0
    }

    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText {
        return ExtractedText().apply { text = textBuffer.toString() }
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        backspaceEvents.add(beforeLength to afterLength)
        val len = textBuffer.length
        if (len > 0) {
            val deleteCount = Math.min(beforeLength, len)
            textBuffer.delete(len - deleteCount, len)
        }
        return true
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        backspaceEvents.add(beforeLength to afterLength)
        val len = textBuffer.length
        if (len > 0) {
            // Delete code points properly
            var count = 0
            var idx = len
            while (count < beforeLength && idx > 0) {
                val codePoint = Character.codePointBefore(textBuffer, idx)
                idx -= Character.charCount(codePoint)
                count++
            }
            textBuffer.delete(idx, len)
        }
        return true
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        return true
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        return true
    }

    override fun finishComposingText(): Boolean {
        return true
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        val str = text?.toString() ?: ""
        committedEvents.add(str)
        textBuffer.append(str)
        return true
    }

    override fun commitCompletion(text: CompletionInfo?): Boolean {
        return true
    }

    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
        return true
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        return true
    }

    override fun performEditorAction(editorAction: Int): Boolean {
        performedActions.add(editorAction)
        return true
    }

    override fun performContextMenuAction(id: Int): Boolean {
        return true
    }

    override fun beginBatchEdit(): Boolean {
        return true
    }

    override fun endBatchEdit(): Boolean {
        return true
    }

    override fun sendKeyEvent(event: android.view.KeyEvent?): Boolean {
        return true
    }

    override fun clearMetaKeyStates(states: Int): Boolean {
        return true
    }

    override fun reportFullscreenMode(enabled: Boolean): Boolean {
        return true
    }

    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
        return true
    }

    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
        return true
    }

    override fun getHandler(): android.os.Handler? {
        return null
    }

    override fun closeConnection() {}

    override fun commitContent(
        inputContentInfo: InputContentInfo,
        flags: Int,
        opts: Bundle?
    ): Boolean {
        return true
    }
}
