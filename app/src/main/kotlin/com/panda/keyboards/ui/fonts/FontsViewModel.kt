package com.panda.keyboards.ui.fonts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.keyboards.data.FontCatalog
import com.panda.keyboards.data.FontFavoritesRepository
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.ime.ImeStatus
import com.panda.keyboards.ime.ImeStatusChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Fonts screen.
 *
 * Combines the font catalog with persisted favorites into a single
 * [FontsUiState] exposed as a [StateFlow]. All user interactions
 * flow through action methods that update the state.
 */
@HiltViewModel
class FontsViewModel @Inject constructor(
    private val favoritesRepository: FontFavoritesRepository,
    private val imeStatusChecker: ImeStatusChecker
) : ViewModel() {

    val imeStatus: StateFlow<ImeStatus> = imeStatusChecker.imeStatus

    private val _uiState = MutableStateFlow(
        FontsUiState(allStyles = FontCatalog.allStyles)
    )
    val uiState: StateFlow<FontsUiState> = _uiState.asStateFlow()

    init {
        // Observe favorites from DataStore and merge into UI state
        favoritesRepository.favorites
            .onEach { favoriteIds ->
                _uiState.update { it.copy(favoriteIds = favoriteIds) }
            }
            .launchIn(viewModelScope)

        // Observe selected font style from DataStore and update UI state & active keyboard font
        favoritesRepository.selectedStyleId
            .onEach { styleId ->
                val style = FontStyle.fromId(styleId) ?: FontStyle.NORMAL
                _uiState.update { it.copy(selectedStyle = style) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Update the preview input text.
     */
    fun onInputChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    /**
     * Select a font style for the preview card and persist to keyboard font preferences.
     */
    fun onStyleSelected(style: FontStyle) {
        _uiState.update { it.copy(selectedStyle = style) }
        viewModelScope.launch {
            favoritesRepository.setSelectedStyleId(style.id)
        }
    }

    /**
     * Switch between All Fonts and Favorites tabs.
     */
    fun onTabSelected(tab: FontTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Toggle a font style's favorite status.
     * Persists to DataStore; the favorites flow will update the UI state.
     */
    fun onToggleFavorite(styleId: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(styleId)
        }
    }

    /**
     * Signal that the user copied text to clipboard.
     * Shows feedback briefly, then auto-hides.
     */
    fun onCopied() {
        _uiState.update { it.copy(showCopiedFeedback = true) }
        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(showCopiedFeedback = false) }
        }
    }

    /**
     * Dismiss the "copied" feedback.
     */
    fun onCopiedFeedbackDismissed() {
        _uiState.update { it.copy(showCopiedFeedback = false) }
    }
}
