package com.panda.keyboards.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Extension property to create a single DataStore instance for theme prefs.
 */
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "panda_theme_prefs"
)

/**
 * Repository managing the keyboard theme catalog and user's selected theme.
 *
 * ## Catalog Loading
 * Themes are loaded from `assets/themes.json` using kotlinx.serialization.
 * The catalog is loaded eagerly on construction and cached in memory.
 *
 * ## Selection Persistence
 * The user's selected theme ID is persisted in DataStore Preferences and
 * exposed as a [Flow]. If the persisted ID is invalid (e.g. removed theme),
 * the repository falls back to [KeyboardTheme.DEFAULT_THEME_ID].
 *
 * ## Thread Safety
 * All reads are non-blocking Flows. The catalog is immutable after init.
 * Selection writes go through DataStore (inherently thread-safe).
 */
open class ThemeRepository(private val context: Context?) {

    companion object {
        internal val SELECTED_THEME_KEY = stringPreferencesKey("selected_theme_id")

        /** Lenient JSON parser — ignores unknown fields for forward compat. */
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        /**
         * Parse a themes catalog from a raw JSON string.
         * Useful for testing without Android Context.
         */
        fun parseCatalog(jsonString: String): List<KeyboardTheme> {
            return json.decodeFromString<ThemeCatalog>(jsonString).themes
        }
    }

    val customThemeRepository: CustomThemeRepository = CustomThemeRepository(context)
    private val _bundledThemes = MutableStateFlow<List<KeyboardTheme>>(emptyList())

    /** All available themes from both the bundled catalog and user-created custom themes. */
    open val themes: Flow<List<KeyboardTheme>> = if (context != null) {
        combine(_bundledThemes, customThemeRepository.customThemes) { bundled, custom ->
            bundled + custom
        }
    } else {
        _bundledThemes
    }

    /** The currently selected theme ID, persisted across app restarts. Null if no theme selected yet. */
    open val selectedThemeId: Flow<String?> = if (context != null) {
        combine(
            context.themeDataStore.data.map { prefs -> prefs[SELECTED_THEME_KEY] },
            themes
        ) { id, catalog ->
            if (id == null) null
            else if (catalog.isEmpty() || catalog.any { it.id == id }) id
            else null
        }
    } else {
        MutableStateFlow(null)
    }

    init {
        loadCatalog()
    }

    /**
     * Loads the theme catalog from `assets/themes.json`.
     *
     * Called once on construction. If parsing fails, the catalog
     * remains empty (graceful degradation).
     */
    private fun loadCatalog() {
        if (context == null) return
        try {
            val jsonString = context.assets.open("themes.json")
                .bufferedReader()
                .use { it.readText() }
            _bundledThemes.value = parseCatalog(jsonString)
        } catch (e: Exception) {
            _bundledThemes.value = emptyList()
        }
    }

    /**
     * Update the selected theme ID.
     *
     * @param themeId Theme ID to select, or null to clear selection.
     */
    open suspend fun setSelectedThemeId(themeId: String?) {
        context?.themeDataStore?.edit { prefs ->
            if (themeId != null) {
                prefs[SELECTED_THEME_KEY] = themeId
            } else {
                prefs.remove(SELECTED_THEME_KEY)
            }
        }
    }

    /**
     * Delete a user-created custom theme and reset active selection if it was active.
     */
    open suspend fun deleteCustomTheme(themeId: String) {
        customThemeRepository.deleteCustomTheme(themeId)
        // If the deleted theme was currently selected, fall back to default theme
        context?.themeDataStore?.edit { prefs ->
            if (prefs[SELECTED_THEME_KEY] == themeId) {
                prefs[SELECTED_THEME_KEY] = KeyboardTheme.DEFAULT_THEME_ID
            }
        }
    }

    /**
     * Convenience: get the full [KeyboardTheme] object for the selected theme.
     * Returns null if no theme has been selected yet.
     */
    val selectedTheme: Flow<KeyboardTheme?> = combine(
        selectedThemeId,
        themes
    ) { id, catalog ->
        if (id != null) catalog.find { it.id == id } else null
    }
}

