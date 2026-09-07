package com.panda.keyboards.ui.themeeditor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.keyboards.theme.KeyShape
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import com.panda.keyboards.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing draft state for the Custom Theme Editor.
 */
@HiltViewModel
class CustomThemeEditorViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    var selectedTab by mutableIntStateOf(0) // 0 = Choose Colors, 1 = Upload Image
    var themeName by mutableStateOf("My Custom Theme")

    var keyBackgroundColor by mutableStateOf("#2D2D44")
    var keyTextColor by mutableStateOf("#FFFFFF")
    var keyboardBackgroundStartHex by mutableStateOf("#1A1A2E")
    var keyboardBackgroundEndHex by mutableStateOf("#16213E")
    var isGradientBackground by mutableStateOf(true)
    var accentColor by mutableStateOf("#7C4DFF")
    var keyShape by mutableStateOf(KeyShape.ROUNDED)

    // Image Upload & Crop State (Sprint 8b)
    var imageCropSourceBitmap by mutableStateOf<Bitmap?>(null)
    var croppedImagePath by mutableStateOf<String?>(null)

    var isSaving by mutableStateOf(false)

    /**
     * Compute current draft KeyboardTheme model for live preview rendering.
     */
    val currentDraftTheme: KeyboardTheme
        get() {
            val bg = if (selectedTab == 1 && !croppedImagePath.isNullOrEmpty()) {
                ThemeBackground.Image(assetPath = croppedImagePath!!)
            } else if (isGradientBackground) {
                ThemeBackground.Gradient(listOf(keyboardBackgroundStartHex, keyboardBackgroundEndHex), angle = 135f)
            } else {
                ThemeBackground.SolidColor(keyboardBackgroundStartHex)
            }
            return KeyboardTheme(
                id = "draft_preview",
                name = themeName.ifBlank { "Custom Theme" },
                isPro = false,
                isCustom = true,
                keyBackgroundColor = keyBackgroundColor,
                keyTextColor = keyTextColor,
                keyboardBackground = bg,
                keyShape = keyShape,
                accentColor = accentColor
            )
        }

    fun onImagePicked(context: Context, uri: Uri) {
        val loaded = ImageUtils.loadBitmapFromUri(context, uri)
        if (loaded != null) {
            imageCropSourceBitmap = loaded
        }
    }

    fun onCropConfirmed(context: Context, croppedBitmap: Bitmap) {
        val path = ImageUtils.saveBitmapToAppPrivateStorage(context, croppedBitmap)
        croppedImagePath = path
        imageCropSourceBitmap = null
    }

    fun onCropCancelled() {
        imageCropSourceBitmap = null
    }

    fun saveTheme(context: Context, onSaved: (String) -> Unit) {
        if (isSaving) return
        isSaving = true

        viewModelScope.launch {
            try {
                val themeId = "custom_${System.currentTimeMillis()}"
                val finalBg = if (selectedTab == 1 && !croppedImagePath.isNullOrEmpty()) {
                    ThemeBackground.Image(assetPath = croppedImagePath!!)
                } else if (isGradientBackground) {
                    ThemeBackground.Gradient(listOf(keyboardBackgroundStartHex, keyboardBackgroundEndHex), angle = 135f)
                } else {
                    ThemeBackground.SolidColor(keyboardBackgroundStartHex)
                }

                val customTheme = KeyboardTheme(
                    id = themeId,
                    name = themeName.ifBlank { "Custom Theme" },
                    isPro = false,
                    isCustom = true,
                    previewImage = if (selectedTab == 1) croppedImagePath else null,
                    keyBackgroundColor = keyBackgroundColor,
                    keyTextColor = keyTextColor,
                    keyboardBackground = finalBg,
                    keyShape = keyShape,
                    accentColor = accentColor
                )

                themeRepository.customThemeRepository.saveCustomTheme(customTheme)
                themeRepository.setSelectedThemeId(themeId)

                isSaving = false
                onSaved(themeId)
            } catch (e: Exception) {
                isSaving = false
                onSaved(KeyboardTheme.DEFAULT_THEME_ID)
            }
        }
    }
}

