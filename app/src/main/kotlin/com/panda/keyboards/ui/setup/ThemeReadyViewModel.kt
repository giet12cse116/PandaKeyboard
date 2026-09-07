package com.panda.keyboards.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel managing the Theme Ready confirmation screen state.
 */
@HiltViewModel
class ThemeReadyViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    /** Applied theme ID passed to this screen. */
    private val _appliedThemeId = MutableStateFlow<String?>(null)
    val appliedThemeId: StateFlow<String?> = _appliedThemeId.asStateFlow()

    /** Resolved [KeyboardTheme] preview object corresponding to [appliedThemeId] (falling back to selectedThemeId). */
    val appliedTheme: StateFlow<KeyboardTheme?> = combine(
        themeRepository.themes,
        themeRepository.selectedThemeId,
        _appliedThemeId
    ) { catalog, selectedId, passedId ->
        val targetId = passedId ?: selectedId
        if (targetId != null) catalog.find { it.id == targetId } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /**
     * Initialize the screen with the applied theme ID.
     */
    fun initAppliedTheme(themeId: String?) {
        _appliedThemeId.value = themeId
    }
}
