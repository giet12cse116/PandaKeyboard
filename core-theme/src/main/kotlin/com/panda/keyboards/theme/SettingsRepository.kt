package com.panda.keyboards.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Top-level DataStore extension property for settings preferences.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "panda_settings_prefs"
)

/**
 * Repository managing persistence and live observation of [KeyboardSettings].
 */
open class SettingsRepository(private val context: Context?) {

    companion object {
        internal val QWERTY_ORDER_KEY = stringPreferencesKey("setting_qwerty_order")
        internal val KEYBOARD_HEIGHT_KEY = stringPreferencesKey("setting_keyboard_height")
        internal val AUTO_CORRECTION_KEY = booleanPreferencesKey("setting_auto_correction")
        internal val OFFLINE_MODE_KEY = booleanPreferencesKey("setting_offline_mode")
        internal val NUMBER_ROW_KEY = booleanPreferencesKey("setting_number_row")
    }

    /**
     * Live [Flow] emitting current [KeyboardSettings] whenever any preference changes.
     */
    open val settings: Flow<KeyboardSettings> = context?.settingsDataStore?.data?.map { prefs ->
        val qwertyOrderStr = prefs[QWERTY_ORDER_KEY] ?: QwertyOrder.QWERTY.name
        val qwertyOrder = try {
            QwertyOrder.valueOf(qwertyOrderStr)
        } catch (e: Exception) {
            QwertyOrder.QWERTY
        }

        val heightStr = prefs[KEYBOARD_HEIGHT_KEY] ?: KeyboardHeight.DEFAULT.name
        val keyboardHeight = try {
            KeyboardHeight.valueOf(heightStr)
        } catch (e: Exception) {
            KeyboardHeight.DEFAULT
        }

        KeyboardSettings(
            qwertyOrder = qwertyOrder,
            keyboardHeight = keyboardHeight,
            autoCorrectionEnabled = prefs[AUTO_CORRECTION_KEY] ?: true,
            offlineModeEnabled = prefs[OFFLINE_MODE_KEY] ?: false,
            numberRowEnabled = prefs[NUMBER_ROW_KEY] ?: false
        )
    } ?: kotlinx.coroutines.flow.flowOf(KeyboardSettings())

    open suspend fun setQwertyOrder(order: QwertyOrder) {
        context?.settingsDataStore?.edit { prefs ->
            prefs[QWERTY_ORDER_KEY] = order.name
        }
    }

    open suspend fun setKeyboardHeight(height: KeyboardHeight) {
        context?.settingsDataStore?.edit { prefs ->
            prefs[KEYBOARD_HEIGHT_KEY] = height.name
        }
    }

    open suspend fun setAutoCorrectionEnabled(enabled: Boolean) {
        context?.settingsDataStore?.edit { prefs ->
            prefs[AUTO_CORRECTION_KEY] = enabled
        }
    }

    open suspend fun setOfflineModeEnabled(enabled: Boolean) {
        context?.settingsDataStore?.edit { prefs ->
            prefs[OFFLINE_MODE_KEY] = enabled
        }
    }

    open suspend fun setNumberRowEnabled(enabled: Boolean) {
        context?.settingsDataStore?.edit { prefs ->
            prefs[NUMBER_ROW_KEY] = enabled
        }
    }
}

