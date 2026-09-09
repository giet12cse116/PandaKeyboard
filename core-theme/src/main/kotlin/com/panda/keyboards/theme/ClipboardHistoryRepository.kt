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
        internal val DISMISSED_CLIP_ID_KEY = stringPreferencesKey("dismissed_clip_id")
        internal val LAST_SHOWN_CLIP_TEXT_KEY = stringPreferencesKey("last_shown_clip_text")
        const val MAX_ITEMS = 10

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    private val _inMemoryHistory = MutableStateFlow<List<ClipboardItem>>(emptyList())
    private val _inMemoryDismissedClipId = MutableStateFlow<String?>(null)
    private val _inMemoryLastShownClipText = MutableStateFlow<String?>(null)

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
     * Flow emitting the ID of the last dismissed quick paste clip item.
     */
    open val dismissedClipId: Flow<String?> = if (context != null) {
        context.clipboardDataStore.data.map { prefs ->
            prefs[DISMISSED_CLIP_ID_KEY]
        }
    } else {
        _inMemoryDismissedClipId
    }

    /**
     * Flow emitting the raw text of the last shown / dismissed quick paste clip item.
     */
    open val lastShownClipText: Flow<String?> = if (context != null) {
        context.clipboardDataStore.data.map { prefs ->
            prefs[LAST_SHOWN_CLIP_TEXT_KEY]
        }
    } else {
        _inMemoryLastShownClipText
    }

    /**
     * Permanently mark a clip ID as dismissed so quick paste never shows again for it.
     */
    open suspend fun dismissClip(id: String) {
        if (context != null) {
            context.clipboardDataStore.edit { prefs ->
                prefs[DISMISSED_CLIP_ID_KEY] = id
                // Also look up text for this clip ID if present, to mark text as shown
                val jsonString = prefs[CLIPBOARD_HISTORY_KEY]
                if (!jsonString.isNullOrEmpty()) {
                    try {
                        val current = json.decodeFromString<List<ClipboardItem>>(jsonString)
                        val item = current.firstOrNull { it.id == id }
                        if (item != null) {
                            prefs[LAST_SHOWN_CLIP_TEXT_KEY] = item.text
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        } else {
            _inMemoryDismissedClipId.value = id
            val item = _inMemoryHistory.value.firstOrNull { it.id == id }
            if (item != null) {
                _inMemoryLastShownClipText.value = item.text
            }
        }
    }

    /**
     * Mark a specific text snippet as shown / dismissed for quick paste.
     */
    open suspend fun markClipAsShown(text: String) {
        if (text.isBlank()) return
        if (context != null) {
            context.clipboardDataStore.edit { prefs ->
                prefs[LAST_SHOWN_CLIP_TEXT_KEY] = text
            }
        } else {
            _inMemoryLastShownClipText.value = text
        }
    }

    /**
     * Add a newly copied text snippet to clipboard history.
     * Deduplicates exact text matches, places entry at top, and caps history at 10 items.
     */
    open suspend fun addClip(text: String) {
        if (text.isBlank()) return

        if (context != null) {
            context.clipboardDataStore.edit { prefs ->
                val current = try {
                    val jsonString = prefs[CLIPBOARD_HISTORY_KEY]
                    if (jsonString.isNullOrEmpty()) emptyList()
                    else json.decodeFromString<List<ClipboardItem>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }

                // If the top item already has identical text, preserve it to prevent ID mutation
                if (current.firstOrNull()?.text == text) {
                    return@edit
                }

                val newItem = ClipboardItem(text = text, timestamp = System.currentTimeMillis())
                val filtered = current.filter { it.text != text }
                val updated = (listOf(newItem) + filtered).take(MAX_ITEMS)
                prefs[CLIPBOARD_HISTORY_KEY] = json.encodeToString(updated)
            }
        } else {
            val current = _inMemoryHistory.value
            if (current.firstOrNull()?.text == text) {
                return
            }
            val newItem = ClipboardItem(text = text, timestamp = System.currentTimeMillis())
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
