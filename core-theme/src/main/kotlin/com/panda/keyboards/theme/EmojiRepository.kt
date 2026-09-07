package com.panda.keyboards.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.emojiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "panda_emoji_prefs"
)

/**
 * Repository for persisting recently used emojis.
 */
open class EmojiRepository(private val context: Context?) {

    companion object {
        internal val RECENT_EMOJIS_KEY = stringPreferencesKey("recent_emojis_json")
        private const val MAX_RECENT_EMOJIS = 35

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val DEFAULT_RECENTS = listOf(
            "😂", "❤️", "😍", "🔥", "👍", "😊", "🙏", "✨", "🥺", "🎉"
        )
    }

    private val _inMemoryRecents = MutableStateFlow(DEFAULT_RECENTS)

    /** Flow of recently used emojis, ordered by most recent first. */
    open val recentEmojis: Flow<List<String>> = if (context != null) {
        context.emojiDataStore.data.map { prefs ->
            val jsonString = prefs[RECENT_EMOJIS_KEY]
            if (jsonString.isNullOrEmpty()) {
                DEFAULT_RECENTS
            } else {
                try {
                    json.decodeFromString<List<String>>(jsonString)
                } catch (e: Exception) {
                    DEFAULT_RECENTS
                }
            }
        }
    } else {
        _inMemoryRecents
    }

    /**
     * Record an emoji usage, moving it to the top of recents.
     */
    open suspend fun addRecentEmoji(emoji: String) {
        if (emoji.isBlank()) return
        if (context != null) {
            context.emojiDataStore.edit { prefs ->
                val current = try {
                    val jsonString = prefs[RECENT_EMOJIS_KEY]
                    if (jsonString.isNullOrEmpty()) DEFAULT_RECENTS
                    else json.decodeFromString<List<String>>(jsonString)
                } catch (e: Exception) {
                    DEFAULT_RECENTS
                }

                val updated = (listOf(emoji) + current.filter { it != emoji }).take(MAX_RECENT_EMOJIS)
                prefs[RECENT_EMOJIS_KEY] = json.encodeToString(updated)
            }
        } else {
            val current = _inMemoryRecents.value
            _inMemoryRecents.value = (listOf(emoji) + current.filter { it != emoji }).take(MAX_RECENT_EMOJIS)
        }
    }
}
