package com.panda.keyboards.ui.keyboards

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing theme gallery state, active theme selection, modal bottom sheet presentation,
 * and incremental pagination on scroll.
 */
@HiltViewModel
class KeyboardsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val imeStatusChecker: ImeStatusChecker
) : ViewModel() {

    /** Live IME status flow. */
    val imeStatus: StateFlow<ImeStatus> = imeStatusChecker.imeStatus

    /** Currently selected theme for bottom sheet detail view (null = sheet closed). */
    private val _selectedThemeForSheet = MutableStateFlow<KeyboardTheme?>(null)
    val selectedThemeForSheet: StateFlow<KeyboardTheme?> = _selectedThemeForSheet.asStateFlow()

    /** Set of theme IDs unlocked during current session (PRO themes unlocked via stubs). */
    private val _unlockedThemeIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedThemeIds: StateFlow<Set<String>> = _unlockedThemeIds.asStateFlow()

    /** Selected category filter ("All", "Abstract", "Classic", "Gradient", "Solid", "Custom"). */
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    /** All available themes flow collected into state. */
    val allThemes: StateFlow<List<KeyboardTheme>> = themeRepository.themes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** All themes filtered by currently active category tab (exposed directly to UI). */
    val themes: StateFlow<List<KeyboardTheme>> = combine(
        allThemes,
        _selectedCategory
    ) { catalog, category ->
        when (category.lowercase()) {
            "all" -> {
                val categoryOrder = listOf("modern ui", "abstract", "hd background", "hd_background", "nature", "gradient", "solid", "custom", "classic")
                catalog.sortedWith(compareBy { theme ->
                    if (theme.isCustom) {
                        categoryOrder.indexOf("custom")
                    } else {
                        val idx = categoryOrder.indexOf(theme.category.lowercase())
                        if (idx >= 0) idx else categoryOrder.size
                    }
                })
            }
            "custom" -> catalog.filter { it.isCustom }
            "hd background" -> catalog.filter { it.category.equals("hd background", ignoreCase = true) || it.category.equals("hd_background", ignoreCase = true) }
            "solid" -> catalog.filter { it.category.equals("solid", ignoreCase = true) || (it.category.equals("classic", ignoreCase = true) && it.keyboardBackground is com.panda.keyboards.theme.ThemeBackground.SolidColor) }
            "gradient" -> catalog.filter { it.category.equals("gradient", ignoreCase = true) || (it.category.equals("classic", ignoreCase = true) && it.keyboardBackground is com.panda.keyboards.theme.ThemeBackground.Gradient) }
            else -> catalog.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Switch current category filter.
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    /** Currently selected active theme ID (null on fresh install until user explicitly applies one). */
    val selectedThemeId: StateFlow<String?> = themeRepository.selectedThemeId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /** Number of rewarding ads watched in current session. */
    private val _adsWatchedInSession = MutableStateFlow(0)
    val adsWatchedInSession: StateFlow<Int> = _adsWatchedInSession.asStateFlow()

    /** Number of remaining direct "Apply" credits accumulated in current session. */
    private val _freeApplyCredits = MutableStateFlow(0)
    val freeApplyCredits: StateFlow<Int> = _freeApplyCredits.asStateFlow()

    /**
     * Open bottom sheet for a given theme item.
     */
    fun openThemeSheet(theme: KeyboardTheme) {
        _selectedThemeForSheet.value = theme
    }

    /**
     * Close the bottom sheet.
     */
    fun dismissThemeSheet() {
        _selectedThemeForSheet.value = null
    }

    /**
     * Delete a user-created custom theme.
     */
    fun deleteCustomTheme(themeId: String) {
        viewModelScope.launch {
            if (_selectedThemeForSheet.value?.id == themeId) {
                dismissThemeSheet()
            }
            themeRepository.deleteCustomTheme(themeId)
        }
    }

    /**
     * Unlock a PRO theme (stub for rewarded ad / purchase flow).
     */
    fun unlockTheme(themeId: String) {
        _unlockedThemeIds.value = _unlockedThemeIds.value + themeId
    }

    /**
     * Watch a rewarded ad to unlock and apply a theme.
     *
     * If user watches 2 rewarding ads back-to-back in a session, grants 2 free Apply passes.
     */
    fun watchRewardedAdAndApply(themeId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val newAdCount = _adsWatchedInSession.value + 1
            _adsWatchedInSession.value = newAdCount

            if (newAdCount >= 2) {
                _freeApplyCredits.value = 2
            }

            themeRepository.setSelectedThemeId(themeId)
            dismissThemeSheet()
            onComplete()
        }
    }

    /**
     * Use a free Apply credit pass to apply a theme directly without watching an ad.
     */
    fun consumeFreeApplyCreditAndApply(themeId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_freeApplyCredits.value > 0) {
                _freeApplyCredits.value = _freeApplyCredits.value - 1
            }
            themeRepository.setSelectedThemeId(themeId)
            dismissThemeSheet()
            onComplete()
        }
    }

    /**
     * Apply a theme as the user's active keyboard theme and close the bottom sheet.
     */
    fun applyTheme(themeId: String) {
        viewModelScope.launch {
            themeRepository.setSelectedThemeId(themeId)
            dismissThemeSheet()
        }
    }

    /**
     * Legacy select helper.
     */
    fun selectTheme(themeId: String) {
        applyTheme(themeId)
    }

    /**
     * Force re-evaluation of current IME status.
     */
    fun refreshImeStatus() {
        imeStatusChecker.refresh()
    }
}

