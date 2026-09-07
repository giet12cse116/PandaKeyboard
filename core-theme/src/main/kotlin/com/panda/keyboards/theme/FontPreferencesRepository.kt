package com.panda.keyboards.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

private val Context.fontPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "panda_font_prefs"
)

/**
 * Repository managing persisted font favorites and last used [FontStyle] selection.
 *
 * Placed in `:core-theme` so both `:app` (Fonts gallery UI) and `:ime`
 * (in-keyboard font selection strip) can share preference state without
 * circular dependencies.
 */
open class FontPreferencesRepository(private val context: Context?) {

    companion object {
        internal val FAVORITES_KEY = stringSetPreferencesKey("font_favorites")
        internal val SELECTED_STYLE_KEY = stringPreferencesKey("selected_font_style_id")
        const val DEFAULT_STYLE_ID = "NORMAL"
    }

    /**
     * Observable set of favorited font style IDs.
     */
    open val favorites: Flow<Set<String>> = context?.fontPreferencesDataStore?.data?.map { prefs ->
        prefs[FAVORITES_KEY] ?: emptySet()
    } ?: flowOf(emptySet())

    /**
     * Observable last selected font style ID.
     */
    open val selectedStyleId: Flow<String> = context?.fontPreferencesDataStore?.data?.map { prefs ->
        prefs[SELECTED_STYLE_KEY] ?: DEFAULT_STYLE_ID
    } ?: flowOf(DEFAULT_STYLE_ID)

    /**
     * Toggle a font style's favorite status.
     */
    open suspend fun toggleFavorite(styleId: String) {
        context?.fontPreferencesDataStore?.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            prefs[FAVORITES_KEY] = if (styleId in current) current - styleId else current + styleId
        }
    }

    /**
     * Set the currently active font style ID for typing output.
     */
    open suspend fun setSelectedStyleId(styleId: String) {
        context?.fontPreferencesDataStore?.edit { prefs ->
            prefs[SELECTED_STYLE_KEY] = styleId
        }
    }
}
