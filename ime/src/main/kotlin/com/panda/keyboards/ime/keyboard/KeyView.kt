package com.panda.keyboards.ime.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Individual keyboard key composable.
 *
 * Renders a key with theme-driven styling based on its [KeyType].
 * Handles tap events with haptic feedback. Backspace supports
 * press-and-hold for continuous deletion.
 * Top-row letter keys support long-press shortcuts for number insertion.
 *
 * Supports polymorphic [KeyVisualStyle] variants (e.g. Glassmorphic backdrop blur,
 * Neubrutalist drop shadows, etc.).
 *
 * @param key The key data (label, type, width weight).
 * @param isShifted Whether shift is currently active (to uppercase labels).
 * @param isCapsLock Whether caps lock is engaged (for shift key indicator).
 * @param imeAction Current IME action (for enter key label).
 * @param activeFontStyle Currently selected font style for key label rendering.
 * @param theme Resolved theme colors and shapes.
 * @param onKeyPress Callback when the key is pressed.
 * @param modifier Modifier for the key's container.
 */
@Composable
fun KeyView(
    key: KeyData,
    isShifted: Boolean,
    isCapsLock: Boolean,
    imeAction: Int,
    theme: ResolvedTheme,
    activeFontStyle: FontStyle = FontStyle.NORMAL,
    showNumberHints: Boolean = true,
    onKeyPress: (KeyData) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var wasLongPressed by remember { mutableStateOf(false) }

    // Continuous delete for backspace long-press
    LaunchedEffect(isLongPressing) {
        if (isLongPressing && key.type == KeyType.BACKSPACE) {
            // Initial delay before repeat starts
            delay(400)
            while (isLongPressing) {
                onKeyPress(key)
                delay(50) // Fast repeat rate
            }
        }
    }

    val isEnterKey = key.type == KeyType.ENTER
    val backgroundColor = when {
        isPressed -> if (key.type == KeyType.CHARACTER) theme.keyBackgroundPressed else theme.specialKeyBackgroundPressed
        key.type == KeyType.SHIFT && isCapsLock -> theme.accentLight
        key.type == KeyType.SHIFT && isShifted -> theme.accent
        isEnterKey -> theme.accent
        key.type == KeyType.CHARACTER -> theme.keyBackground
        key.type == KeyType.SPACE -> theme.keyBackground
        else -> theme.specialKeyBackground
    }

    val rawLabel = when {
        key.type == KeyType.CHARACTER && isShifted -> key.label.uppercase()
        key.type == KeyType.SPACE -> "space"
        key.type == KeyType.ENTER -> getEnterLabel(imeAction)
        else -> key.label
    }

    val displayLabel = if (key.type == KeyType.CHARACTER && activeFontStyle != FontStyle.NORMAL) {
        FontTransformer.transform(rawLabel, activeFontStyle)
    } else {
        rawLabel
    }

    val fontScale = (theme.fontSizeSp / 16f).coerceIn(0.75f, 1.4f)

    val fontSize = when (key.type) {
        KeyType.CHARACTER -> (21f * fontScale).sp
        KeyType.SPACE -> (13f * fontScale).sp
        KeyType.SHIFT -> (26f * fontScale).sp
        KeyType.BACKSPACE, KeyType.HIDE_KEYBOARD -> (20f * fontScale).sp
        KeyType.ENTER -> (16f * fontScale).sp
        KeyType.SYMBOLS -> (13f * fontScale).sp
        KeyType.GLOBE, KeyType.EMOJI -> (20f * fontScale).sp
    }

    val activeFontFamily = when (theme.fontStyleName.lowercase()) {
        "bold" -> FontFamily.Serif
        "rounded" -> FontFamily.SansSerif
        "modern" -> FontFamily.Monospace
        "playful" -> FontFamily.Cursive
        else -> FontFamily.Default
    }

    val textShadow = if (theme.hasTextShadow) {
        Shadow(
            color = Color.Black.copy(alpha = 0.65f),
            offset = Offset(2f, 3f),
            blurRadius = 4f
        )
    } else null

    val textColor = when {
        key.type == KeyType.SHIFT && (isShifted || isCapsLock) -> Color.White
        isEnterKey -> Color.White
        key.type == KeyType.CHARACTER -> theme.keyText
        else -> theme.specialKeyText
    }

    val semiFlatStyle = theme.keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.SemiFlat
    val glassStyle = theme.keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.Glassmorphic
    val neobrutalistStyle = theme.keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.Neobrutalist
    val claymorphicStyle = theme.keyStyle as? com.panda.keyboards.theme.KeyVisualStyle.Claymorphic
    val density = androidx.compose.ui.platform.LocalDensity.current

    val unpressedElevationDp = when {
        semiFlatStyle != null -> semiFlatStyle.elevationDp
        neobrutalistStyle != null -> neobrutalistStyle.shadowOffsetDp
        claymorphicStyle != null -> claymorphicStyle.elevationDp
        theme.keyShadowOffsetDp > 0f -> theme.keyShadowOffsetDp
        else -> 2.5f
    }

    val pressedElevationDp = when {
        semiFlatStyle != null -> semiFlatStyle.pressedElevationDp
        neobrutalistStyle != null -> 1.0f
        claymorphicStyle != null -> claymorphicStyle.pressedElevationDp
        else -> 0.5f
    }

    val currentShadowOffsetDp = if (isPressed) pressedElevationDp else unpressedElevationDp

    val currentSurfaceOffsetYDp = if (isPressed) {
        (unpressedElevationDp - pressedElevationDp).coerceAtLeast(0f).dp
    } else {
        0.dp
    }

    val isSemiTransparent = backgroundColor.alpha < 0.95f
    val hasShadow = currentShadowOffsetDp > 0f && theme.keyShadowColor != null && !isSemiTransparent
    val hasBorder = theme.keyBorderWidthDp > 0f && theme.keyBorderColor != null

    val blurModifier = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && glassStyle != null && glassStyle.blurRadiusDp > 0f) {
        val blurRadiusPx = with(density) { glassStyle.blurRadiusDp.dp.toPx() }
        Modifier.graphicsLayer {
            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                blurRadiusPx,
                blurRadiusPx,
                android.graphics.Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
        }
    } else {
        // Pre-API 31 Fallback: Gracefully degrade to a semi-transparent solid overlay WITHOUT real blur.
        // Real-time backdrop blur requires RenderEffect (API 31+). On API < 31, graphicsLayer blur is unavailable.
        Modifier
    }

        val scaleFactor = if (isPressed) 0.96f else 1.0f

        Box(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scaleFactor, scaleY = scaleFactor)
                .padding(horizontal = 2.5.dp, vertical = 3.dp)
        ) {
        if (hasShadow) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 0.dp, y = currentShadowOffsetDp.dp)
                    .clip(theme.keyShape)
                    .background(theme.keyShadowColor!!)
            )
        }

        val synthwaveStyle = theme.synthwaveCyberpunkNeonStyle
        if (synthwaveStyle != null) {
            val isModifierKey = key.type == KeyType.SHIFT || key.type == KeyType.BACKSPACE ||
                    key.type == KeyType.ENTER || key.type == KeyType.EMOJI ||
                    key.type == KeyType.SYMBOLS || key.type == KeyType.GLOBE

            val cyanBg = Color(0xFF0C081D)
            val cyanBorder = Color(0xFF00E5FF)
            val cyanText = Color(0xFFFFFFFF)

            val magentaBg = Color(0xFF14081E)
            val magentaBorder = Color(0xFFFF2A85)
            val magentaText = Color(0xFFFF2A85)

            val (baseBg, borderColor, textColor) = if (isModifierKey) {
                Triple(magentaBg, magentaBorder, magentaText)
            } else {
                Triple(cyanBg, cyanBorder, cyanText)
            }

            val currentBg = if (isPressed) borderColor.copy(alpha = 0.25f) else baseBg
            val currentBorderColor = if (isPressed) borderColor else borderColor.copy(alpha = 0.85f)
            val strokeWidth = if (isPressed) 2.dp else 1.5.dp

            val modifierIconRes = when (key.type) {
                KeyType.SHIFT -> synthwaveStyle.shiftArrowIconRes
                KeyType.BACKSPACE -> synthwaveStyle.backspaceTagIconRes
                KeyType.ENTER -> synthwaveStyle.enterArrowIconRes
                KeyType.EMOJI -> synthwaveStyle.emojiSmileIconRes
                else -> null
            }


            val context = androidx.compose.ui.platform.LocalContext.current
            val iconDrawableId = remember(modifierIconRes) {
                if (modifierIconRes != null) {
                    context.resources.getIdentifier(modifierIconRes, "drawable", context.packageName)
                } else 0
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = currentBg,
                border = BorderStroke(strokeWidth, currentBorderColor),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .graphicsLayer {
                        scaleX = if (isPressed) 0.95f else 1.0f
                        scaleY = if (isPressed) 0.95f else 1.0f
                    }
                    .pointerInput(key) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                isLongPressing = false
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                                val timeoutJob = coroutineScope.launch {
                                    delay(500)
                                    if (isPressed) {
                                        isLongPressing = true
                                    }
                                }

                                tryAwaitRelease()
                                timeoutJob.cancel()
                                isPressed = false
                                isLongPressing = false
                            },
                            onTap = {
                                onKeyPress(key)
                            }
                        )
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when (key.type) {
                        KeyType.SHIFT -> NeonShiftIcon(color = textColor)
                        KeyType.BACKSPACE -> NeonBackspaceIcon(color = textColor)
                        KeyType.ENTER -> NeonEnterIcon(color = textColor)
                        KeyType.EMOJI -> NeonEmojiIcon(color = textColor)
                        else -> {
                            val displayLabel = when {
                                key.type == KeyType.CHARACTER && isShifted -> key.label.uppercase()
                                key.type == KeyType.ENTER -> when (imeAction) {
                                    android.view.inputmethod.EditorInfo.IME_ACTION_GO -> "Go"
                                    android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH -> "Search"
                                    android.view.inputmethod.EditorInfo.IME_ACTION_SEND -> "Send"
                                    android.view.inputmethod.EditorInfo.IME_ACTION_NEXT -> "Next"
                                    android.view.inputmethod.EditorInfo.IME_ACTION_DONE -> "Done"
                                    else -> "↵"
                                }
                                else -> key.label
                            }

                            Text(
                                text = displayLabel,
                                color = textColor,
                                fontSize = if (displayLabel.length > 2) 12.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            return
        }

        val assetSkinStyle = theme.assetSkinStyle

        val origamiStyle = theme.origamiPaperCraftStyle
        val pizzaStyle = theme.pizzaSliceStyle
        val steampunkStyle = theme.steampunkIndustrialStyle
        val themeKeyBackgroundsStyle = theme.themeKeyBackgroundsStyle
        val context = androidx.compose.ui.platform.LocalContext.current

        if (assetSkinStyle != null || origamiStyle != null || pizzaStyle != null || steampunkStyle != null || themeKeyBackgroundsStyle != null) {
            val templateResName = when {
                themeKeyBackgroundsStyle != null -> {
                    when (key.type) {
                        KeyType.SPACE -> themeKeyBackgroundsStyle.spaceBackgroundRes
                        KeyType.SHIFT,
                        KeyType.BACKSPACE,
                        KeyType.ENTER,
                        KeyType.SYMBOLS -> themeKeyBackgroundsStyle.specialKeyBackgroundRes
                        KeyType.EMOJI,
                        KeyType.CHARACTER -> themeKeyBackgroundsStyle.emojiBackgroundRes
                        else -> themeKeyBackgroundsStyle.emojiBackgroundRes
                    }
                }
                steampunkStyle != null -> {
                    val raw = key.label.lowercase()
                    when {
                        key.type == KeyType.SPACE -> steampunkStyle.spacebarNixieRes
                        key.type == KeyType.SHIFT || key.type == KeyType.BACKSPACE || key.type == KeyType.ENTER -> steampunkStyle.leatherWideRes
                        key.type == KeyType.EMOJI || key.type == KeyType.SYMBOLS || raw == "," || raw == "." -> steampunkStyle.leatherSquareRes
                        raw.length == 1 && raw[0].isDigit() -> steampunkStyle.nixieTubeRes
                        else -> steampunkStyle.enamelKeyRes
                    }
                }
                pizzaStyle != null -> {
                    val raw = key.label.lowercase()
                    when {
                        key.type == KeyType.SPACE -> pizzaStyle.spacebarRes
                        key.type == KeyType.ENTER || key.type == KeyType.SHIFT -> pizzaStyle.enterKeyRes
                        key.type == KeyType.EMOJI -> pizzaStyle.smileyKeyRes
                        key.type == KeyType.SYMBOLS || key.type == KeyType.BACKSPACE -> pizzaStyle.numberpadKeyRes
                        key.type == KeyType.CHARACTER -> {
                            when (raw) {
                                "," -> pizzaStyle.commaKeyRes
                                "." -> pizzaStyle.fullstopKeyRes
                                "b" -> pizzaStyle.bKeyRes
                                "q" -> pizzaStyle.qKeyRes
                                "w" -> pizzaStyle.pKeyRes
                                "p" -> pizzaStyle.pKeyRes
                                "u" -> pizzaStyle.uKeyRes
                                else -> pizzaStyle.standardKeyRes
                            }
                        }
                        else -> pizzaStyle.standardKeyRes
                    }
                }
                origamiStyle != null -> {
                    when (key.type) {
                        KeyType.SPACE -> origamiStyle.spacebarRes
                        KeyType.SHIFT -> origamiStyle.shiftKeyRes
                        KeyType.BACKSPACE -> origamiStyle.backspaceKeyRes
                        KeyType.ENTER -> origamiStyle.enterKeyRes
                        KeyType.EMOJI, KeyType.GLOBE -> origamiStyle.emojiKeyRes
                        KeyType.SYMBOLS -> origamiStyle.symbolKeyRes
                        KeyType.CHARACTER -> {
                            when (key.label.lowercase()) {
                                "w", "e", "r", "y", "u", "s", "g", "v" -> origamiStyle.purpleKeyRes
                                "i", "o", "j", "k", "b", "n", "," -> origamiStyle.peachKeyRes
                                "p", "l", "m" -> origamiStyle.blueKeyRes
                                else -> origamiStyle.mintKeyRes
                            }
                        }
                        else -> origamiStyle.mintKeyRes
                    }
                }
                assetSkinStyle != null -> {
                    when (key.type) {
                        KeyType.CHARACTER -> assetSkinStyle.letterTemplateRes
                        KeyType.BACKSPACE -> assetSkinStyle.functionTemplateRes
                        KeyType.SPACE -> assetSkinStyle.spacebarRes
                        else -> assetSkinStyle.functionTemplateBlankRes
                    }
                }
                else -> ""
            }
            val resId = remember(templateResName) {
                if (templateResName.isNotEmpty()) {
                    context.resources.getIdentifier(templateResName, "drawable", context.packageName)
                } else 0
            }
            val hasBackgroundAsset = resId != 0

            val effectiveClipShape = if (theme.keyShape == RoundedCornerShape(0.dp)) RoundedCornerShape(8.dp) else theme.keyShape

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = currentSurfaceOffsetYDp)
                    .clip(effectiveClipShape)
            ) {
                if (resId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = resId),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .then(
                                if (hasBorder) {
                                    Modifier.border(
                                        width = theme.keyBorderWidthDp.dp,
                                        color = theme.keyBorderColor!!,
                                        shape = effectiveClipShape
                                    )
                                } else Modifier
                            )
                    )
                }
            }
        } else {
            val isSpaceKey = key.type == KeyType.SPACE
            val effectiveBgColor = when {
                isSpaceKey && !theme.decorativeIcon.isNullOrEmpty() -> {
                    if (backgroundColor.alpha > 0f) backgroundColor.copy(alpha = 0.85f) else Color.Transparent
                }
                isSpaceKey && backgroundColor.alpha == 0f -> {
                    Color.Transparent
                }
                else -> backgroundColor
            }
            val effectiveShape = when {
                isSpaceKey && !theme.decorativeIcon.isNullOrEmpty() -> RoundedCornerShape(8.dp)
                !theme.decorativeIcon.isNullOrEmpty() && theme.keyShape == RoundedCornerShape(0.dp) -> RoundedCornerShape(8.dp)
                else -> theme.keyShape
            }

            if (effectiveBgColor.alpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = currentSurfaceOffsetYDp)
                        .clip(effectiveShape)
                        .then(blurModifier)
                        .background(effectiveBgColor)
                        .then(
                            if (hasBorder) {
                                Modifier.border(
                                    width = theme.keyBorderWidthDp.dp,
                                    color = theme.keyBorderColor!!,
                                    shape = effectiveShape
                                )
                            } else Modifier
                        )
                )
            } else if (hasBorder) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = currentSurfaceOffsetYDp)
                        .border(
                            width = theme.keyBorderWidthDp.dp,
                            color = theme.keyBorderColor!!,
                            shape = effectiveShape
                        )
                )
            }
        }

        // ── Sharp & Unblurred Key Label & Interaction Layer ────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = currentSurfaceOffsetYDp)
                .pointerInput(key) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            wasLongPressed = false
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                            if (key.type == KeyType.BACKSPACE) {
                                // Immediately delete 1 char on touch down for responsive single click
                                onKeyPress(key)
                                isLongPressing = true
                            }

                            val hasLongPressShortcut = (showNumberHints && key.numberHint != null)
                            val released = if (hasLongPressShortcut) {
                                val releaseResult = withTimeoutOrNull(400) {
                                    tryAwaitRelease()
                                }
                                if (releaseResult == null) {
                                    // Long-press threshold reached while still pressed
                                    wasLongPressed = true
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    onKeyPress(KeyData(label = key.numberHint!!, output = key.numberHint!!, type = KeyType.CHARACTER))
                                    tryAwaitRelease()
                                    true
                                } else {
                                    releaseResult
                                }
                            } else {
                                tryAwaitRelease()
                            }

                            isPressed = false
                            isLongPressing = false

                            if (released && key.type != KeyType.BACKSPACE && !wasLongPressed) {
                                onKeyPress(key)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Corner hint badge for long-press number shortcut on top letter row
            if (showNumberHints && key.numberHint != null) {
                Text(
                    text = key.numberHint,
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 4.dp)
                )
            }

            // Pressed Key Highlight Bubble (Message Logo Popup)
            val bubbleSymbol = if (wasLongPressed && showNumberHints && key.numberHint != null) key.numberHint else displayLabel
            if (isPressed && bubbleSymbol.isNotEmpty() && key.type != KeyType.SPACE) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, -170),
                    properties = PopupProperties(
                        focusable = false,
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                        clippingEnabled = false
                    )
                ) {
                    MessageBubblePopup(
                        symbol = bubbleSymbol,
                        theme = theme
                    )
                }
            }

            val origamiStyle = theme.origamiPaperCraftStyle
            val pizzaStyle = theme.pizzaSliceStyle

            Box(contentAlignment = Alignment.Center) {
                val isCharacterKey = key.type == KeyType.CHARACTER
                val hasBackgroundAsset = themeKeyBackgroundsStyle?.let { style ->
                    val resName = when (key.type) {
                        KeyType.SPACE -> style.spaceBackgroundRes
                        KeyType.SHIFT, KeyType.BACKSPACE, KeyType.ENTER, KeyType.SYMBOLS -> style.specialKeyBackgroundRes
                        KeyType.EMOJI, KeyType.CHARACTER -> style.emojiBackgroundRes
                        else -> style.emojiBackgroundRes
                    }
                    if (resName.isNotEmpty()) {
                        context.resources.getIdentifier(resName, "drawable", context.packageName) != 0
                    } else false
                } ?: false

                val shouldDrawDecorativeIcon = !theme.decorativeIcon.isNullOrEmpty() && (
                    if (themeKeyBackgroundsStyle != null) (isCharacterKey && !hasBackgroundAsset) else key.type != KeyType.SPACE
                )

                if (shouldDrawDecorativeIcon) {
                    val decorativeAlpha = theme.keyOpacityAlpha.coerceIn(0.0f, 1.0f)
                    val iconName = theme.decorativeIcon!!
                    val context = LocalContext.current
                    val resId = remember(iconName) {
                        val direct = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                        if (direct != 0) direct else {
                            context.resources.getIdentifier("ic_popular_$iconName", "drawable", context.packageName)
                        }
                    }
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(38.dp)
                                .graphicsLayer(alpha = decorativeAlpha),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = iconName,
                            fontSize = 34.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.graphicsLayer(alpha = decorativeAlpha)
                        )
                    }
                }
            }

            if (pizzaStyle != null) {
                val sauceColor = remember(pizzaStyle.sauceTextColorHex) { KeyboardThemeMapper.parseColor(pizzaStyle.sauceTextColorHex) }
                val gorgonzolaColor = remember(pizzaStyle.gorgonzolaTextColorHex) { KeyboardThemeMapper.parseColor(pizzaStyle.gorgonzolaTextColorHex) }
                val enterColor = remember(pizzaStyle.enterTextColorHex) { KeyboardThemeMapper.parseColor(pizzaStyle.enterTextColorHex) }

                val rawLabel = key.label.lowercase()

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (key.type) {
                        KeyType.SPACE, KeyType.ENTER, KeyType.EMOJI, KeyType.SYMBOLS -> {
                            // Dedicated PNG assets handle the key visual representation
                        }
                        KeyType.SHIFT -> {
                            val shiftSymbol = if (isCapsLock) "⇪" else "⇧"
                            Text(
                                text = shiftSymbol,
                                color = gorgonzolaColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        KeyType.BACKSPACE -> {
                            Text(
                                text = "⌫",
                                color = gorgonzolaColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        else -> {
                            val labelText = if (key.type == KeyType.CHARACTER && isShifted) key.label.uppercase() else displayLabel
                            val keyTextColor = sauceColor

                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = labelText,
                                    color = Color(0xFF580B02),
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.offset(y = 1.5.dp)
                                )
                                Text(
                                    text = labelText,
                                    color = keyTextColor,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else if (origamiStyle != null) {
                val plumColor = remember(origamiStyle.labelColorHex) { KeyboardThemeMapper.parseColor(origamiStyle.labelColorHex) }
                val labelText = when {
                    key.type == KeyType.CHARACTER && isShifted -> key.label.uppercase()
                    key.type == KeyType.SPACE -> "space"
                    key.type == KeyType.ENTER -> getEnterLabel(imeAction)
                    key.type == KeyType.SHIFT -> if (isCapsLock) "⇪" else "⇧"
                    key.type == KeyType.BACKSPACE -> "⌫"
                    key.type == KeyType.EMOJI -> "😊"
                    else -> displayLabel
                }
                Text(
                    text = labelText,
                    color = plumColor,
                    fontSize = if (key.type == KeyType.SPACE || key.type == KeyType.SYMBOLS) 13.sp else fontSize,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            } else if (steampunkStyle != null) {
                val rawLabel = key.label.lowercase()
                val isDigitKey = rawLabel.length == 1 && rawLabel[0].isDigit()
                val isLeatherKey = key.type == KeyType.SHIFT || key.type == KeyType.BACKSPACE || key.type == KeyType.ENTER || key.type == KeyType.EMOJI || key.type == KeyType.SYMBOLS || rawLabel == "," || rawLabel == "."

                when {
                    isDigitKey -> {
                        // Nixie tube glowing amber digit
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayLabel,
                                color = Color(0xFFFF9326),
                                fontSize = 22.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.offset(y = (-1).dp)
                            )
                            Text(
                                text = displayLabel,
                                color = Color(0xFFFFF5D9),
                                fontSize = 22.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.offset(y = (-1).dp)
                            )
                        }
                    }
                    key.type == KeyType.SPACE -> {
                        // Vacuum tube spacebar with illuminated orange sine-wave filament
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "∿ ∿ ∿",
                                color = Color(0xFFFF9326),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    isLeatherKey -> {
                        val leatherColor = remember(steampunkStyle.leatherTextColorHex) { KeyboardThemeMapper.parseColor(steampunkStyle.leatherTextColorHex) }
                        val labelText = when {
                            key.type == KeyType.SHIFT -> if (isCapsLock) "⇪" else "⇧"
                            key.type == KeyType.BACKSPACE -> "⌫"
                            key.type == KeyType.ENTER -> "↵"
                            key.type == KeyType.EMOJI -> "😊"
                            else -> displayLabel
                        }
                        Text(
                            text = labelText,
                            color = leatherColor,
                            fontSize = if (key.type == KeyType.SPACE || key.type == KeyType.SYMBOLS) 13.sp else fontSize,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    else -> {
                        val enamelColor = remember(steampunkStyle.enamelTextColorHex) { KeyboardThemeMapper.parseColor(steampunkStyle.enamelTextColorHex) }
                        val labelText = if (key.type == KeyType.CHARACTER && isShifted) key.label.uppercase() else displayLabel
                        Text(
                            text = labelText,
                            color = enamelColor,
                            fontSize = fontSize,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            } else if (assetSkinStyle != null) {
                val darkColor = remember(assetSkinStyle.embossDarkColorHex) { KeyboardThemeMapper.parseColor(assetSkinStyle.embossDarkColorHex) }
                val lightColor = remember(assetSkinStyle.embossLightColorHex) { KeyboardThemeMapper.parseColor(assetSkinStyle.embossLightColorHex) }
                val brassColor = remember(assetSkinStyle.brassTextColorHex) { KeyboardThemeMapper.parseColor(assetSkinStyle.brassTextColorHex) }

                when (key.type) {
                    KeyType.SPACE -> {
                        // Spacebar plaque art already includes carved "SPACE" — no text overlay needed
                    }
                    KeyType.BACKSPACE -> {
                        // Backspace template art already includes baked arrow+X icon — no overlay needed
                    }
                    KeyType.CHARACTER -> {
                        // Dual-shadow embossed wood carved character label overlay
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayLabel,
                                color = darkColor,
                                fontSize = fontSize,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.offset(x = 1.dp, y = 1.dp)
                            )
                            Text(
                                text = displayLabel,
                                color = lightColor,
                                fontSize = fontSize,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.offset(x = (-0.5).dp, y = (-0.5).dp)
                            )
                            Text(
                                text = displayLabel,
                                color = darkColor,
                                fontSize = fontSize,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                    KeyType.SHIFT -> {
                        val shiftSymbol = when {
                            isCapsLock -> "⇪"
                            isShifted -> "⇧"
                            else -> "⇧"
                        }
                        val activeBrassColor = if (isShifted || isCapsLock) Color(0xFFFFD700) else brassColor
                        Text(
                            text = shiftSymbol,
                            color = activeBrassColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    KeyType.ENTER -> {
                        Text(
                            text = getEnterLabel(imeAction),
                            color = brassColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = displayLabel,
                            color = brassColor,
                            fontSize = fontSize,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center) {
                    val isCharacterKey = key.type == KeyType.CHARACTER
                    val hasBackgroundAsset = themeKeyBackgroundsStyle?.let { style ->
                        val resName = when (key.type) {
                            KeyType.SPACE -> style.spaceBackgroundRes
                            KeyType.SHIFT, KeyType.BACKSPACE, KeyType.ENTER, KeyType.SYMBOLS -> style.specialKeyBackgroundRes
                            KeyType.EMOJI, KeyType.CHARACTER -> style.emojiBackgroundRes
                            else -> style.emojiBackgroundRes
                        }
                        if (resName.isNotEmpty()) {
                            context.resources.getIdentifier(resName, "drawable", context.packageName) != 0
                        } else false
                    } ?: false

                    val shouldDrawDecorativeIcon = !theme.decorativeIcon.isNullOrEmpty() && (
                        if (themeKeyBackgroundsStyle != null) (isCharacterKey && !hasBackgroundAsset) else key.type != KeyType.SPACE
                    )

                    if (shouldDrawDecorativeIcon) {
                        val decorativeAlpha = theme.keyOpacityAlpha.coerceIn(0.0f, 1.0f)
                        val iconName = theme.decorativeIcon!!
                        val context = LocalContext.current
                        val resId = remember(iconName) {
                            val direct = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                            if (direct != 0) direct else {
                                context.resources.getIdentifier("ic_popular_$iconName", "drawable", context.packageName)
                            }
                        }
                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .graphicsLayer(alpha = decorativeAlpha),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = iconName,
                                fontSize = 34.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.graphicsLayer(alpha = decorativeAlpha)
                            )
                        }
                    }

                    val shouldDrawText = when {
                        themeKeyBackgroundsStyle != null && key.type == KeyType.EMOJI -> false
                        else -> true
                    }

                    if (shouldDrawText) {
                        Text(
                            text = displayLabel,
                            color = textColor,
                            fontSize = fontSize,
                            fontFamily = activeFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(shadow = textShadow),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

        }
    }
}

/**
 * Highlight popup bubble displayed above a key when pressed, styled like a message logo callout.
 */
@Composable
private fun MessageBubblePopup(
    symbol: String,
    theme: ResolvedTheme
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp
        ),
        color = theme.accent,
        shadowElevation = 8.dp,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 62.dp)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🐼",
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = symbol,
                    color = Color.White,
                    fontSize = if (symbol.length > 2) 15.sp else 26.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Get the appropriate label for the Enter key based on the IME action.
 */
private fun getEnterLabel(imeAction: Int): String {
    return when (imeAction) {
        android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH -> "🔍"
        android.view.inputmethod.EditorInfo.IME_ACTION_SEND -> "➤"
        android.view.inputmethod.EditorInfo.IME_ACTION_NEXT -> "→"
        android.view.inputmethod.EditorInfo.IME_ACTION_DONE -> "✓"
        android.view.inputmethod.EditorInfo.IME_ACTION_GO -> "➜"
        else -> "↵"
    }
}



@Composable
private fun NeonShiftIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFF2A85)) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(20.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.15f)
            lineTo(size.width * 0.2f, size.height * 0.48f)
            lineTo(size.width * 0.35f, size.height * 0.48f)
            lineTo(size.width * 0.35f, size.height * 0.85f)
            lineTo(size.width * 0.65f, size.height * 0.85f)
            lineTo(size.width * 0.65f, size.height * 0.48f)
            lineTo(size.width * 0.8f, size.height * 0.48f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun NeonBackspaceIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFF2A85)) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(22.dp)) {
        val tagPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.5f)
            lineTo(size.width * 0.35f, size.height * 0.18f)
            lineTo(size.width * 0.9f, size.height * 0.18f)
            lineTo(size.width * 0.9f, size.height * 0.82f)
            lineTo(size.width * 0.35f, size.height * 0.82f)
            close()
        }
        drawPath(
            path = tagPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        // X mark inside tag
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.48f, size.height * 0.36f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.74f, size.height * 0.64f),
            strokeWidth = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.74f, size.height * 0.36f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.48f, size.height * 0.64f),
            strokeWidth = 1.8.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun NeonEnterIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFF2A85)) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(20.dp)) {
        val arrowPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.75f, size.height * 0.3f)
            lineTo(size.width * 0.75f, size.height * 0.65f)
            lineTo(size.width * 0.25f, size.height * 0.65f)
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        val headPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.45f)
            lineTo(size.width * 0.25f, size.height * 0.65f)
            lineTo(size.width * 0.45f, size.height * 0.85f)
        }
        drawPath(
            path = headPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun NeonEmojiIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFF2A85)) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(20.dp)) {
        val radius = size.width * 0.42f
        val center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f)
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
        )
        // Eyes
        drawCircle(color = color, radius = 1.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.36f, size.height * 0.4f))
        drawCircle(color = color, radius = 1.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * 0.64f, size.height * 0.4f))
        // Smile Arc
        val smilePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.33f, size.height * 0.6f)
            quadraticTo(
                size.width * 0.5f, size.height * 0.78f,
                size.width * 0.67f, size.height * 0.6f
            )
        }
        drawPath(
            path = smilePath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.8.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}
