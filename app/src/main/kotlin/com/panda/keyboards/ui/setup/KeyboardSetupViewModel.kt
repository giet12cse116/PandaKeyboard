package com.panda.keyboards.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.keyboards.ime.ImeStatus
import com.panda.keyboards.ime.ImeStatusChecker
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the dedicated Keyboard Setup screen state and auto-apply workflow.
 */
@HiltViewModel
class KeyboardSetupViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val imeStatusChecker: ImeStatusChecker
) : ViewModel() {

    /** Live IME status flow. */
    val imeStatus: StateFlow<ImeStatus> = imeStatusChecker.imeStatus

    /** Pending theme ID passed into setup from Sprint 10a. */
    private val _pendingThemeId = MutableStateFlow<String?>(null)
    val pendingThemeId: StateFlow<String?> = _pendingThemeId.asStateFlow()

    /** Resolved [KeyboardTheme] preview object corresponding to [pendingThemeId]. */
    val pendingTheme: StateFlow<KeyboardTheme?> = combine(
        themeRepository.themes,
        _pendingThemeId
    ) { catalog, id ->
        if (id != null) catalog.find { it.id == id } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /** Signal indicating setup complete and pending theme auto-applied (triggers navigation to Sprint 10c). */
    private val _setupCompleted = MutableStateFlow(false)
    val setupCompleted: StateFlow<Boolean> = _setupCompleted.asStateFlow()

    init {
        // Automatically apply pending theme and signal completion when IME becomes fully configured
        viewModelScope.launch {
            imeStatusChecker.imeStatus
                .filter { it.isFullyConfigured }
                .collectLatest {
                    val targetId = _pendingThemeId.value
                    if (targetId != null && !_setupCompleted.value) {
                        themeRepository.setSelectedThemeId(targetId)
                        _setupCompleted.value = true
                    } else if (!_setupCompleted.value && targetId == null) {
                        _setupCompleted.value = true
                    }
                }
        }
    }

    /**
     * Initialize setup with the target theme ID passed from Sprint 10a bottom sheet.
     */
    fun initPendingTheme(themeId: String?) {
        _pendingThemeId.value = themeId
        _setupCompleted.value = false
    }

    /**
     * Force re-evaluation of IME status.
     */
    fun refreshImeStatus() {
        imeStatusChecker.refresh()
    }
}
