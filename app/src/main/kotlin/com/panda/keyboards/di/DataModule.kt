package com.panda.keyboards.di

import android.content.Context
import com.panda.keyboards.data.FontFavoritesRepository
import com.panda.keyboards.theme.FontPreferencesRepository
import com.panda.keyboards.theme.SettingsRepository
import com.panda.keyboards.theme.ThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing data layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFontPreferencesRepository(
        @ApplicationContext context: Context
    ): FontPreferencesRepository = FontPreferencesRepository(context)

    @Provides
    @Singleton
    fun provideFontFavoritesRepository(
        fontPreferencesRepository: FontPreferencesRepository
    ): FontFavoritesRepository = FontFavoritesRepository(fontPreferencesRepository)

    @Provides
    @Singleton
    fun provideThemeRepository(
        @ApplicationContext context: Context
    ): ThemeRepository = ThemeRepository(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository = SettingsRepository(context)
}
