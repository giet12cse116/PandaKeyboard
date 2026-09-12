package com.panda.keyboards.ui.themeeditor

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
 * ViewModel managing state for the 3-Step Custom Theme Editor Wizard.
 */
@HiltViewModel
class CustomThemeEditorViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    // ── Stepper Navigation ──────────────────────────────────────────────────
    var currentStep by mutableIntStateOf(1) // 1 = Background, 2 = Key, 3 = Font & Apply

    // ── Page 1: Background Options ──────────────────────────────────────────
    // 0 = Upload Image, 1 = Background Color (Solid / Gradient), 2 = Choose Background
    var backgroundMode by mutableIntStateOf(2)

    // Color Mode: 0 = Single Color, 1 = 2-Color Gradient, 2 = 3-Color Gradient
    var colorType by mutableIntStateOf(1)
    var colorStop1Hex by mutableStateOf("#10B981")
    var colorStop2Hex by mutableStateOf("#8B5CF6")
    var colorStop3Hex by mutableStateOf("#F59E0B")
    // Gradient Direction: 0 = Horizontal (0°), 1 = Vertical (90°), 2 = Diagonal (135°)
    var gradientDirection by mutableIntStateOf(2)

    // Image Upload & Crop State
    var uncroppedSourceBitmap by mutableStateOf<Bitmap?>(null)
    var showCropScreen by mutableStateOf(false)
    var croppedImagePath by mutableStateOf<String?>(null)

    // Categorized Built-in Wallpapers: 0 = HD Backdrops, 1 = Nature, 2 = Other
    var backgroundCategoryIndex by mutableIntStateOf(0)
    var selectedBuiltinImage by mutableStateOf<String?>(null)

    // ── Page 2: Key Style Options ───────────────────────────────────────────
    // Key Shape: NONE, SQUARE, SQUARE_ROUNDED, OVAL, OVAL_ROUNDED
    var keyShape by mutableStateOf(KeyShape.NONE)
    var keyBackgroundColor by mutableStateOf("#FFFFFF")
    var keyOpacity by mutableFloatStateOf(0.85f) // 0.0f to 1.0f

    // Popular Key Background Decorative Icon Imprints
    var decorativeCategory by mutableStateOf("cute") // cute, sweets, fun, nature
    var decorativeIcon by mutableStateOf<String?>(null) // Default to null ("None")

    // ── Page 3: Font & Text Options ─────────────────────────────────────────
    var keyTextColor by mutableStateOf("#FFFFFF")
    var fontStyleName by mutableStateOf("rounded") // default, bold, rounded, modern, playful
    var fontSizeSp by mutableIntStateOf(16) // 14 (Small), 16 (Medium), 18 (Large)
    var hasTextShadow by mutableStateOf(true)
    var accentColor by mutableStateOf("#10B981")
    var themeName by mutableStateOf("")

    // ── Interactive Keyboard Preview Modal ──────────────────────────────────
    var showPreviewModal by mutableStateOf(false)
    var showPreviewBoard by mutableStateOf(true)
    var isSaving by mutableStateOf(false)

    /**
     * Compute current draft KeyboardTheme model for live preview rendering.
     */
    val currentDraftTheme: KeyboardTheme
        get() {
            val angle = when (gradientDirection) {
                0 -> 0f   // Horizontal
                1 -> 90f  // Vertical
                else -> 135f // Diagonal
            }

            val bg: ThemeBackground = when (backgroundMode) {
                0 -> {
                    if (!croppedImagePath.isNullOrEmpty()) {
                        ThemeBackground.Image(assetPath = croppedImagePath!!)
                    } else {
                        ThemeBackground.SolidColor(colorStop1Hex)
                    }
                }
                1 -> {
                    when (colorType) {
                        0 -> ThemeBackground.SolidColor(colorStop1Hex)
                        1 -> ThemeBackground.Gradient(listOf(colorStop1Hex, colorStop2Hex), angle = angle)
                        else -> ThemeBackground.Gradient(listOf(colorStop1Hex, colorStop2Hex, colorStop3Hex), angle = angle)
                    }
                }
                2 -> {
                    val builtinImg = selectedBuiltinImage
                    if (!builtinImg.isNullOrEmpty()) {
                        ThemeBackground.Image(assetPath = builtinImg)
                    } else {
                        ThemeBackground.SolidColor("#1C1C1E")
                    }
                }
                else -> ThemeBackground.SolidColor("#1C1C1E")
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
                keyOpacityAlpha = keyOpacity,
                accentColor = accentColor,
                decorativeIcon = decorativeIcon,
                fontSizeSp = fontSizeSp,
                fontStyleName = fontStyleName,
                hasTextShadow = hasTextShadow
            )
        }

    fun onImagePicked(context: Context, uri: Uri) {
        val loaded = ImageUtils.loadBitmapFromUri(context, uri)
        if (loaded != null) {
            uncroppedSourceBitmap = loaded
            croppedImagePath = null
            showCropScreen = true
            backgroundMode = 0
        }
    }

    fun openCropScreen() {
        if (uncroppedSourceBitmap != null) {
            showCropScreen = true
        }
    }

    fun onCropConfirmed(context: Context, croppedBitmap: Bitmap) {
        val path = ImageUtils.saveBitmapToAppPrivateStorage(context, croppedBitmap)
        croppedImagePath = path
        showCropScreen = false
        backgroundMode = 0
    }

    fun onCropCancelled() {
        showCropScreen = false
    }

    fun nextStep() {
        if (currentStep < 3) {
            currentStep++
            if (currentStep == 2) {
                showPreviewBoard = false
            }
        }
    }

    fun previousStep() {
        if (currentStep > 1) {
            currentStep--
            if (currentStep == 2) {
                showPreviewBoard = false
            }
        }
    }

    fun goToStep(step: Int) {
        if (step in 1..3) {
            currentStep = step
            if (step == 2) {
                showPreviewBoard = false
            }
        }
    }

    fun togglePreviewModal() {
        showPreviewModal = !showPreviewModal
    }

    fun resetScreen() {
        currentStep = 1
        backgroundMode = 2
        colorType = 1
        colorStop1Hex = "#10B981"
        colorStop2Hex = "#8B5CF6"
        colorStop3Hex = "#F59E0B"
        gradientDirection = 2
        uncroppedSourceBitmap = null
        showCropScreen = false
        croppedImagePath = null
        backgroundCategoryIndex = 0
        selectedBuiltinImage = null
        keyShape = KeyShape.NONE
        keyBackgroundColor = "#FFFFFF"
        keyOpacity = 0.85f
        decorativeCategory = "cute"
        decorativeIcon = null
        keyTextColor = "#FFFFFF"
        fontStyleName = "rounded"
        fontSizeSp = 16
        hasTextShadow = true
        accentColor = "#10B981"
        themeName = ""
        showPreviewModal = false
        isSaving = false
    }

    fun saveTheme(context: Context, onSaved: (String) -> Unit) {
        if (isSaving) return
        isSaving = true

        viewModelScope.launch {
            try {
                val themeId = "custom_${System.currentTimeMillis()}"
                val angle = when (gradientDirection) {
                    0 -> 0f
                    1 -> 90f
                    else -> 135f
                }
                val builtinImg = selectedBuiltinImage
                val finalBg: ThemeBackground = when (backgroundMode) {
                    0 -> if (!croppedImagePath.isNullOrEmpty()) ThemeBackground.Image(assetPath = croppedImagePath!!) else ThemeBackground.SolidColor(colorStop1Hex)
                    1 -> when (colorType) {
                        0 -> ThemeBackground.SolidColor(colorStop1Hex)
                        1 -> ThemeBackground.Gradient(listOf(colorStop1Hex, colorStop2Hex), angle = angle)
                        else -> ThemeBackground.Gradient(listOf(colorStop1Hex, colorStop2Hex, colorStop3Hex), angle = angle)
                    }
                    2 -> if (!builtinImg.isNullOrEmpty()) ThemeBackground.Image(assetPath = builtinImg) else ThemeBackground.SolidColor("#1C1C1E")
                    else -> ThemeBackground.SolidColor("#1C1C1E")
                }

                val customTheme = KeyboardTheme(
                    id = themeId,
                    name = themeName.ifBlank { "Custom Theme" },
                    isPro = false,
                    isCustom = true,
                    previewImage = if (backgroundMode == 0) croppedImagePath else if (backgroundMode == 2) selectedBuiltinImage else null,
                    keyBackgroundColor = keyBackgroundColor,
                    keyTextColor = keyTextColor,
                    keyboardBackground = finalBg,
                    keyShape = keyShape,
                    keyOpacityAlpha = keyOpacity,
                    accentColor = accentColor,
                    decorativeIcon = decorativeIcon,
                    fontSizeSp = fontSizeSp,
                    fontStyleName = fontStyleName,
                    hasTextShadow = hasTextShadow
                )

                themeRepository.customThemeRepository.saveCustomTheme(customTheme)
                themeRepository.setSelectedThemeId(themeId)

                resetScreen()
                onSaved(themeId)
            } catch (e: Exception) {
                resetScreen()
                onSaved(KeyboardTheme.DEFAULT_THEME_ID)
            }
        }
    }
}
