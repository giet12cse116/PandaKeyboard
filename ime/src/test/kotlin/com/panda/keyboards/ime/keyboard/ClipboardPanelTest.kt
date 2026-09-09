package com.panda.keyboards.ime.keyboard

import com.panda.keyboards.theme.ClipboardHistoryRepository
import com.panda.keyboards.theme.ClipboardItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Clipboard History Part 2 feature.
 *
 * Tests:
 * - [KeyboardMode] enum contains [KeyboardMode.CLIPBOARD]
 * - [ClipboardHistoryRepository] item addition, 10-item capping, deduplication, deletion, clearAll
 * - Relative timestamp formatting ("Just now", "2m ago", "1h ago", "2d ago")
 */
class ClipboardPanelTest {

    @Test
    fun `keyboardMode includes CLIPBOARD mode`() {
        val modes = KeyboardMode.entries
        assertTrue("KeyboardMode must contain CLIPBOARD mode", modes.contains(KeyboardMode.CLIPBOARD))
    }

    @Test
    fun `repository adds clips and orders most recent first`() = runBlocking {
        val repo = ClipboardHistoryRepository(context = null)
        repo.addClip("First clip")
        repo.addClip("Second clip")

        val history = repo.history.first()
        assertEquals(2, history.size)
        assertEquals("Second clip", history[0].text)
        assertEquals("First clip", history[1].text)
    }

    @Test
    fun `repository caps history at 10 items max`() = runBlocking {
        val repo = ClipboardHistoryRepository(context = null)
        for (i in 1..15) {
            repo.addClip("Clip snippet #$i")
        }

        val history = repo.history.first()
        assertEquals("History must be capped at 10 items maximum", 10, history.size)
        assertEquals("Clip snippet #15", history.first().text)
        assertEquals("Clip snippet #6", history.last().text)
    }

    @Test
    fun `repository deduplicates repeated clip strings`() = runBlocking {
        val repo = ClipboardHistoryRepository(context = null)
        repo.addClip("Unique entry A")
        repo.addClip("Unique entry B")
        repo.addClip("Unique entry A") // re-copied

        val history = repo.history.first()
        assertEquals("Deduplicated history length must be 2", 2, history.size)
        assertEquals("Unique entry A", history[0].text)
        assertEquals("Unique entry B", history[1].text)
    }

    @Test
    fun `repository removes single clip entry by id`() = runBlocking {
        val repo = ClipboardHistoryRepository(context = null)
        repo.addClip("Snippet X")
        repo.addClip("Snippet Y")

        val historyBefore = repo.history.first()
        val itemToRemove = historyBefore.first { it.text == "Snippet X" }

        repo.removeClip(itemToRemove.id)

        val historyAfter = repo.history.first()
        assertEquals(1, historyAfter.size)
        assertEquals("Snippet Y", historyAfter[0].text)
    }

    @Test
    fun `repository clearAll wipes all entries`() = runBlocking {
        val repo = ClipboardHistoryRepository(context = null)
        repo.addClip("Item 1")
        repo.addClip("Item 2")
        repo.addClip("Item 3")

        repo.clearAll()

        val history = repo.history.first()
        assertTrue("History must be empty after clearAll()", history.isEmpty())
    }

    @Test
    fun `formatRelativeTimestamp formats seconds minutes hours and days correctly`() {
        val now = 1_000_000_000_000L

        // Just now (< 60s)
        assertEquals("Just now", formatRelativeTimestamp(now - 10_000L, nowMs = now))
        assertEquals("Just now", formatRelativeTimestamp(now - 59_000L, nowMs = now))

        // Minutes (1m to 59m)
        assertEquals("2m ago", formatRelativeTimestamp(now - 120_000L, nowMs = now))
        assertEquals("45m ago", formatRelativeTimestamp(now - 45 * 60 * 1000L, nowMs = now))

        // Hours (1h to 23h)
        assertEquals("1h ago", formatRelativeTimestamp(now - 3600_000L, nowMs = now))
        assertEquals("5h ago", formatRelativeTimestamp(now - 5 * 3600_000L, nowMs = now))

        // Days (>= 24h)
        assertEquals("1d ago", formatRelativeTimestamp(now - 24 * 3600_000L, nowMs = now))
        assertEquals("3d ago", formatRelativeTimestamp(now - 3 * 24 * 3600_000L, nowMs = now))
    }

    @Test
    fun `quick paste visibility logic displays only once per distinct text copy and ignores session reopens`() = runBlocking {
        fun resolveQuickPasteText(
            history: List<ClipboardItem>,
            currentClipId: String?,
            dismissedClipId: String?,
            persistedLastShownClipText: String?,
            hasKeyPressed: Boolean
        ): String? {
            val latestClip = history.firstOrNull()
            val latestClipId = latestClip?.id
            val latestClipText = latestClip?.text
            val activeHasKeyPressed = if (latestClipId != currentClipId) false else hasKeyPressed
            val showQuickPaste = latestClipText != null &&
                    latestClipText.isNotBlank() &&
                    !activeHasKeyPressed &&
                    latestClipId != dismissedClipId &&
                    latestClipText != persistedLastShownClipText
            return if (showQuickPaste) latestClipText else null
        }

        val repo = ClipboardHistoryRepository(context = null)
        val clip1 = ClipboardItem(id = "1", text = "Hello")
        val clip2 = ClipboardItem(id = "2", text = "World")

        // 1. Initial state with clip1 -> shows "Hello"
        var currentClipId: String? = clip1.id
        var dismissedClipId: String? = null
        var persistedLastShownClipText: String? = null
        var hasKeyPressed = false
        var result = resolveQuickPasteText(listOf(clip1), currentClipId, dismissedClipId, persistedLastShownClipText, hasKeyPressed)
        assertEquals("Hello", result)

        // 2. User presses a key or pastes -> clip text "Hello" marked as shown
        repo.markClipAsShown("Hello")
        persistedLastShownClipText = repo.lastShownClipText.first()
        hasKeyPressed = true
        result = resolveQuickPasteText(listOf(clip1), currentClipId, dismissedClipId, persistedLastShownClipText, hasKeyPressed)
        assertEquals(null, result)

        // 3. Keyboard reopens with same clipboard text "Hello" (even with new UUID item) -> does NOT show "Hello" again
        val clip1Reopen = ClipboardItem(id = "1_new", text = "Hello")
        currentClipId = clip1Reopen.id
        hasKeyPressed = false
        result = resolveQuickPasteText(listOf(clip1Reopen), currentClipId, dismissedClipId, persistedLastShownClipText, hasKeyPressed)
        assertEquals("Same content must not re-trigger quick paste on keyboard reopen", null, result)

        // 4. Genuine new copy event ("World") -> triggers quick paste for "World"
        currentClipId = clip2.id
        hasKeyPressed = false
        result = resolveQuickPasteText(listOf(clip2, clip1Reopen), currentClipId, dismissedClipId, persistedLastShownClipText, hasKeyPressed)
        assertEquals("World", result)

        // 5. User pastes "World" -> dismisses and marks "World" as shown
        repo.markClipAsShown("World")
        persistedLastShownClipText = repo.lastShownClipText.first()
        result = resolveQuickPasteText(listOf(clip2, clip1Reopen), currentClipId, dismissedClipId, persistedLastShownClipText, hasKeyPressed)
        assertEquals(null, result)
    }
}

