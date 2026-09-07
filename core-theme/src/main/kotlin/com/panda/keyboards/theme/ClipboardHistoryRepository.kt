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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Data model for a clipboard history entry.
 *
 * @property id Unique identifier for the item.
 * @property text Raw copied string content.
 * @property timestamp Epoch time in milliseconds when the clip was recorded.
 */
@Serializable
data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

private val Context.clipboardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "panda_clipboard_prefs"
)

/**
 * Repository managing persisted clipboard history entries (up to 10 entries max).
 *
 * Exposed as a [Flow] of [ClipboardItem] lists, ordered most recent first.
 * Supports both Android DataStore persistence and in-memory operations for testing.
 */
open class ClipboardHistoryRepository(private val context: Context?) {

    companion object {
        internal val CLIPBOARD_HISTORY_KEY = stringPreferencesKey("clipboard_history_json")
        const val MAX_ITEMS = 10

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val _inMemoryHistory = MutableStateFlow<List<ClipboardItem>>(emptyList())

    /**
     * Flow of copied clipboard history items, most recent first, capped at 10 items.
     */
    open val history: Flow<List<ClipboardItem>> = if (context != null) {
        context.clipboardDataStore.data.map { prefs ->
            val jsonString = prefs[CLIPBOARD_HISTORY_KEY]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    json.decodeFromString<List<ClipboardItem>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    } else {
        _inMemoryHistory
    }

    /**
     * Add a newly copied text snippet to clipboard history.
     * Deduplicates exact text matches, places entry at top, and caps history at 10 items.
     */
    open suspend fun addClip(text: String) {
        if (text.isBlank()) return
        val newItem = ClipboardItem(text = text, timestamp = System.currentTimeMillis())

        if (context != null) {
            context.clipboardDataStore.edit { prefs ->
                val current = try {
                    val jsonString = prefs[CLIPBOARD_HISTORY_KEY]
                    if (jsonString.isNullOrEmpty()) emptyList()
                    else json.decodeFromString<List<ClipboardItem>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }

                val filtered = current.filter { it.text != text }
                val updated = (listOf(newItem) + filtered).take(MAX_ITEMS)
                prefs[CLIPBOARD_HISTORY_KEY] = json.encodeToString(updated)
            }
        } else {
            val current = _inMemoryHistory.value
            val filtered = current.filter { it.text != text }
            _inMemoryHistory.value = (listOf(newItem) + filtered).take(MAX_ITEMS)
        }
    }

    /**
     * Delete a single entry from clipboard history by its ID.
     */
    open suspend fun removeClip(id: String) {
        if (context != null) {
            context.clipboardDataStore.edit { prefs ->
                val current = try {
                    val jsonString = prefs[CLIPBOARD_HISTORY_KEY]
                    if (jsonString.isNullOrEmpty()) emptyList()
                    else json.decodeFromString<List<ClipboardItem>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }

                val updated = current.filter { it.id != id }
                prefs[CLIPBOARD_HISTORY_KEY] = json.encodeToString(updated)
            }
        } else {
            val current = _inMemoryHistory.value
            _inMemoryHistory.value = current.filter { it.id != id }
        }
    }

    /**
     * Clear all clipboard history entries.
     */
    open suspend fun clearAll() {
        if (context != null) {
            context.clipboardDataStore.edit { prefs ->
                prefs.remove(CLIPBOARD_HISTORY_KEY)
            }
        } else {
            _inMemoryHistory.value = emptyList()
        }
    }
}
