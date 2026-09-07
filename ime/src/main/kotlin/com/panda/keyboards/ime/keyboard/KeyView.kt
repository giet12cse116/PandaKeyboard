package com.panda.keyboards.ime.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Individual keyboard key composable.
 *
 * Renders a key with theme-driven styling based on its [KeyType].
 * Handles tap events with haptic feedback. Backspace supports
 * press-and-hold for continuous deletion.
 * Top-row letter keys support long-press shortcuts for number insertion.
 *
 * @param key The key data (label, type, width weight).
 * @param isShifted Whether shift is currently active (to uppercase labels).
 * @param isCapsLock Whether caps lock is engaged (for shift key indicator).
 * @param imeAction Current IME action (for enter key label).
 * @param activeFontStyle Currently selected font style for key label rendering.
 * @param theme Resolved theme colors and shapes.
 * @param onKeyPress Callback when the key is pressed.
 * @param modifier Modifier for the key's container.
 */
@Composable
fun KeyView(
    key: KeyData,
    isShifted: Boolean,
    isCapsLock: Boolean,
    imeAction: Int,
    theme: ResolvedTheme,
    activeFontStyle: FontStyle = FontStyle.NORMAL,
    showNumberHints: Boolean = true,
    onKeyPress: (KeyData) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var wasLongPressed by remember { mutableStateOf(false) }

    // Continuous delete for backspace long-press
    LaunchedEffect(isLongPressing) {
        if (isLongPressing && key.type == KeyType.BACKSPACE) {
            // Initial delay before repeat starts
            delay(400)
            while (isLongPressing) {
                onKeyPress(key)
                delay(50) // Fast repeat rate
            }
        }
    }

    val isEnterKey = key.type == KeyType.ENTER
    val backgroundColor = when {
        isPressed -> if (key.type == KeyType.CHARACTER) theme.keyBackgroundPressed else theme.specialKeyBackgroundPressed
        key.type == KeyType.SHIFT && isCapsLock -> theme.accentLight
        key.type == KeyType.SHIFT && isShifted -> theme.accent
        isEnterKey -> theme.accent
        key.type == KeyType.CHARACTER -> theme.keyBackground
        key.type == KeyType.SPACE -> theme.keyBackground
        else -> theme.specialKeyBackground
    }

    val rawLabel = when {
        key.type == KeyType.CHARACTER && isShifted -> key.label.uppercase()
        key.type == KeyType.SPACE -> "space"
        key.type == KeyType.ENTER -> getEnterLabel(imeAction)
        else -> key.label
    }

    val displayLabel = if (key.type == KeyType.CHARACTER && activeFontStyle != FontStyle.NORMAL) {
        FontTransformer.transform(rawLabel, activeFontStyle)
    } else {
        rawLabel
    }

    val fontSize = when (key.type) {
        KeyType.CHARACTER -> 21.sp
        KeyType.SPACE -> 13.sp
        KeyType.SHIFT, KeyType.BACKSPACE, KeyType.HIDE_KEYBOARD -> 18.sp
        KeyType.ENTER -> 16.sp
        KeyType.SYMBOLS -> 13.sp
        KeyType.GLOBE, KeyType.EMOJI -> 18.sp
    }

    val textColor = when {
        key.type == KeyType.SHIFT && (isShifted || isCapsLock) -> Color.White
        isEnterKey -> Color.White
        key.type == KeyType.CHARACTER -> theme.keyText
        else -> theme.specialKeyText
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.5.dp, vertical = 3.dp)
            .clip(theme.keyShape)
            .background(backgroundColor)
            .pointerInput(key) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        wasLongPressed = false
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                        if (key.type == KeyType.BACKSPACE) {
                            // Immediately delete 1 char on touch down for responsive single click
                            onKeyPress(key)
                            isLongPressing = true
                        }

                        val hasLongPressShortcut = (showNumberHints && key.numberHint != null)
                        val released = if (hasLongPressShortcut) {
                            val releaseResult = withTimeoutOrNull(400) {
                                tryAwaitRelease()
                            }
                            if (releaseResult == null) {
                                // Long-press threshold reached while still pressed
                                wasLongPressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onKeyPress(KeyData(label = key.numberHint!!, output = key.numberHint!!, type = KeyType.CHARACTER))
                                tryAwaitRelease()
                                true
                            } else {
                                releaseResult
                            }
                        } else {
                            tryAwaitRelease()
                        }

                        isPressed = false
                        isLongPressing = false

                        if (released && key.type != KeyType.BACKSPACE && !wasLongPressed) {
                            onKeyPress(key)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Corner hint badge for long-press number shortcut on top letter row
        if (showNumberHints && key.numberHint != null) {
            Text(
                text = key.numberHint,
                color = textColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }

        // Pressed Key Highlight Bubble (Message Logo Popup)
        val bubbleSymbol = if (wasLongPressed && showNumberHints && key.numberHint != null) key.numberHint else displayLabel
        if (isPressed && bubbleSymbol.isNotEmpty() && key.type != KeyType.SPACE) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -170),
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    clippingEnabled = false
                )
            ) {
                MessageBubblePopup(
                    symbol = bubbleSymbol,
                    theme = theme
                )
            }
        }

        Text(
            text = displayLabel,
            color = textColor,
            fontSize = fontSize,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            fontWeight = if (key.type == KeyType.CHARACTER) FontWeight.Normal else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Highlight popup bubble displayed above a key when pressed, styled like a message logo callout.
 */
@Composable
private fun MessageBubblePopup(
    symbol: String,
    theme: ResolvedTheme
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp
        ),
        color = theme.accent,
        shadowElevation = 8.dp,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 62.dp)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🐼",
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = symbol,
                    color = Color.White,
                    fontSize = if (symbol.length > 2) 15.sp else 26.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Get the appropriate label for the Enter key based on the IME action.
 */
private fun getEnterLabel(imeAction: Int): String {
    return when (imeAction) {
        android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH -> "🔍"
        android.view.inputmethod.EditorInfo.IME_ACTION_SEND -> "➤"
        android.view.inputmethod.EditorInfo.IME_ACTION_NEXT -> "→"
        android.view.inputmethod.EditorInfo.IME_ACTION_DONE -> "✓"
        android.view.inputmethod.EditorInfo.IME_ACTION_GO -> "➜"
        else -> "↵"
    }
}
