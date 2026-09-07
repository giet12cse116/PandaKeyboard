package com.panda.keyboards

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Panda Keyboards.
 * Annotated with @HiltAndroidApp to trigger Hilt code generation
 * and serve as the application-level DI component.
 */
@HiltAndroidApp
class PandaKeyboardsApp : Application()
