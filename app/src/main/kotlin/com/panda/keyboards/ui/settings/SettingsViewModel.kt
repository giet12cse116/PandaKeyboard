package com.panda.keyboards.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.keyboards.theme.KeyboardHeight
import com.panda.keyboards.theme.KeyboardSettings
import com.panda.keyboards.theme.QwertyOrder
import com.panda.keyboards.theme.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing settings state and preference mutations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<KeyboardSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = KeyboardSettings()
        )

    fun setQwertyOrder(order: QwertyOrder) {
        viewModelScope.launch {
            settingsRepository.setQwertyOrder(order)
        }
    }

    fun setKeyboardHeight(height: KeyboardHeight) {
        viewModelScope.launch {
            settingsRepository.setKeyboardHeight(height)
        }
    }

    fun setAutoCorrectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCorrectionEnabled(enabled)
        }
    }

    fun setAutoCapitalizationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCapitalizationEnabled(enabled)
        }
    }

    fun setOfflineModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOfflineModeEnabled(enabled)
        }
    }

    fun setNumberRowEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNumberRowEnabled(enabled)
        }
    }
}

