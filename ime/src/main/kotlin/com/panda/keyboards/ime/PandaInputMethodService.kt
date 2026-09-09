package com.panda.keyboards.ime

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.ime.keyboard.PandaKeyboardLayout
import com.panda.keyboards.theme.ClipboardHistoryRepository
import com.panda.keyboards.theme.ClipboardItem
import com.panda.keyboards.theme.EmojiRepository
import com.panda.keyboards.theme.FontPreferencesRepository
import com.panda.keyboards.theme.KeyboardSettings
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.SettingsRepository
import com.panda.keyboards.theme.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val TAG = "PandaInputMethod"

/**
 * Panda Keyboards InputMethodService.
 *
 * Hosts a [ComposeView] containing the [PandaKeyboardLayout] keyboard.
 * Text input is wired to [currentInputConnection] through a [DefaultKeyboardActionHandler].
 *
 * ## Live Settings, Themes, Fonts & Clipboard (Sprint 13)
 *
 * On `onCreateInputView()`, reads current [KeyboardTheme], [KeyboardSettings],
 * [FontPreferencesRepository] active font style, [EmojiRepository] recents,
 * and [ClipboardHistoryRepository] clip history.
 */
class PandaInputMethodService : InputMethodService() {

    private val lifecycleOwner = ImeLifecycleOwner()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var themeJob: Job? = null
    private var settingsJob: Job? = null
    private var fontJob: Job? = null
    private var emojiJob: Job? = null
    private var clipJob: Job? = null

    private var inputViewRef: View? = null

    /** Current IME action from the active editor — drives enter key appearance. */
    private var currentImeAction by mutableIntStateOf(EditorInfo.IME_ACTION_UNSPECIFIED)

    /** Incremented on each new input session to reset keyboard to default alphabet mode. */
    private var inputSessionId by mutableIntStateOf(0)

    /** Current keyboard theme — updated from ThemeRepository on input view creation. */
    private var currentTheme by mutableStateOf<KeyboardTheme?>(null)

    /** Available themes for quick switcher strip. */
    private var availableThemes by mutableStateOf<List<KeyboardTheme>>(emptyList())

    /** Current keyboard settings — updated from SettingsRepository on input view creation. */
    private var currentSettings by mutableStateOf(KeyboardSettings())

    /** Currently active output font style for typing. */
    private var currentActiveStyle by mutableStateOf(FontStyle.NORMAL)

    /** Set of favorited font style IDs from FontPreferencesRepository. */
    private var favoriteFontIds by mutableStateOf<Set<String>>(emptySet())

    /** Recent emojis list from EmojiRepository. */
    private var recentEmojis by mutableStateOf<List<String>>(emptyList())

    /** Clipboard history list from ClipboardHistoryRepository. */
    private var clipboardHistory by mutableStateOf<List<ClipboardItem>>(emptyList())

    /** Persisted dismissed clip ID from ClipboardHistoryRepository. */
    private var persistedDismissedClipId by mutableStateOf<String?>(null)

    /** Persisted last shown / dismissed clip text from ClipboardHistoryRepository. */
    private var persistedLastShownClipText by mutableStateOf<String?>(null)

    /** Voice Input Reactive States */
    private var voiceStatusText by mutableStateOf("")
    private var voicePartialText by mutableStateOf("")
    private var voiceRmsDb by androidx.compose.runtime.mutableFloatStateOf(0f)
    private var isVoiceRecording by mutableStateOf(false)
    private var isVoiceOfflineUnavailable by mutableStateOf(false)
    private var hasAudioPermission by mutableStateOf(true)

    /** Android SpeechRecognizer instance for voice input button. */
    private var speechRecognizer: android.speech.SpeechRecognizer? = null

    /** Manually constructed repositories — no Hilt in the IME process. */
    private lateinit var themeRepository: ThemeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var fontPreferencesRepository: FontPreferencesRepository
    private lateinit var emojiRepository: EmojiRepository
    private lateinit var clipboardHistoryRepository: ClipboardHistoryRepository

