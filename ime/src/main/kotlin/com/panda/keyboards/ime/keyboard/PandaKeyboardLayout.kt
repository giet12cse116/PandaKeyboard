package com.panda.keyboards.ime.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.navigationBarsPadding
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.ime.KeyboardActionHandler
import com.panda.keyboards.ime.isNumericField
import com.panda.keyboards.ime.isPasswordField
import com.panda.keyboards.theme.KeyboardTheme

/** Time threshold for double-tap shift → caps lock (ms). */
private const val DOUBLE_TAP_THRESHOLD_MS = 400L

/** Top toolbar panel display mode. */
enum class TopToolbarPanel {
    NONE,
    THEME_SWITCHER,
    FONT_STRIP
}

/**
 * Main keyboard layout composable.
 *
 * Renders a full QWERTY keyboard with support for:
 * - Lowercase / Uppercase / Caps Lock modes
 * - Two symbols layers (?123 / =\<)
 * - Top toolbar with 4 action icons (Theme, Settings, Voice, Fonts) and expandable panels
 * - Theme-driven appearance (colors, key shapes, background)
 * - Haptic feedback per key press
 * - Continuous backspace on long-press
 *
 * This composable is the **single source of truth** for keyboard rendering.
 * All visual properties are derived from the [theme] parameter.
 *
 * @param actionHandler Callback interface for committing text, deleting, etc.
 * @param imeAction Current IME action from EditorInfo.
 * @param theme The keyboard theme to apply. If null, uses the default dark theme.
 * @param availableThemes List of themes available for the quick theme switcher panel.
 * @param onThemeSelected Callback when a theme is selected from the quick switcher.
 * @param onOpenSettings Callback when Settings icon is tapped.
 * @param onTriggerVoiceInput Callback when Voice icon is tapped.
 */
