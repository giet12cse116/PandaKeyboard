package com.panda.keyboards.ui.fonts

import com.panda.keyboards.fonts.FontStyle

/**
 * Tab options for the Fonts screen.
 */
enum class FontTab(val label: String) {
    ALL("All Fonts"),
    FAVORITES("Favorites")
}

/**
 * Immutable UI state for the Fonts screen.
 *
 * The ViewModel is the single source of truth for this state;
 * Composables read it but never mutate it directly.
 */
data class FontsUiState(
    /** The user's input text to preview in different font styles. */
    val inputText: String = "Panda Keyboards",

    /** The currently selected font style for the preview card. */
    val selectedStyle: FontStyle = FontStyle.NORMAL,

    /** All available font styles from the catalog. */
    val allStyles: List<FontStyle> = emptyList(),

    /** Set of favorited font style IDs. */
    val favoriteIds: Set<String> = emptySet(),

    /** Which tab is currently selected. */
    val selectedTab: FontTab = FontTab.ALL,

    /** Whether the clipboard copy was just triggered (for snackbar/toast). */
    val showCopiedFeedback: Boolean = false
)
