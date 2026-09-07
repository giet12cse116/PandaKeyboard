package com.panda.keyboards.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

private val Context.customThemeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "panda_custom_themes_prefs"
)

/**
 * Repository managing persistence of user-created custom themes.
 *
 * Persists custom [KeyboardTheme] definitions as a serialized JSON list
 * in DataStore Preferences (`panda_custom_themes_prefs`).
 */
open class CustomThemeRepository(private val context: Context?) {

    companion object {
        internal val CUSTOM_THEMES_KEY = stringPreferencesKey("custom_themes_json_list")
    }

    /**
     * Flow of user-created custom themes.
     */
    open val customThemes: Flow<List<KeyboardTheme>> = context?.customThemeDataStore?.data?.map { prefs ->
        val rawJson = prefs[CUSTOM_THEMES_KEY]
        if (rawJson.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                ThemeRepository.json.decodeFromString<List<KeyboardTheme>>(rawJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    } ?: flowOf(emptyList())

    /**
     * Create or update a custom theme.
     */
    open suspend fun saveCustomTheme(theme: KeyboardTheme) {
        if (context == null) return
        context.customThemeDataStore.edit { prefs ->
            val rawJson = prefs[CUSTOM_THEMES_KEY]
            val currentList = if (rawJson.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    ThemeRepository.json.decodeFromString<List<KeyboardTheme>>(rawJson)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val isCustomTheme = theme.copy(isCustom = true, isPro = false)
            val updatedList = currentList.filterNot { it.id == isCustomTheme.id } + isCustomTheme
            prefs[CUSTOM_THEMES_KEY] = ThemeRepository.json.encodeToString(updatedList)
        }
    }

    /**
     * Delete a custom theme by ID.
     */
    open suspend fun deleteCustomTheme(themeId: String) {
        if (context == null) return
        context.customThemeDataStore.edit { prefs ->
            val rawJson = prefs[CUSTOM_THEMES_KEY]
            if (!rawJson.isNullOrBlank()) {
                try {
                    val currentList = ThemeRepository.json.decodeFromString<List<KeyboardTheme>>(rawJson)
                    val updatedList = currentList.filterNot { it.id == themeId }
                    prefs[CUSTOM_THEMES_KEY] = ThemeRepository.json.encodeToString(updatedList)
                } catch (e: Exception) {
                    // Ignore parse error on deletion
                }
            }
        }
    }
}
