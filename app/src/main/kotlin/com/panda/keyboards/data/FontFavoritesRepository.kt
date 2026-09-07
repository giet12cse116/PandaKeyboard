package com.panda.keyboards.data

import com.panda.keyboards.theme.FontPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adapter delegating font favorites management to [FontPreferencesRepository].
 */
@Singleton
class FontFavoritesRepository @Inject constructor(
    private val fontPreferencesRepository: FontPreferencesRepository
) {
    val favorites: Flow<Set<String>> = fontPreferencesRepository.favorites
    val selectedStyleId: Flow<String> = fontPreferencesRepository.selectedStyleId

    suspend fun toggleFavorite(styleId: String) {
        fontPreferencesRepository.toggleFavorite(styleId)
    }

    suspend fun setSelectedStyleId(styleId: String) {
        fontPreferencesRepository.setSelectedStyleId(styleId)
    }
}