@Composable
fun PandaKeyboardLayout(
    actionHandler: KeyboardActionHandler? = null,
    imeAction: Int = android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED,
    editorInfo: android.view.inputmethod.EditorInfo? = null,
    theme: KeyboardTheme? = null,
    settings: com.panda.keyboards.theme.KeyboardSettings = com.panda.keyboards.theme.KeyboardSettings(),
    activeFontStyle: FontStyle = FontStyle.NORMAL,
    favoriteFontIds: Set<String> = emptySet(),
    availableThemes: List<KeyboardTheme> = emptyList(),
    recentEmojis: List<String> = emptyList(),
    clipboardHistory: List<com.panda.keyboards.theme.ClipboardItem> = emptyList(),
    persistedDismissedClipId: String? = null,
    persistedLastShownClipText: String? = null,
    voiceStatusText: String = "",
    voicePartialText: String = "",
    voiceRmsDb: Float = 0f,
    isVoiceRecording: Boolean = false,
    hasAudioPermission: Boolean = true,
    isVoiceOfflineUnavailable: Boolean = false,
    onStartVoiceRecording: () -> Unit = {},
    onStopVoiceRecording: () -> Unit = {},
    onCancelVoiceRecording: () -> Unit = {},
    onRequestAudioPermission: () -> Unit = {},
    onOpenVoiceSettings: () -> Unit = {},
    onFontStyleSelected: (FontStyle) -> Unit = {},
    onThemeSelected: (String) -> Unit = {},
    onEmojiUsed: (String) -> Unit = {},
    onDeleteClipItem: (String) -> Unit = {},
    onClearClipboardHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onTriggerVoiceInput: () -> Unit = {},
    onDismissClip: (String) -> Unit = {},
    inputSessionId: Int = 0
) {
    // Resolve theme to Compose rendering values — memoized per theme instance and height setting
    val resolvedTheme = remember(theme, settings.keyboardHeight) {
        if (theme != null) ResolvedTheme.from(theme, settings.keyboardHeight) else ResolvedTheme.DEFAULT
    }

    var keyboardState by remember {
        mutableStateOf(KeyboardState(imeAction = imeAction))
    }

    var currentTopPanel by remember {
        mutableStateOf(TopToolbarPanel.NONE)
    }

    // Reset to default alphabet screen whenever a new input session starts
    androidx.compose.runtime.LaunchedEffect(inputSessionId, editorInfo) {
        if (inputSessionId > 0) {
            val initialMode = if (editorInfo.isNumericField()) {
                KeyboardMode.NUMBER_PAD
            } else if (editorInfo.isPasswordField()) {
                KeyboardMode.LOWERCASE
            } else if (settings.autoCapitalizationEnabled && actionHandler?.shouldAutoCapitalize() == true) {
                KeyboardMode.UPPERCASE
            } else {
                KeyboardMode.LOWERCASE
            }
            keyboardState = keyboardState.copy(mode = initialMode)
            currentTopPanel = TopToolbarPanel.NONE
        }
    }

    // Update IME action when it changes
    if (keyboardState.imeAction != imeAction) {
        keyboardState = keyboardState.copy(imeAction = imeAction)
    }

    // Track last shift tap time for double-tap caps lock detection
    var lastShiftTapTime by remember { mutableLongStateOf(0L) }

    // Determine which rows to display based on current mode, QWERTY layout setting, and number row preference
    val baseRows = when (keyboardState.mode) {
        KeyboardMode.LOWERCASE, KeyboardMode.UPPERCASE, KeyboardMode.CAPS_LOCK ->
            KeyboardLayouts.getLetterRows(settings.qwertyOrder)
        KeyboardMode.SYMBOLS_1 -> KeyboardLayouts.symbols1Rows
        KeyboardMode.SYMBOLS_2 -> KeyboardLayouts.symbols2Rows
        KeyboardMode.NUMBER_PAD -> KeyboardLayouts.numberPadRows
        KeyboardMode.EMOJI, KeyboardMode.CLIPBOARD, KeyboardMode.VOICE -> emptyList()
    }

    val rows = if (settings.numberRowEnabled && (keyboardState.mode == KeyboardMode.LOWERCASE || keyboardState.mode == KeyboardMode.UPPERCASE || keyboardState.mode == KeyboardMode.CAPS_LOCK)) {
        listOf(KeyboardLayouts.numberRow) + baseRows
    } else {
        baseRows
    }

    // Track current suggestions reactively for SuggestionBar
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // Track quick-paste visibility: display to user only once per distinct new copy event
    var hasKeyPressed by remember { mutableStateOf(false) }
    var dismissedClipId by remember { mutableStateOf<String?>(null) }
    var currentClipId by remember { mutableStateOf<String?>(null) }

    val latestClip = clipboardHistory.firstOrNull()
    val latestClipId = latestClip?.id
    val latestClipText = latestClip?.text

    if (latestClipId != currentClipId) {
        currentClipId = latestClipId
        hasKeyPressed = false
    }

    val activeQuickPasteText = if (
        latestClipText != null &&
        latestClipText.isNotBlank() &&
        !hasKeyPressed &&
        latestClipId != dismissedClipId &&
        latestClipId != persistedDismissedClipId &&
        latestClipText != persistedLastShownClipText
    ) {
        latestClipText
    } else {
        null
    }

    fun dismissCurrentClip() {
        hasKeyPressed = true
        latestClipText?.let { clipText ->
            latestClipId?.let { clipId ->
                dismissedClipId = clipId
                onDismissClip(clipText)
            }
        }
    }

    fun checkAutoCap() {
        if (editorInfo.isPasswordField() || editorInfo.isNumericField()) return
        if (settings.autoCapitalizationEnabled && actionHandler != null && keyboardState.mode == KeyboardMode.LOWERCASE) {
            if (actionHandler.shouldAutoCapitalize()) {
                keyboardState = keyboardState.copy(mode = KeyboardMode.UPPERCASE)
            }
        }
    }

    fun refreshSuggestions() {
        if (!editorInfo.isPasswordField() && !editorInfo.isNumericField() && settings.autoCorrectionEnabled && actionHandler != null && keyboardState.mode != KeyboardMode.EMOJI && keyboardState.mode != KeyboardMode.CLIPBOARD && keyboardState.mode != KeyboardMode.VOICE && keyboardState.mode != KeyboardMode.NUMBER_PAD) {
            val currentWord = actionHandler.getCurrentWord()
            suggestions = com.panda.keyboards.ime.autocomplete.SuggestionManager.getSuggestions(currentWord, 4)
        } else {
            suggestions = emptyList()
        }
    }

    androidx.compose.runtime.LaunchedEffect(settings.autoCorrectionEnabled, settings.autoCapitalizationEnabled, editorInfo) {
        refreshSuggestions()
        checkAutoCap()
    }

    // Decode background image bitmap (supports local files and bundled assets)
    val context = androidx.compose.ui.platform.LocalContext.current
    val bgImageBitmap = remember(resolvedTheme.imagePath) {
        resolvedTheme.imagePath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists() && file.isFile) {
                    BitmapFactory.decodeFile(path)?.asImageBitmap()
                } else {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    val synthwaveStyle = resolvedTheme.synthwaveCyberpunkNeonStyle
    // Apply keyboard background — image background takes priority, then gradient brush, then solid color
    val backgroundModifier = when {
        synthwaveStyle != null -> Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF031B24),
                    Color(0xFF074554),
                    Color(0xFF0C6B7D),
                    Color(0xFF0F939A),
                    Color(0xFF10B981)
                )
            )
        )
        bgImageBitmap == null -> {
            if (resolvedTheme.keyboardBackgroundBrush != null) {
                Modifier.background(resolvedTheme.keyboardBackgroundBrush)
            } else {
                Modifier.background(resolvedTheme.keyboardBackground)
            }
        }
        else -> Modifier
    }

    val baseHeightDp = settings.keyboardHeight.heightDp.dp
    val rowHeightDp = baseHeightDp / 4
    val keyRowsCount = if (rows.isEmpty()) 4 else rows.size
    val keyRowsHeightDp = rowHeightDp * keyRowsCount
    val suggestionBarHeightDp = if (keyboardState.mode == KeyboardMode.EMOJI || keyboardState.mode == KeyboardMode.CLIPBOARD || keyboardState.mode == KeyboardMode.VOICE) 36.dp else if (settings.autoCorrectionEnabled) 36.dp else 0.dp
    val totalHeightDp = keyRowsHeightDp + 44.dp + suggestionBarHeightDp + 6.dp



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(backgroundModifier)
            .navigationBarsPadding()
    ) {
        if (bgImageBitmap != null && synthwaveStyle == null) {
            androidx.compose.foundation.Image(
                bitmap = bgImageBitmap,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }



        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeightDp)
                .padding(start = 3.dp, end = 3.dp, top = 3.dp, bottom = 9.dp)
        ) {
            if (keyboardState.mode == KeyboardMode.EMOJI) {
                // ── Full Emoji & Stickers Panel View ────────────────────────
                EmojiPanel(
                    recentEmojis = recentEmojis,
                    onEmojiSelected = { emojiStr ->
                        dismissCurrentClip()
                        actionHandler?.onTextInput(emojiStr)
                        onEmojiUsed(emojiStr)
                    },
                    onBackToKeyboard = {
                        keyboardState = keyboardState.copy(mode = KeyboardMode.LOWERCASE)
                    },
                    actionHandler = actionHandler,
                    editorInfo = editorInfo,
                    resolvedTheme = resolvedTheme,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (keyboardState.mode == KeyboardMode.CLIPBOARD) {
                // ── Full Clipboard History Panel View ───────────────────────
                ClipboardPanel(
                    history = clipboardHistory,
                    onItemClick = { text ->
                        dismissCurrentClip()
                        actionHandler?.onTextInput(text)
                        keyboardState = keyboardState.copy(mode = KeyboardMode.LOWERCASE)
                    },
                    onDeleteItem = onDeleteClipItem,
                    onClearAll = onClearClipboardHistory,
                    onBackToKeyboard = {
                        keyboardState = keyboardState.copy(mode = KeyboardMode.LOWERCASE)
                    },
                    resolvedTheme = resolvedTheme,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (keyboardState.mode == KeyboardMode.VOICE) {
                // ── Full Voice Recording Panel View ─────────────────────────
                VoicePanel(
                    statusText = voiceStatusText,
                    partialText = voicePartialText,
                    rmsDb = voiceRmsDb,
                    isRecording = isVoiceRecording,
                    hasPermission = hasAudioPermission,
                    isOfflineUnavailable = isVoiceOfflineUnavailable,
                    onStartRecording = onStartVoiceRecording,
                    onStopRecording = {
                        onStopVoiceRecording()
                        keyboardState = keyboardState.copy(mode = KeyboardMode.LOWERCASE)
                    },
                    onCancelRecording = {
                        onCancelVoiceRecording()
                        keyboardState = keyboardState.copy(mode = KeyboardMode.LOWERCASE)
                    },
                    onRequestPermission = onRequestAudioPermission,
                    onOpenSettings = onOpenVoiceSettings,
                    resolvedTheme = resolvedTheme,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // ── Live Suggestion Strip (always reserved when Auto Correction is enabled) ──
                if (settings.autoCorrectionEnabled) {
                    SuggestionBar(
                        suggestions = suggestions,
                        onSuggestionClick = { suggestion ->
                            dismissCurrentClip()
                            actionHandler?.onSuggestionSelected(suggestion)
                            refreshSuggestions()
                        },
                        quickPasteText = activeQuickPasteText,
                        onQuickPasteClick = { text ->
                            actionHandler?.onTextInput(text)
                            dismissCurrentClip()
                        },
                        resolvedTheme = resolvedTheme
                    )
                }

                // ── Top Toolbar / Expandable Panel Slot (Fixed 44.dp height) ───────
                when (currentTopPanel) {
                    TopToolbarPanel.NONE -> {
                        TopToolbarRow(
                            onThemeClick = { currentTopPanel = TopToolbarPanel.THEME_SWITCHER },
                            onClipboardClick = { keyboardState = keyboardState.copy(mode = KeyboardMode.CLIPBOARD) },
                            onSettingsClick = onOpenSettings,
                            onVoiceClick = {
                                keyboardState = keyboardState.copy(mode = KeyboardMode.VOICE)
                                onTriggerVoiceInput()
                                onStartVoiceRecording()
                            },
                            onFontsClick = { currentTopPanel = TopToolbarPanel.FONT_STRIP },
                            resolvedTheme = resolvedTheme
                        )
                    }

                    TopToolbarPanel.THEME_SWITCHER -> {
                        ThemeQuickSwitcherBar(
                            themes = availableThemes,
                            currentThemeId = theme?.id,
                            onThemeSelected = onThemeSelected,
                            onCloseClick = { currentTopPanel = TopToolbarPanel.NONE },
                            resolvedTheme = resolvedTheme
                        )
                    }

                    TopToolbarPanel.FONT_STRIP -> {
                        FontSelectionBar(
                            activeFontStyle = activeFontStyle,
                            favoriteFontIds = favoriteFontIds,
                            onFontStyleSelected = onFontStyleSelected,
                            resolvedTheme = resolvedTheme,
                            onCloseClick = { currentTopPanel = TopToolbarPanel.NONE }
                        )
                    }
                }

                val steampunkStyle = resolvedTheme.steampunkIndustrialStyle
                val gearsResId = remember(steampunkStyle) {
                    if (steampunkStyle != null && steampunkStyle.clockworkGearsRes.isNotEmpty()) {
                        context.resources.getIdentifier(steampunkStyle.clockworkGearsRes, "drawable", context.packageName)
                    } else 0
                }

                val glowBrush = resolvedTheme.glassmorphicGlowBrush
                val keyMatrixUnderlayModifier = if (glowBrush != null) {
                    Modifier.background(glowBrush)
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(keyMatrixUnderlayModifier)
                ) {
                    if (gearsResId != 0) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = gearsResId),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                    Column {
                        rows.forEach { row ->
                            val isMiddleRow = (row.size == 9 && row.firstOrNull()?.type == KeyType.CHARACTER)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(rowHeightDp)
                                    .padding(vertical = 1.dp)
                            ) {
                                if (isMiddleRow) {
                                    androidx.compose.foundation.layout.Spacer(
                                        modifier = Modifier.weight(0.5f)
                                    )
                                }

                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .weight(key.widthWeight)
                                            .fillMaxHeight()
                                    ) {
                                        KeyView(
                                            key = key,
                                            isShifted = keyboardState.isShifted,
                                            isCapsLock = keyboardState.isCapsLock,
                                            imeAction = keyboardState.imeAction,
                                            theme = resolvedTheme,
                                            activeFontStyle = activeFontStyle,
                                            showNumberHints = !settings.numberRowEnabled,
                                            onKeyPress = { pressedKey ->
                                                dismissCurrentClip()
                                                handleKeyPress(
                                                    key = pressedKey,
                                                    state = keyboardState,
                                                    actionHandler = actionHandler,
                                                    lastShiftTapTime = lastShiftTapTime,
                                                    onStateChange = { newState ->
                                                        keyboardState = newState
                                                    },
                                                    onShiftTap = { time ->
                                                        lastShiftTapTime = time
                                                    }
                                                )
                                                refreshSuggestions()
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                if (isMiddleRow) {
                                    androidx.compose.foundation.layout.Spacer(
                                        modifier = Modifier.weight(0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Handle a key press event, updating keyboard state and committing text.
 */
private fun handleKeyPress(
    key: KeyData,
    state: KeyboardState,
    actionHandler: KeyboardActionHandler?,
    lastShiftTapTime: Long,
    onStateChange: (KeyboardState) -> Unit,
    onShiftTap: (Long) -> Unit
) {
    when (key.type) {
        KeyType.CHARACTER -> {
            val text = if (state.isShifted) key.output.uppercase() else key.output
            actionHandler?.onTextInput(text)

            // After typing a character in UPPERCASE (not CAPS_LOCK), revert to lowercase
            if (state.mode == KeyboardMode.UPPERCASE) {
                onStateChange(state.copy(mode = KeyboardMode.LOWERCASE))
            }
        }

        KeyType.BACKSPACE -> {
            actionHandler?.onBackspace()
        }

        KeyType.SPACE -> {
            actionHandler?.onSpace()
        }

        KeyType.ENTER -> {
            actionHandler?.onEnter()
        }

        KeyType.EMOJI -> {
            onStateChange(state.copy(mode = KeyboardMode.EMOJI))
        }

        KeyType.SHIFT -> {
            when {
                // In symbols mode, shift toggles between symbols layers
                state.mode == KeyboardMode.SYMBOLS_1 -> {
                    onStateChange(state.copy(mode = KeyboardMode.SYMBOLS_2))
                }
                state.mode == KeyboardMode.SYMBOLS_2 -> {
                    onStateChange(state.copy(mode = KeyboardMode.SYMBOLS_1))
                }
                // In letter mode, handle shift / caps lock
                else -> {
                    val now = System.currentTimeMillis()
                    when (state.mode) {
                        KeyboardMode.LOWERCASE -> {
                            if (now - lastShiftTapTime < DOUBLE_TAP_THRESHOLD_MS) {
                                onStateChange(state.copy(mode = KeyboardMode.CAPS_LOCK))
                            } else {
                                onStateChange(state.copy(mode = KeyboardMode.UPPERCASE))
                            }
                            onShiftTap(now)
                        }
                        KeyboardMode.UPPERCASE -> {
                            if (now - lastShiftTapTime < DOUBLE_TAP_THRESHOLD_MS) {
                                onStateChange(state.copy(mode = KeyboardMode.CAPS_LOCK))
                            } else {
                                onStateChange(state.copy(mode = KeyboardMode.LOWERCASE))
                            }
                            onShiftTap(now)
                        }
                        KeyboardMode.CAPS_LOCK -> {
                            onStateChange(state.copy(mode = KeyboardMode.LOWERCASE))
                            onShiftTap(0L) // Reset
                        }
                        else -> {} // Symbols handled above
                    }
                }
            }
        }

        KeyType.SYMBOLS -> {
            when (state.mode) {
                KeyboardMode.SYMBOLS_1, KeyboardMode.SYMBOLS_2, KeyboardMode.NUMBER_PAD -> {
                    onStateChange(state.copy(mode = KeyboardMode.LOWERCASE))
                }
                else -> {
                    onStateChange(state.copy(mode = KeyboardMode.SYMBOLS_1))
                }
            }
        }

        KeyType.GLOBE -> {
            actionHandler?.onGlobeKey()
        }

        KeyType.HIDE_KEYBOARD -> {
            actionHandler?.onHideKeyboard()
        }
    }
}
