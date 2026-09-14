package com.panda.keyboards.ime

import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorInfoExtensionsTest {

    @Test
    fun `isNumericField returns true for numeric input classes`() {
        val numberInfo = EditorInfo().apply {
            inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        }
        val phoneInfo = EditorInfo().apply {
            inputType = EditorInfo.TYPE_CLASS_PHONE
        }
        val dateTimeInfo = EditorInfo().apply {
            inputType = EditorInfo.TYPE_CLASS_DATETIME
        }

        assertTrue("TYPE_CLASS_NUMBER must be numeric field", numberInfo.isNumericField())
        assertTrue("TYPE_CLASS_PHONE must be numeric field", phoneInfo.isNumericField())
        assertTrue("TYPE_CLASS_DATETIME must be numeric field", dateTimeInfo.isNumericField())
    }

    @Test
    fun `isNumericField returns false for text fields`() {
        val textInfo = EditorInfo().apply {
            inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val nullInfo: EditorInfo? = null

        assertFalse("TYPE_CLASS_TEXT is not numeric field", textInfo.isNumericField())
        assertFalse("Null EditorInfo is not numeric field", nullInfo.isNumericField())
    }
}
