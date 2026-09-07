package com.panda.keyboards.ime

import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer
import com.panda.keyboards.ime.keyboard.KeyboardLayouts
import com.panda.keyboards.ime.keyboard.PandaKeyboardLayout
import com.panda.keyboards.theme.KeyboardTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI test suite for [PandaKeyboardLayout].
 *
 * Verifies key rendering, shift/caps-lock state transitions, symbol layer navigation,
 * functional keys (space, Unicode surrogate-pair backspace, enter, globe),
 * font-selection strip transformation, and regression safety.
 */
@RunWith(AndroidJUnit4::class)
class PandaKeyboardLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // ── 2. LOWERCASE LETTER KEYS (a–z) ───────────────────────────────────────

    @Test
    fun testLowercaseLetterKeysDataDriven() {
        val alphabet = ('a'..'z').toList()

        for (letter in alphabet) {
            val fakeIc = FakeInputConnection()
            val actionHandler = DefaultKeyboardActionHandler(
                inputConnectionProvider = { fakeIc },
                editorInfoProvider = { null }
            )

            composeTestRule.setContent {
                PandaKeyboardLayout(
                    actionHandler = actionHandler,
                    theme = KeyboardTheme.DEFAULT
                )
            }

            val letterStr = letter.toString()
            composeTestRule.onNodeWithText(letterStr).assertIsDisplayed().performClick()

            assertEquals(
                "Tapping key '$letterStr' should commit '$letterStr'",
                letterStr,
                fakeIc.currentText
            )
        }
    }

    // ── 3. SHIFT / CAPS-LOCK ──────────────────────────────────────────────────

    @Test
    fun testShiftSingleTapAutoRelease() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Tap Shift -> Tap 'a'
        composeTestRule.onNodeWithText("⇧").performClick()
        composeTestRule.onNodeWithText("A").performClick()

        assertEquals("First letter after Shift should be uppercase 'A'", "A", fakeIc.currentText)

        // Tap 'b' -> Shift should auto-release back to lowercase 'b'
        composeTestRule.onNodeWithText("b").performClick()

        assertEquals("Second letter after Shift should auto-release to lowercase 'b'", "Ab", fakeIc.currentText)
    }

    @Test
    fun testShiftDoubleTapCapsLockPersistenceAndRelease() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Rapid double tap Shift -> engages Caps Lock
        composeTestRule.onNodeWithText("⇧").performClick()
        composeTestRule.onNodeWithText("⇧").performClick()

        // Type 'a', 'b', 'c'
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.onNodeWithText("B").performClick()
        composeTestRule.onNodeWithText("C").performClick()

        assertEquals("All letters in Caps Lock mode should be uppercase", "ABC", fakeIc.currentText)

        // Tap Shift once to release Caps Lock
        composeTestRule.onNodeWithText("⇧").performClick()
        composeTestRule.onNodeWithText("d").performClick()

        assertEquals("Letter after releasing Caps Lock should be lowercase 'd'", "ABCd", fakeIc.currentText)
    }

    // ── 4. SYMBOLS LAYERS ────────────────────────────────────────────────────

    @Test
    fun testSymbolsLayersNavigation() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Switch to Symbols Layer 1 (?123)
        composeTestRule.onNodeWithText("?123").performClick()
        composeTestRule.onNodeWithText("@").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("#").assertIsDisplayed().performClick()

        assertEquals("Symbols layer 1 input should commit '@#'", "@#", fakeIc.currentText)

        // Switch to Symbols Layer 2 (=\<)
        composeTestRule.onNodeWithText("=\\<").performClick()
        composeTestRule.onNodeWithText("~").assertIsDisplayed().performClick()

        assertEquals("Symbols layer 2 input should commit '@#~'", "@#~", fakeIc.currentText)

        // Switch back to ABC
        composeTestRule.onNodeWithText("ABC").performClick()
        composeTestRule.onNodeWithText("a").assertIsDisplayed().performClick()

        assertEquals("Switching back to ABC should commit 'a'", "@#~a", fakeIc.currentText)
    }

    // ── 5. FUNCTIONAL KEYS ───────────────────────────────────────────────────

    @Test
    fun testFunctionalKeysSpaceAndEnter() {
        val fakeIc = FakeInputConnection(isSingleLine = false)
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Space key test
        composeTestRule.onNodeWithText("a").performClick()
        composeTestRule.onNodeWithText("space").performClick()
        composeTestRule.onNodeWithText("b").performClick()

        assertEquals("Space key should commit space character 'a b'", "a b", fakeIc.currentText)

        // Enter key test (multi-line)
        composeTestRule.onNodeWithText("↵").performClick()
        composeTestRule.onNodeWithText("c").performClick()

        assertEquals("Enter key should commit newline in multi-line mode", "a b\nc", fakeIc.currentText)
    }

    @Test
    fun testUnicodeSurrogatePairSafeBackspace() {
        val fakeIc = FakeInputConnection()
        var activeStyle = FontStyle.GOTHIC

        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null },
            activeFontStyleProvider = { activeStyle }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                activeFontStyle = activeStyle,
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Type 'a' in GOTHIC style -> produces 2-char UTF-16 surrogate pair '𝔞'
        composeTestRule.onNodeWithText("a").performClick()
        val transformedA = FontTransformer.transform("a", FontStyle.GOTHIC)
        assertEquals("Committed text should be surrogate pair '𝔞'", transformedA, fakeIc.currentText)
        assertEquals("Surrogate pair '𝔞' length should be 2 Char units", 2, fakeIc.currentText.length)

        // Tap Backspace -> should delete entire surrogate pair cluster via deleteSurroundingTextInCodePoints(1, 0)
        composeTestRule.onNodeWithText("⌫").performClick()

        assertEquals("Backspace must delete full surrogate pair cluster without leaving orphan surrogate bytes", "", fakeIc.currentText)
    }

    @Test
    fun testGlobeKeyCallback() {
        var globeKeyTapped = false
        val fakeIc = FakeInputConnection()
        val actionHandler = object : KeyboardActionHandler {
            override fun onTextInput(text: String) {}
            override fun onBackspace() {}
            override fun onEnter() {}
            override fun onSpace() {}
            override fun onGlobeKey() {
                globeKeyTapped = true
            }
        }

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                theme = KeyboardTheme.DEFAULT
            )
        }

        composeTestRule.onNodeWithText("🌐").performClick()
        assertTrue("Tapping Globe 🌐 key should invoke onGlobeKey() callback", globeKeyTapped)
    }

    // ── 6. FONT-SWITCHING STRIP ──────────────────────────────────────────────

    @Test
    fun testFontSwitchingStripTransformationAndFavorites() {
        val fakeIc = FakeInputConnection()
        var currentStyle = FontStyle.NORMAL
        val favoriteIds = setOf("gothic")

        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null },
            activeFontStyleProvider = { currentStyle }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                activeFontStyle = currentStyle,
                favoriteFontIds = favoriteIds,
                onFontStyleSelected = { style -> currentStyle = style },
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Favorite font style 'Gothic' (♥ 𝔄Head) should surface at beginning of strip
        composeTestRule.onNodeWithText("♥ 𝔄Head", substring = true).assertIsDisplayed().performClick()

        // Type 'a' (key cap label now reflects Gothic style '𝔞')
        val expectedGothicA = FontTransformer.transform("a", FontStyle.GOTHIC)
        composeTestRule.onNodeWithText(expectedGothicA).performClick()
        assertEquals("Committed text after selecting Gothic chip should be transformed", expectedGothicA, fakeIc.currentText)

        // Switch back to Normal chip
        composeTestRule.onNodeWithText("Normal").performClick()
        composeTestRule.onNodeWithText("b").performClick()

        assertEquals("Committed text after switching back to Normal should append raw 'b'", expectedGothicA + "b", fakeIc.currentText)
    }

    // ── 7. NEW SPRINT FEATURES & REGRESSION SAFETY ───────────────────────────

    @Test
    fun testFontAwareKeyLabelRendering() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                activeFontStyle = FontStyle.BOLD_SERIF,
                theme = KeyboardTheme.DEFAULT
            )
        }

        val boldSerifA = FontTransformer.transform("a", FontStyle.BOLD_SERIF)
        composeTestRule.onNodeWithText(boldSerifA).assertIsDisplayed().performClick()
        assertEquals("Key cap displaying bold serif 'a' should commit transformed text", boldSerifA, fakeIc.currentText)
    }

    @Test
    fun testAutoCapVisualShiftSync() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null },
            isAutoCapEnabled = { true }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                settings = com.panda.keyboards.theme.KeyboardSettings(autoCorrectionEnabled = true),
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Auto-cap should visually shift keyboard at start of field -> uppercase 'A' key displayed
        composeTestRule.onNodeWithText("A").assertIsDisplayed().performClick()
        assertEquals("Auto-cap at start of field should commit uppercase 'A'", "A", fakeIc.currentText)

        // Second letter should auto-revert to lowercase 'b'
        composeTestRule.onNodeWithText("b").assertIsDisplayed().performClick()
        assertEquals("Subsequent letter should auto-release to lowercase 'b'", "Ab", fakeIc.currentText)
    }

    @Test
    fun testLongPressNumberShortcutOnTopRow() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                settings = com.panda.keyboards.theme.KeyboardSettings(autoCorrectionEnabled = false),
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Key 'q' displays hint badge '1' in top right
        composeTestRule.onNodeWithText("1", substring = true).assertIsDisplayed()

        // Quick tap 'q' -> commits 'q'
        composeTestRule.onNodeWithText("q").performClick()
        assertEquals("Normal tap on key 'q' should commit 'q'", "q", fakeIc.currentText)
    }

    @Test
    fun testRegressionSafetyNoUnwiredKeys() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                settings = com.panda.keyboards.theme.KeyboardSettings(autoCorrectionEnabled = false),
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Tap all keys in row 1 without exception
        val row1Keys = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        for (k in row1Keys) {
            composeTestRule.onNodeWithText(k).performClick()
        }

        assertEquals("Row 1 typing sequence should commit 'qwertyuiop'", "qwertyuiop", fakeIc.currentText)
    }

    @Test
    fun testRapidTypingSequenceRaceConditionSafety() {
        val fakeIc = FakeInputConnection()
        val actionHandler = DefaultKeyboardActionHandler(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { null }
        )

        composeTestRule.setContent {
            PandaKeyboardLayout(
                actionHandler = actionHandler,
                settings = com.panda.keyboards.theme.KeyboardSettings(autoCorrectionEnabled = false),
                theme = KeyboardTheme.DEFAULT
            )
        }

        // Rapid 25-key typing sequence
        val targetSequence = "p-a-n-d-a-k-e-y-b-o-a-r-d-s".replace("-", "")
        for (char in targetSequence) {
            composeTestRule.onNodeWithText(char.toString()).performClick()
        }

        assertEquals("Rapid typing sequence should commit exact string without race dropouts", targetSequence, fakeIc.currentText)
    }
}