    private val clipChangedListener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
        capturePrimaryClip()
    }

    private val actionHandler = DefaultKeyboardActionHandler(
        inputConnectionProvider = { currentInputConnection },
        editorInfoProvider = { currentInputEditorInfo },
        isAutoCapEnabled = { currentSettings.autoCapitalizationEnabled },
        activeFontStyleProvider = { currentActiveStyle },
        onGlobeKeyAction = {
            try {
                if (!switchToNextInputMethod(false)) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                    imm?.showInputMethodPicker()
                }
            } catch (e: Exception) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.showInputMethodPicker()
            }
        },
        onHideKeyboardAction = {
            requestHideSelf(0)
        }
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called")
        lifecycleOwner.onCreate()
        themeRepository = ThemeRepository(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        fontPreferencesRepository = FontPreferencesRepository(applicationContext)
        emojiRepository = EmojiRepository(applicationContext)
        clipboardHistoryRepository = ClipboardHistoryRepository(applicationContext)
        loadCurrentTheme()
        loadCurrentSettings()
        loadFontPreferences()
        loadEmojiPreferences()
        loadClipboardHistory()

        try {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipboardManager?.addPrimaryClipChangedListener(clipChangedListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering clip listener: ${e.message}", e)
        }
    }

    override fun onCreateInputView(): View {
        Log.d(TAG, "onCreateInputView() called")
        try {
            loadCurrentTheme()
            loadCurrentSettings()
            loadFontPreferences()
            loadEmojiPreferences()
            loadClipboardHistory()

            // Attach lifecycle owner to the SoftInputWindow decor view so WindowRecomposer finds it when traversing up parentPanel
            window?.window?.decorView?.let { decorView ->
                lifecycleOwner.attachToDecorView(decorView)
            }

            val composeView = ComposeView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lifecycleOwner.attachToDecorView(this)

                setContent {
                    PandaKeyboardLayout(
                        actionHandler = actionHandler,
                        imeAction = currentImeAction,
                        editorInfo = currentInputEditorInfo,
                        theme = currentTheme,
                        settings = currentSettings,
                        activeFontStyle = currentActiveStyle,
                        favoriteFontIds = favoriteFontIds,
                        availableThemes = availableThemes,
                        recentEmojis = recentEmojis,
                        clipboardHistory = clipboardHistory,
                        persistedDismissedClipId = persistedDismissedClipId,
                        persistedLastShownClipText = persistedLastShownClipText,
                        voiceStatusText = voiceStatusText,
                        voicePartialText = voicePartialText,
                        voiceRmsDb = voiceRmsDb,
                        isVoiceRecording = isVoiceRecording,
                        hasAudioPermission = hasAudioPermission,
                        isVoiceOfflineUnavailable = isVoiceOfflineUnavailable,
                        onStartVoiceRecording = { startVoiceInput() },
                        onStopVoiceRecording = { stopVoiceInput(commit = true) },
                        onCancelVoiceRecording = { stopVoiceInput(commit = false) },
                        onRequestAudioPermission = { checkAudioPermission(); startVoiceInput() },
                        onOpenVoiceSettings = { VoicePermissionActivity.launchAppSettings(applicationContext) },
                        inputSessionId = inputSessionId,
                        onFontStyleSelected = { style ->
                            currentActiveStyle = style
                            saveSelectedStyle(style.id)
                        },
                        onThemeSelected = { themeId ->
                            serviceScope.launch {
                                themeRepository.setSelectedThemeId(themeId)
                            }
                        },
                        onEmojiUsed = { emoji ->
                            serviceScope.launch {
                                emojiRepository.addRecentEmoji(emoji)
                            }
                        },
                        onDeleteClipItem = { id ->
                            serviceScope.launch {
                                clipboardHistoryRepository.removeClip(id)
                            }
                        },
                        onClearClipboardHistory = {
                            serviceScope.launch {
                                clipboardHistoryRepository.clearAll()
                            }
                        },
                        onOpenSettings = {
                            try {
                                val intent = android.content.Intent().apply {
                                    setClassName(packageName, "com.panda.keyboards.ui.settings.SettingsActivity")
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error opening settings: ${e.message}", e)
                            }
                        },
                        onTriggerVoiceInput = {
                            startVoiceInput()
                        },
                        onDismissClip = { clipIdOrText ->
                            serviceScope.launch {
                                clipboardHistoryRepository.dismissClip(clipIdOrText)
                                clipboardHistoryRepository.markClipAsShown(clipIdOrText)
                            }
                        }
                    )
                }
            }
            inputViewRef = composeView
            Log.d(TAG, "onCreateInputView() successfully created ComposeView: $composeView")
            return composeView
        } catch (e: Throwable) {
            Log.e(TAG, "ERROR in onCreateInputView(): ${e.message}", e)
            throw e
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        Log.d(TAG, "onStartInput() called: inputType=${attribute?.inputType}, imeOptions=${attribute?.imeOptions}, restarting=$restarting")
        updateImeAction(attribute)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        Log.d(TAG, "onStartInputView() called: info=$info, restarting=$restarting")
        try {
            super.onStartInputView(info, restarting)
            inputSessionId++
            window?.window?.decorView?.let { decorView ->
                lifecycleOwner.attachToDecorView(decorView)
            }
            updateImeAction(info)
            capturePrimaryClip()
            lifecycleOwner.onResume()

            inputViewRef?.post {
                val height = inputViewRef?.height ?: 0
                val width = inputViewRef?.width ?: 0
                Log.d(TAG, "onStartInputView() inputView measured dimensions: width=$width, height=$height")
            }
        } catch (e: Exception) {
            Log.e(TAG, "ERROR in onStartInputView(): ${e.message}", e)
            throw e
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.d(TAG, "onFinishInputView() called: finishingInput=$finishingInput")
        lifecycleOwner.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event?.repeatCount == 0) {
            if (isInputViewShown) {
                requestHideSelf(0)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        try {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            clipboardManager?.removePrimaryClipChangedListener(clipChangedListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering clip listener: ${e.message}", e)
        }
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer: ${e.message}", e)
        }
        serviceScope.cancel()
        lifecycleOwner.onDestroy()
    }

    private fun checkAudioPermission(): Boolean {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasAudioPermission = granted
        return granted
    }

    private fun startVoiceInput() {
        if (!checkAudioPermission()) {
            voiceStatusText = "Microphone access needed"
            try {
                val intent = android.content.Intent(this, VoicePermissionActivity::class.java).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error launching VoicePermissionActivity: ${e.message}", e)
            }
            return
        }

        // Offline mode & On-device recognition check
        val isOfflineMode = currentSettings.offlineModeEnabled
        val isOnDeviceAvailable = if (android.os.Build.VERSION.SDK_INT >= 31) {
            try {
                android.speech.SpeechRecognizer.isOnDeviceRecognitionAvailable(applicationContext)
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }

        if (isOfflineMode && !isOnDeviceAvailable) {
            isVoiceOfflineUnavailable = true
            voiceStatusText = "Offline mode is active and on-device speech recognition is unavailable"
            isVoiceRecording = false
            return
        } else {
            isVoiceOfflineUnavailable = false
        }

        try {
            stopVoiceInput(commit = false)

            speechRecognizer = if (isOfflineMode && isOnDeviceAvailable && android.os.Build.VERSION.SDK_INT >= 31) {
                android.speech.SpeechRecognizer.createOnDeviceSpeechRecognizer(applicationContext)
            } else {
                android.speech.SpeechRecognizer.createSpeechRecognizer(applicationContext)
            }

            speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    voiceStatusText = "Listening..."
                    isVoiceRecording = true
                }

                override fun onBeginningOfSpeech() {
                    voiceStatusText = "Listening..."
                    isVoiceRecording = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    voiceRmsDb = rmsdB
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    voiceStatusText = "Processing..."
                }

                override fun onError(error: Int) {
                    isVoiceRecording = false
                    voiceRmsDb = 0f
                    voiceStatusText = when (error) {
                        android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No speech heard — tap mic to retry"
                        android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard — tap mic to retry"
                        android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        android.speech.SpeechRecognizer.ERROR_NETWORK -> "Network error — check connection"
                        android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        else -> "Voice recognition error ($error)"
                    }
                }

                override fun onResults(results: android.os.Bundle?) {
                    isVoiceRecording = false
                    voiceRmsDb = 0f
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    val textToCommit = if (!matches.isNullOrEmpty()) matches[0] else voicePartialText
                    if (!textToCommit.isNullOrBlank()) {
                        currentInputConnection?.commitText(textToCommit, 1)
                    }
                    voiceStatusText = ""
                    voicePartialText = ""
                }

                override fun onPartialResults(partialResults: android.os.Bundle?) {
                    val matches = partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        voicePartialText = matches[0]
                    }
                }

                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })

            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.startListening(intent)
            isVoiceRecording = true
            voiceStatusText = "Listening..."
        } catch (e: Exception) {
            Log.e(TAG, "Error starting SpeechRecognizer: ${e.message}", e)
            voiceStatusText = "Unable to start voice recognition"
            isVoiceRecording = false
        }
    }

    private fun stopVoiceInput(commit: Boolean) {
        try {
            if (speechRecognizer != null) {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping SpeechRecognizer: ${e.message}", e)
        }
        if (commit && voicePartialText.isNotBlank()) {
            currentInputConnection?.commitText(voicePartialText, 1)
        }
        isVoiceRecording = false
        voiceRmsDb = 0f
        voicePartialText = ""
        voiceStatusText = ""
    }

    private fun loadCurrentTheme() {
        if (themeJob != null) return
        themeJob = serviceScope.launch {
            try {
                launch {
                    themeRepository.selectedTheme.collect { theme ->
                        currentTheme = theme
                        Log.d(TAG, "Loaded current theme: ${theme?.name} (id=${theme?.id})")
                    }
                }
                launch {
                    themeRepository.themes.collect { list ->
                        availableThemes = list
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading theme: ${e.message}", e)
                currentTheme = null
            }
        }
    }

    private fun loadCurrentSettings() {
        if (settingsJob != null) return
        settingsJob = serviceScope.launch {
            try {
                settingsRepository.settings.collect { settings ->
                    currentSettings = settings
                    Log.d(TAG, "Loaded current settings: $settings")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading settings: ${e.message}", e)
                currentSettings = KeyboardSettings()
            }
        }
    }

    private fun loadFontPreferences() {
        if (fontJob != null) return
        fontJob = serviceScope.launch {
            try {
                launch {
                    fontPreferencesRepository.favorites.collect { favorites ->
                        favoriteFontIds = favorites
                    }
                }
                launch {
                    fontPreferencesRepository.selectedStyleId.collect { styleId ->
                        currentActiveStyle = FontStyle.fromId(styleId) ?: FontStyle.NORMAL
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading font preferences: ${e.message}", e)
                favoriteFontIds = emptySet()
                currentActiveStyle = FontStyle.NORMAL
            }
        }
    }

    private fun loadEmojiPreferences() {
        if (emojiJob != null) return
        emojiJob = serviceScope.launch {
            try {
                emojiRepository.recentEmojis.collect { recents ->
                    recentEmojis = recents
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent emojis: ${e.message}", e)
                recentEmojis = emptyList()
            }
        }
    }

    private fun loadClipboardHistory() {
        if (clipJob != null) return
        clipJob = serviceScope.launch {
            try {
                launch {
                    clipboardHistoryRepository.history.collect { list ->
                        clipboardHistory = list
                    }
                }
                launch {
                    clipboardHistoryRepository.dismissedClipId.collect { id ->
                        persistedDismissedClipId = id
                    }
                }
                launch {
                    clipboardHistoryRepository.lastShownClipText.collect { text ->
                        persistedLastShownClipText = text
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading clipboard history: ${e.message}", e)
                clipboardHistory = emptyList()
            }
        }
    }

    private fun capturePrimaryClip() {
        try {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = clipboardManager?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) {
                    serviceScope.launch {
                        clipboardHistoryRepository.addClip(text)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing primary clip: ${e.message}", e)
        }
    }

    private fun saveSelectedStyle(styleId: String) {
        serviceScope.launch {
            try {
                fontPreferencesRepository.setSelectedStyleId(styleId)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving selected font style: ${e.message}", e)
            }
        }
    }

    private fun updateImeAction(info: EditorInfo?) {
        currentImeAction = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_UNSPECIFIED
    }
}
