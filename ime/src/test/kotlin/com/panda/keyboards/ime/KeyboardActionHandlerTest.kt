package com.panda.keyboards.ime

import android.view.inputmethod.InputConnection
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

/**
 * Unit tests for [DefaultKeyboardActionHandler] and auto-capitalization logic.
 */
class KeyboardActionHandlerTest {

    private fun createMockInputConnection(
        committedTextList: MutableList<String>,
        buffer: StringBuilder
    ): InputConnection {
        return Proxy.newProxyInstance(
            InputConnection::class.java.classLoader,
            arrayOf(InputConnection::class.java)
        ) { _, method, args ->
            when (method.name) {
                "commitText" -> {
                    val text = (args[0] as CharSequence).toString()
                    committedTextList.add(text)
                    buffer.append(text)
                    true
                }
                "getTextBeforeCursor" -> {
                    val n = args[0] as Int
                    val str = buffer.toString()
                    if (str.length <= n) str else str.substring(str.length - n)
                }
                "deleteSurroundingText" -> {
                    val before = args[0] as Int
                    if (buffer.length >= before) {
                        buffer.delete(buffer.length - before, buffer.length)
                    }
                    true
                }
                else -> null
            }
        } as InputConnection
    }

    @Test
    fun `autoCap enabled reports true for first letter when buffer is empty and commits character verbatim`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder()
        val mockIc = createMockInputConnection(committed, buffer)

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { true }
        )

        org.junit.Assert.assertTrue(handler.shouldAutoCapitalize())
        handler.onTextInput("H")
        assertEquals("H", committed.last())
    }

    @Test
    fun `autoCap enabled reports true after dot space and commits character verbatim`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder("Hello world. ")
        val mockIc = createMockInputConnection(committed, buffer)

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { true }
        )

        org.junit.Assert.assertTrue(handler.shouldAutoCapitalize())
        handler.onTextInput("T")
        assertEquals("T", committed.last())
    }

    @Test
    fun `autoCap enabled does not capitalize letter in middle of word`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder("Hel")
        val mockIc = createMockInputConnection(committed, buffer)

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { true }
        )

        org.junit.Assert.assertFalse(handler.shouldAutoCapitalize())
        handler.onTextInput("l")
        assertEquals("l", committed.last())
    }

    @Test
    fun `autoCap disabled preserves lowercase input at sentence start`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder()
        val mockIc = createMockInputConnection(committed, buffer)

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { false }
        )

        org.junit.Assert.assertFalse(handler.shouldAutoCapitalize())
        handler.onTextInput("h")
        assertEquals("h", committed.last())
    }

    @Test
    fun `space and backspace interact properly with input connection`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder("Hi")
        val mockIc = createMockInputConnection(committed, buffer)

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { true }
        )

        handler.onSpace()
        assertEquals("Hi ", buffer.toString())

        handler.onBackspace()
        assertEquals("Hi", buffer.toString())
    }

    @Test
    fun `shouldAutoCapitalize returns true at field start and sentence endings`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder()
        val mockIc = createMockInputConnection(committed, buffer)

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { true }
        )

        org.junit.Assert.assertTrue("Empty buffer should trigger auto-cap", handler.shouldAutoCapitalize())

        buffer.append("Hello. ")
        org.junit.Assert.assertTrue("After dot space should trigger auto-cap", handler.shouldAutoCapitalize())

        buffer.clear()
        buffer.append("Hey! ")
        org.junit.Assert.assertTrue("After exclamation space should trigger auto-cap", handler.shouldAutoCapitalize())

        buffer.clear()
        buffer.append("Really? ")
        org.junit.Assert.assertTrue("After question mark space should trigger auto-cap", handler.shouldAutoCapitalize())

        buffer.clear()
        buffer.append("Line\n")
        org.junit.Assert.assertTrue("After newline should trigger auto-cap", handler.shouldAutoCapitalize())

        buffer.clear()
        buffer.append("Mid")
        org.junit.Assert.assertFalse("Middle of word should not trigger auto-cap", handler.shouldAutoCapitalize())
    }

    @Test
    fun `shouldAutoCapitalize returns false for password fields`() {
        val committed = mutableListOf<String>()
        val buffer = StringBuilder()
        val mockIc = createMockInputConnection(committed, buffer)

        val passwordEditorInfo = android.view.inputmethod.EditorInfo().apply {
            inputType = android.view.inputmethod.EditorInfo.TYPE_CLASS_TEXT or android.view.inputmethod.EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        }

        val handler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { mockIc },
            editorInfoProvider = { passwordEditorInfo },
            isAutoCapEnabled = { true }
        )

        org.junit.Assert.assertFalse("Password field should not trigger auto-cap", handler.shouldAutoCapitalize())

        handler.onTextInput("a")
        assertEquals("a", committed.last())
    }
}
