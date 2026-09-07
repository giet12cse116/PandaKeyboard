package com.panda.keyboards.di

import com.panda.keyboards.ime.ImeStatusChecker
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for IME-related dependencies in the :app module.
 *
 * [ImeStatusChecker] is constructor-injected with @Singleton + @Inject,
 * so it doesn't need an explicit @Provides method — Hilt creates it
 * automatically. This module exists as a placeholder for any future
 * IME-related bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImeModule
