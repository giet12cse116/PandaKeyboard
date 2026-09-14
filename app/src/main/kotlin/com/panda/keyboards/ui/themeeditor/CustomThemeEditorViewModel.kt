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
import com.panda.keyboards.ime.ImeStatusChecker
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
    private val themeRepository: ThemeRepository,
    val imeStatusChecker: ImeStatusChecker
) : ViewModel() {

    val imeStatus = imeStatusChecker.imeStatus

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

    // ── Interactive Keyboard Preview Modal & Applied Theme Dialogs ─────────
    var showPreviewModal by mutableStateOf(false)
    var showPreviewBoard by mutableStateOf(true)
    var isSaving by mutableStateOf(false)

    var savedThemeForDialog by mutableStateOf<KeyboardTheme?>(null)
    var showThemeReadyDialog by mutableStateOf(false)
    var showSetupDialog by mutableStateOf(false)

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

            val resolvedKeyStyle = when (decorativeIcon) {
                "ic_popular_sun", "sun", "theme_sun" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_sun_space", "theme_sun_special", "theme_sun_emoji")
                "ic_popular_sunflower", "sunflower", "theme_sunflower" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_sunflower_space", "theme_sunflower_special", "theme_sunflower_emoji")
                "ic_popular_burger", "burger", "theme_burger" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_burger_space", "theme_burger_special", "theme_burger_emoji")
                "ic_popular_pizza", "pizza", "theme_pizza" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_pizza_space", "theme_pizza_special", "theme_pizza_emoji")
                "ic_popular_cookie", "cookie", "theme_cookie" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_cookie_space", "theme_cookie_special", "theme_cookie_emoji")
                "ic_popular_donut", "donut", "theme_donut" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_donut_space", "theme_donut_special", "theme_donut_emoji")
                "ic_popular_mango", "mango", "theme_mango" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_mango_space", "theme_mango_special", "theme_mango_emoji")
                "ic_popular_heart", "heart", "theme_heart" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_heart_space", "theme_heart_special", "theme_heart_emoji")
                "ic_popular_star", "star", "theme_star" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_star_space", "theme_star_special", "theme_star_emoji")
                "ic_popular_panda", "panda", "theme_panda" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_panda_space", "theme_panda_special", "theme_panda_emoji")
                "ic_popular_pig", "pig", "theme_pig" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_pig_space", "theme_pig_special", "theme_pig_emoji")
                "ic_popular_flower", "flower", "theme_flower" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_flower_space", "theme_flower_special", "theme_flower_emoji")
                "ic_popular_fire", "fire", "theme_fire" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_fire_space", "theme_fire_special", "theme_fire_emoji")
                "ic_popular_earth", "earth", "theme_earth" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_earth_space", "theme_earth_special", "theme_earth_emoji")
                "ic_popular_butterfly", "butterfly", "theme_butterfly" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_butterfly_space", "theme_butterfly_special", "theme_butterfly_emoji")
                "ic_popular_football", "football", "theme_football" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_football_space", "theme_football_special", "theme_football_emoji")
                "ic_popular_basketball", "basketball", "theme_basketball" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_basketball_space", "theme_basketball_special", "theme_basketball_emoji")
                "ic_popular_gift", "gift", "theme_gift" -> com.panda.keyboards.theme.KeyVisualStyle.ThemeKeyBackgrounds("theme_gift_space", "theme_gift_special", "theme_gift_emoji")
                else -> com.panda.keyboards.theme.KeyVisualStyle.Flat
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
                keyStyle = resolvedKeyStyle,
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
                    keyStyle = currentDraftTheme.keyStyle,
                    fontSizeSp = fontSizeSp,
                    fontStyleName = fontStyleName,
                    hasTextShadow = hasTextShadow
                )

                themeRepository.customThemeRepository.saveCustomTheme(customTheme)
                themeRepository.setSelectedThemeId(themeId)

                savedThemeForDialog = customTheme
                isSaving = false

                if (!imeStatusChecker.imeStatus.value.isFullyConfigured) {
                    showSetupDialog = true
                } else {
                    showThemeReadyDialog = true
                }
            } catch (e: Exception) {
                isSaving = false
                resetScreen()
                onSaved(KeyboardTheme.DEFAULT_THEME_ID)
            }
        }
    }
}
